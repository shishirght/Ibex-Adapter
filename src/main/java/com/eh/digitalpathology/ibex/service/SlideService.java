/**
 * SlideService is used to fetch all the required data for slide creation and create the slide.
 * Author: Preeti Ankam
 * Date: October 30, 2024
 */

package com.eh.digitalpathology.ibex.service;

import com.eh.digitalpathology.ibex.client.IbexApiClient;
import com.eh.digitalpathology.ibex.config.OrganMappingConfig;
import com.eh.digitalpathology.ibex.constants.AppConstants;
import com.eh.digitalpathology.ibex.enums.StainNames;
import com.eh.digitalpathology.ibex.exceptions.IbexApiException;
import com.eh.digitalpathology.ibex.exceptions.SignedUrlGenerationException;
import com.eh.digitalpathology.ibex.model.BarcodeInstanceRequest;
import com.eh.digitalpathology.ibex.model.Slide;
import com.eh.digitalpathology.ibex.model.SlideScanProgressEvent;
import com.eh.digitalpathology.ibex.util.AppUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RefreshScope
public class SlideService {
    private static final Logger logger = LoggerFactory.getLogger(SlideService.class);
    private final SignedUrlCreationService signedUrlCreationService;
    private final IbexApiClient ibexApiClient;
    private final OrganMappingConfig organMappingConfig;
    private final ApiSignedUrlService apiSignedUrlService;
    private final KafkaNotificationProducer kafkaNotificationProducer;

    @Value( "${network.enabled}" )
    private boolean networkEnabled;

    public SlideService( SignedUrlCreationService signedUrlCreationService, IbexApiClient ibexApiClient, OrganMappingConfig organMappingConfig, KafkaNotificationProducer kafkaNotificationProducer, ApiSignedUrlService apiSignedUrlService ) {
        this.signedUrlCreationService = signedUrlCreationService;
        this.ibexApiClient = ibexApiClient;
        this.organMappingConfig = organMappingConfig;
        this.kafkaNotificationProducer = kafkaNotificationProducer;
        this.apiSignedUrlService = apiSignedUrlService;
    }

    public void createSlide(Map<String, Object> metadataMap, String studyUid, String seriesUid, String sourceUrl, String barcode) throws IbexApiException {
        String caseId = AppUtil.getTagValue(metadataMap, AppConstants.ACCESSION_NO_TAG_KEY);
        Map<String, String> organCodeMap = AppUtil.extractOrganType(metadataMap, AppConstants.SPECIMEN_DESCRIPTION_SEQUENCE, AppConstants.PRIMARY_ANATOMIC_STRUCTURE_SEQ, AppConstants.CODE_VALUE, AppConstants.CODE_MEANING);

        if (organCodeMap.isEmpty()) {
            logger.warn("createSlide :: No organ code found in metadata. Skipping case and slide creation.");
            return;
        }
        organCodeMap.forEach((key, value) -> logger.info("buildSlide :: Code key : {} Code value: {}", key, value));

        String organCode = organCodeMap.values().iterator().next().toLowerCase();
        String organ = resolveOrgan(organCode);

        if (isAllowedOrgan(organ)) {
            logger.info("createSlide :: Proceeding with organ: {}", organ);
        } else {
            SlideScanProgressEvent slideScanProgressEvent = new SlideScanProgressEvent(barcode,"ibex-warning-completed","Unsupported organ detected: "+organ);
            kafkaNotificationProducer.notifyScanProgress(slideScanProgressEvent);
            logger.info("createSlide :: Unsupported organ detected: '{}'. Skipping case and slide creation.",
                    organ);
            return;
        }

        if (AppUtil.validate(barcode) && AppUtil.validate(caseId)) {
            Slide newSlide = buildSlide(metadataMap, sourceUrl, caseId, barcode, organ);
            String jsonRequestBody = AppUtil.convertObjectToString(newSlide);
            if (jsonRequestBody != null) {
                try {
                    ibexApiClient.createSlide(jsonRequestBody, barcode,seriesUid);
                    logger.info("createSlide :: Case and slide creation completed for study uid: {} , series uid:{}.", studyUid, seriesUid);
                } catch (IbexApiException ex) {
                    throw new IbexApiException(ex);
                }
            }
        } else {
            String errorMessage = String.format("400 BAD REQUEST: CHECK REQUEST DATA: Invalid Barcode value: %s or Invalid Case Id: %s for study uid: %s , series uid:%s", barcode, caseId, studyUid, seriesUid);
            BarcodeInstanceRequest barcodeInstanceRequest = new BarcodeInstanceRequest(barcode,null,seriesUid,errorMessage);
            kafkaNotificationProducer.notifyScanProgressAndUpdateDicomInstances(new SlideScanProgressEvent(barcode,"ibex-invalid-data"),barcodeInstanceRequest);
            throw new IbexApiException(errorMessage);
        }
    }

