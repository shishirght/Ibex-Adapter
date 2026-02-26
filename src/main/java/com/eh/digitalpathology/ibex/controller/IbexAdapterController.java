/**
 * IbexAdapterController contains the controller of ibex adapter.
 * Author: Preeti Ankam
 * Date:December 01, 2024
 */

package com.eh.digitalpathology.ibex.controller;

import com.eh.digitalpathology.ibex.client.IbexApiClient;
import com.eh.digitalpathology.ibex.enums.IbexEventTypes;
import com.eh.digitalpathology.ibex.exceptions.IbexApiException;
import com.eh.digitalpathology.ibex.model.BarcodeInstanceRequest;
import com.eh.digitalpathology.ibex.model.Event;
import com.eh.digitalpathology.ibex.model.SlideScanProgressEvent;
import com.eh.digitalpathology.ibex.service.BarcodeStudyInfo;
import com.eh.digitalpathology.ibex.service.IbexAdapterService;
import com.eh.digitalpathology.ibex.service.KafkaNotificationProducer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RefreshScope
@RestController
public class IbexAdapterController {
    @Value("${kafka.topic.email}")
    private String emailSvcTopic;

    private static final Logger logger = LoggerFactory.getLogger(IbexAdapterController.class);
    private final IbexAdapterService ibexAdapterService;
    private final IbexApiClient ibexApiClient;
    private final KafkaNotificationProducer kafkaNotificationProducer;
    private final BarcodeStudyInfo barcodeStudyInfo;

    public IbexAdapterController(IbexAdapterService ibexAdapterService, IbexApiClient ibexApiClient, KafkaNotificationProducer kafkaNotificationProducer, BarcodeStudyInfo barcodeStudyInfo) {
        this.ibexAdapterService = ibexAdapterService;
        this.ibexApiClient = ibexApiClient;
        this.kafkaNotificationProducer = kafkaNotificationProducer;
        this.barcodeStudyInfo = barcodeStudyInfo;
    }

    @GetMapping("/labelling")
    public String getLabel() throws IbexApiException {
        try {
            return ibexApiClient.getLabelingResources();
        } catch (IbexApiException e) {
            throw new IbexApiException("Exception occurred while getting the labels ", e);
        }
    }

    @PostMapping("/webhook/digitalpathology/system")
    public ResponseEntity<String> handleEvent(@RequestBody Event event) throws JsonProcessingException {
        logger.info("Event received {}", event.getEventType());
        ObjectMapper objectMapper = new ObjectMapper();
        IbexEventTypes eventType;
        String errorMessage = null;
        try {
            eventType = IbexEventTypes.fromEvent(event.getEventType());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Event type could not be found");
        }
        final String studyId = barcodeStudyInfo.get(event.getSubjectId());
        String logMessage = switch (Objects.requireNonNull(eventType)) {
            case SLIDE_DOWNLOAD_COMPLETED -> {
                logger.info("handleEvent :: Download completed successfully for Slide ID: {}", event.getSubjectId());
                kafkaNotificationProducer.sendNotification(emailSvcTopic, "IBEX_SLIDE_DOWNLOAD_COMPLETED", objectMapper.writeValueAsString(event));
                yield "Download completed";
            }
            case SLIDE_DOWNLOAD_FAILED -> {
                logger.error("handleEvent :: Download failed for Slide ID: {}", event.getSubjectId());
                errorMessage = "Download failed for Slide ID: "+event.getSubjectId();
                kafkaNotificationProducer.sendNotification(emailSvcTopic, "IBEX_SLIDE_DOWNLOAD_FAILED", objectMapper.writeValueAsString(event));
                yield "Download failed";
            }
            case INVALID_SLIDE -> {
                logger.error("handleEvent :: Slide is invalid error for Slide ID: {}", event.getSubjectId());
                errorMessage = "Slide is invalid error for Slide ID: "+event.getSubjectId();
                kafkaNotificationProducer.sendNotification(emailSvcTopic, "IBEX_INVALID_SLIDE", objectMapper.writeValueAsString(event));
                yield "Invalid slide";
            }
            case CLASSIFICATION_FINISHED -> {
                logger.info("handleEvent :: Classification is finished for Slide ID: {}", event.getSubjectId());
                kafkaNotificationProducer.sendNotification(emailSvcTopic, "IBEX_CLASSIFICATION_FINISHED", objectMapper.writeValueAsString(event));
                ibexAdapterService.deleteFolderFromBucket(event.getSubjectId());

                logger.info("handleEvent :: Fetching findings of slide: {}", event.getSubjectId());
                ibexAdapterService.retrieveFindings(event.getSubjectId());

                logger.info("handleEvent :: Fetching heatmaps of slide: {}", event.getSubjectId());
                ibexAdapterService.retrieveHeatmaps(event.getSubjectId());
                yield "Classification finished";
            }
            case CLASSIFICATION_FAILED -> {
                logger.error("handleEvent :: Classification is failed for Slide ID: {}", event.getSubjectId());
                errorMessage = "Classification is failed for Slide ID: "+event.getSubjectId();
                kafkaNotificationProducer.sendNotification(emailSvcTopic, "IBEX_CLASSIFICATION_FAILED", objectMapper.writeValueAsString(event));
                yield "Classification failed";
            }
            case REPORT_SUBMITTED -> {
                logger.info("handleEvent :: Report is submitted for Slide ID: {}", event.getSubjectId());
                kafkaNotificationProducer.sendNotification(emailSvcTopic, "IBEX_REPORT_SUBMITTED", objectMapper.writeValueAsString(event));
                yield "Report submitted";
            }

            default -> {
                logger.warn("handleEvent :: Unsupported event type received: {}", eventType);
                kafkaNotificationProducer.sendNotification(emailSvcTopic, "IBEX_UNSUPPORTED_EVENT", objectMapper.writeValueAsString(event));
                yield "Unsupported event type";
            }
        };
        updateAndNotifyScanProgress(event.getSubjectId(),studyId,"ibex-".concat(eventType.getEventType()),errorMessage);
        logger.info("logMessage :: log messages :: {}", logMessage);
        return ResponseEntity.ok("Event processed successfully...");
    }

    private void updateAndNotifyScanProgress(String barcode, String studyId, String eventType,String errorMessage) {

        SlideScanProgressEvent slideScanProgressEvent = new SlideScanProgressEvent(barcode,eventType);
        BarcodeInstanceRequest barcodeInstanceRequest = new BarcodeInstanceRequest(barcode,studyId,null,errorMessage);
        if (StringUtils.hasText(studyId)) {
            kafkaNotificationProducer.notifyScanProgressAndUpdateDicomInstances(slideScanProgressEvent,barcodeInstanceRequest);
        }else {
            kafkaNotificationProducer.notifyScanProgress(slideScanProgressEvent);
        }
    }

}