    private boolean isAllowedOrgan(String organ) {
        logger.info("Allowed organs from config: {}", organMappingConfig.getAllowed());
        return organMappingConfig.getAllowed().contains(organ.toLowerCase());
    }


    private Slide buildSlide(Map<String, Object> metadataMap, String sourceUrl, String caseId, String barcodeValue, String organ) {
        Optional<String> stain = AppUtil.getStainValue(metadataMap);
        Slide newSlide = new Slide();
        newSlide.setId(barcodeValue);
        newSlide.setCaseId(caseId);
        newSlide.getDetails().setOrganType(organ);

        if (stain.isPresent()) {
            logger.info("buildSlide :: Stain is present.");
            StainNames stainEnum = StainNames.fromStain(stain.get());
            newSlide.getDetails().setStain(stainEnum.getStain());

            if ("Other".equalsIgnoreCase(stainEnum.getStain())) {
                newSlide.getDetails().setStainName(stain.get());
            }
        } else {
            logger.info("buildSlide :: No stain present for case id: {} and slide id: {}", caseId, barcodeValue);
        }

        if (networkEnabled) {
            newSlide.setSource( getSignedUrl( sourceUrl ) );
        }else{
            newSlide.setSource( getApiSignedUrl( sourceUrl ) );
        }
        // Optional fields
        newSlide.setScannedDate(LocalDate.now().toString());
        newSlide.setFileExt("zip");
        logger.info( "buildSlide :: newSlide :: {}", newSlide );
        return newSlide;
    }

    private String getSignedUrl(String sourceUrl) {
        try {
            return signedUrlCreationService.generateSignedUrl(sourceUrl);
        } catch (IOException ex) {
            throw new SignedUrlGenerationException("Failed to create signed URL: ", ex);
        }
    }

    private String getApiSignedUrl(String sourceUrl)  {
        try {
            logger.info("getApiSignedUrl :: Generating signed URL for source URL: {}", sourceUrl);
            URI uri = new URI( sourceUrl );
            Pattern urlPattern = Pattern.compile( "/b/([^/]+)/o/([^?]+)" );
            Matcher urlMatcher = urlPattern.matcher( uri.getPath( ) );

            if ( !urlMatcher.find( ) ) {
                throw new IllegalArgumentException( "Invalid GCS media URL format" );
            }

            String bucketName = urlMatcher.group( 1 );
            String objectNameEncoded = urlMatcher.group( 2 );
            String objectName = URLDecoder.decode( objectNameEncoded, StandardCharsets.UTF_8 );
            logger.info( "getApiSignedUrl :: bucketName :: {}", bucketName );
            logger.info( "getApiSignedUrl :: objectName :: {}", objectName );

            // Optional generation param
            String query = uri.getQuery( );
            Long generation = null;
            if ( query != null ) {
                for ( String param : query.split( "&" ) ) {
                    String[] kv = param.split( "=", 2 );
                    if ( kv.length == 2 && kv[ 0 ].equals( "generation" ) ) {
                        generation = Long.parseLong( kv[ 1 ] );
                        break;
                    }
                }
            }

            return apiSignedUrlService.generateApiSignedUrl( bucketName, objectName, generation );
        }catch ( Exception e ){
            throw new SignedUrlGenerationException( "Failed to create signed URL: ", e );
        }
    }

    public String resolveOrgan(String rawValue) {
        String valueFromYaml = organMappingConfig.getMapping().getOrDefault(rawValue.trim().toLowerCase(), rawValue);
        logger.info("resolveOrgan :: Fetched organ value from YAML is: {}", valueFromYaml);
        return valueFromYaml;
    }



}
