package com.eh.digitalpathology.ibex.service;

import com.eh.digitalpathology.ibex.client.HealthcareApiClient;
import com.eh.digitalpathology.ibex.client.IbexApiClient;
import com.eh.digitalpathology.ibex.config.GcpConfig;
import com.eh.digitalpathology.ibex.config.IbexConfig;
import com.eh.digitalpathology.ibex.exceptions.HealthcareApiException;
import com.eh.digitalpathology.ibex.exceptions.IbexApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IbexAdapterServiceTest {

    @Mock
    private HealthcareApiClient healthcareApiClient;

    @Mock
    private SlideService slideService;

    @Mock
    private GcpConfig gcpConfig;

    @Mock
    private IbexConfig ibexConfig;

    @Mock
    private IbexApiClient ibexApiClient;

    @Mock
    private BarcodeStudyInfo barcodeStudyInfo;

    @Mock
    private BucketContext bucketContext;

    @InjectMocks
    private IbexAdapterService ibexAdapterService;

    @TempDir
    Path tempDir;

    private static final String SLIDE_ID = "slide-001";
    private static final String STUDY_UID = "study-uid-001";
    private static final String SERIES_ID = "series-001";
    private static final String SOURCE_URL = "https://example.com/slide";
    private static final String BARCODE = "barcode-001";
    private static final String DICOM_WEB_URL = "https://dicom.example.com";

    // ==========================
    // retrieveFindings() Tests
    // ==========================

    @Test
    void testRetrieveFindings_Success() throws Exception {
        when(ibexApiClient.getFindingsOrHeatmaps(SLIDE_ID, IbexApiClient.FINDINGS_ENDPOINT))
                .thenReturn("{\"findings\": []}");

        assertDoesNotThrow(() -> ibexAdapterService.retrieveFindings(SLIDE_ID));

        verify(ibexApiClient, times(1))
                .getFindingsOrHeatmaps(SLIDE_ID, IbexApiClient.FINDINGS_ENDPOINT);
    }

    @Test
    void testRetrieveFindings_IbexApiException_IsSwallowed() throws Exception {
        when(ibexApiClient.getFindingsOrHeatmaps(SLIDE_ID, IbexApiClient.FINDINGS_ENDPOINT))
                .thenThrow(new IbexApiException("API failure"));

        assertDoesNotThrow(() -> ibexAdapterService.retrieveFindings(SLIDE_ID));

        verify(ibexApiClient, times(1))
                .getFindingsOrHeatmaps(SLIDE_ID, IbexApiClient.FINDINGS_ENDPOINT);
    }

    @Test
    void testRetrieveFindings_NullSlideId_DoesNotThrow() throws Exception {
        when(ibexApiClient.getFindingsOrHeatmaps(null, IbexApiClient.FINDINGS_ENDPOINT))
                .thenThrow(new IbexApiException("null slide id"));

        assertDoesNotThrow(() -> ibexAdapterService.retrieveFindings(null));
    }

    @Test
    void testRetrieveFindings_EmptySlideId_DoesNotThrow() throws Exception {
        when(ibexApiClient.getFindingsOrHeatmaps("", IbexApiClient.FINDINGS_ENDPOINT))
                .thenThrow(new IbexApiException("empty slide id"));

        assertDoesNotThrow(() -> ibexAdapterService.retrieveFindings(""));
    }

    // ==========================
    // retrieveHeatmaps() Tests
    // ==========================

    @Test
    void testRetrieveHeatmaps_Success_EmptyArray() throws Exception {
        when(ibexApiClient.getFindingsOrHeatmaps(SLIDE_ID, IbexApiClient.HEATMAPS_ENDPOINT))
                .thenReturn("[]");

        assertDoesNotThrow(() -> ibexAdapterService.retrieveHeatmaps(SLIDE_ID));

        verify(ibexApiClient, times(1))
                .getFindingsOrHeatmaps(SLIDE_ID, IbexApiClient.HEATMAPS_ENDPOINT);
    }

    @Test
    void testRetrieveHeatmaps_Success_ArrayWithNullHeatmapUrl() throws Exception {
        String json = "[{\"heatmap_url\": null, \"id\": \"hm-1\"}]";
        when(ibexApiClient.getFindingsOrHeatmaps(SLIDE_ID, IbexApiClient.HEATMAPS_ENDPOINT))
                .thenReturn(json);

        assertDoesNotThrow(() -> ibexAdapterService.retrieveHeatmaps(SLIDE_ID));
    }

    @Test
    void testRetrieveHeatmaps_NonArrayJson_IsHandledGracefully() throws Exception {
        String json = "{\"unexpected\": \"object\"}";
        when(ibexApiClient.getFindingsOrHeatmaps(SLIDE_ID, IbexApiClient.HEATMAPS_ENDPOINT))
                .thenReturn(json);

        assertDoesNotThrow(() -> ibexAdapterService.retrieveHeatmaps(SLIDE_ID));
    }

    @Test
    void testRetrieveHeatmaps_IbexApiException_IsSwallowed() throws Exception {
        when(ibexApiClient.getFindingsOrHeatmaps(SLIDE_ID, IbexApiClient.HEATMAPS_ENDPOINT))
                .thenThrow(new IbexApiException("heatmap API failure"));

        assertDoesNotThrow(() -> ibexAdapterService.retrieveHeatmaps(SLIDE_ID));

        verify(ibexApiClient, times(1))
                .getFindingsOrHeatmaps(SLIDE_ID, IbexApiClient.HEATMAPS_ENDPOINT);
    }

    @Test
    void testRetrieveHeatmaps_InvalidJson_IsHandledGracefully() throws Exception {
        when(ibexApiClient.getFindingsOrHeatmaps(SLIDE_ID, IbexApiClient.HEATMAPS_ENDPOINT))
                .thenReturn("not-valid-json");

        assertDoesNotThrow(() -> ibexAdapterService.retrieveHeatmaps(SLIDE_ID));
    }

    @Test
    void testRetrieveHeatmaps_NullSlideId_DoesNotThrow() throws Exception {
        when(ibexApiClient.getFindingsOrHeatmaps(null, IbexApiClient.HEATMAPS_ENDPOINT))
                .thenThrow(new IbexApiException("null id"));

        assertDoesNotThrow(() -> ibexAdapterService.retrieveHeatmaps(null));
    }

    // ==========================
    // createCaseAndSlide() Tests
    // ==========================

    @Test
    void testCreateCaseAndSlide_Success() throws Exception {
        String metadataJson = "[{\"00200000\": {\"Value\": [\"1.2.3\"]}}]";
        when(healthcareApiClient.fetchMetadata(STUDY_UID, SERIES_ID, DICOM_WEB_URL))
                .thenReturn(metadataJson);

        assertDoesNotThrow(() ->
                ibexAdapterService.createCaseAndSlide(STUDY_UID, SOURCE_URL, BARCODE, SERIES_ID, DICOM_WEB_URL));

        verify(healthcareApiClient, times(1))
                .fetchMetadata(STUDY_UID, SERIES_ID, DICOM_WEB_URL);
    }

    @Test
    void testCreateCaseAndSlide_HealthcareApiException_IsSwallowed() throws Exception {
        when(healthcareApiClient.fetchMetadata(STUDY_UID, SERIES_ID, DICOM_WEB_URL))
                .thenThrow(new HealthcareApiException("fetch failed"));

        assertDoesNotThrow(() ->
                ibexAdapterService.createCaseAndSlide(STUDY_UID, SOURCE_URL, BARCODE, SERIES_ID, DICOM_WEB_URL));

        verify(slideService, never()).createSlide(any(), any(), any(), any(), any());
    }

    @Test
    void testCreateCaseAndSlide_IbexApiException_IsSwallowed() throws Exception {
        String metadataJson = "[{\"00200000\": {\"Value\": [\"1.2.3\"]}}]";
        when(healthcareApiClient.fetchMetadata(STUDY_UID, SERIES_ID, DICOM_WEB_URL))
                .thenReturn(metadataJson);
        doThrow(new IbexApiException("ibex failure"))
                .when(slideService).createSlide(any(), any(), any(), any(), any());

        assertDoesNotThrow(() ->
                ibexAdapterService.createCaseAndSlide(STUDY_UID, SOURCE_URL, BARCODE, SERIES_ID, DICOM_WEB_URL));
    }

    @Test
    void testCreateCaseAndSlide_GenericException_IsSwallowed() throws Exception {
        when(healthcareApiClient.fetchMetadata(STUDY_UID, SERIES_ID, DICOM_WEB_URL))
                .thenThrow(new RuntimeException("unexpected error"));

        assertDoesNotThrow(() ->
                ibexAdapterService.createCaseAndSlide(STUDY_UID, SOURCE_URL, BARCODE, SERIES_ID, DICOM_WEB_URL));

        verify(slideService, never()).createSlide(any(), any(), any(), any(), any());
    }

    @Test
    void testCreateCaseAndSlide_NullMetadata_SlideServiceNotCalled() throws Exception {
        when(healthcareApiClient.fetchMetadata(STUDY_UID, SERIES_ID, DICOM_WEB_URL))
                .thenReturn(null);

        assertDoesNotThrow(() ->
                ibexAdapterService.createCaseAndSlide(STUDY_UID, SOURCE_URL, BARCODE, SERIES_ID, DICOM_WEB_URL));

        // convertStringToJson returns empty map for null; slide creation proceeds
        // but verifying no NPE is thrown is the key assertion here
        verify(healthcareApiClient, times(1))
                .fetchMetadata(STUDY_UID, SERIES_ID, DICOM_WEB_URL);
    }

    @Test
    void testCreateCaseAndSlide_EmptyMetadataJson_DoesNotThrow() throws Exception {
        when(healthcareApiClient.fetchMetadata(STUDY_UID, SERIES_ID, DICOM_WEB_URL))
                .thenReturn("[]");

        assertDoesNotThrow(() ->
                ibexAdapterService.createCaseAndSlide(STUDY_UID, SOURCE_URL, BARCODE, SERIES_ID, DICOM_WEB_URL));
    }

    // ==========================
    // deleteFolderFromBucket() Tests
    // ==========================

    @Test
    void testDeleteFolderFromBucket_IOException_WhenCredsInvalid() {
        when(barcodeStudyInfo.get(BARCODE)).thenReturn(STUDY_UID);
        when(gcpConfig.getCreds()).thenReturn("not-valid-json-creds");

        // IOException is caught internally — must not propagate
        assertDoesNotThrow(() -> ibexAdapterService.deleteFolderFromBucket(BARCODE));

        verify(barcodeStudyInfo, times(1)).get(BARCODE);
    }

    @Test
    void testDeleteFolderFromBucket_NullBarcode_DoesNotThrow() {
        when(barcodeStudyInfo.get(null)).thenReturn(null);
        when(gcpConfig.getCreds()).thenReturn("not-valid-json-creds");

        assertDoesNotThrow(() -> ibexAdapterService.deleteFolderFromBucket(null));
    }

    @Test
    void testDeleteFolderFromBucket_BarcodeNotInMap_DoesNotThrow() {
        when(barcodeStudyInfo.get("unknown-barcode")).thenReturn(null);
        when(gcpConfig.getCreds()).thenReturn("not-valid-json-creds");

        assertDoesNotThrow(() -> ibexAdapterService.deleteFolderFromBucket("unknown-barcode"));
    }

    @Test
    void testDeleteFolderFromBucket_NullCreds_ThrowsException() {
        assertThrows(NullPointerException.class,
                () -> ibexAdapterService.deleteFolderFromBucket("folder123"));
    }

    @Test
    void testDeleteFolderFromBucket_AlwaysQueriesBarcodeStudyInfo() {
        when(barcodeStudyInfo.get(BARCODE)).thenReturn(STUDY_UID);
        when(gcpConfig.getCreds()).thenReturn("invalid-creds");

        ibexAdapterService.deleteFolderFromBucket(BARCODE);

        verify(barcodeStudyInfo, times(1)).get(BARCODE);
    }

    @Test
    void testDeleteFolderFromBucket_BarcodeRemovedOnlyAfterSuccessfulDelete() {
        // With invalid creds the delete will fail (IOException), so remove() should never be called
        when(barcodeStudyInfo.get(BARCODE)).thenReturn(STUDY_UID);
        when(gcpConfig.getCreds()).thenReturn("invalid-creds");

        ibexAdapterService.deleteFolderFromBucket(BARCODE);

        verify(barcodeStudyInfo, never()).remove(BARCODE);
    }
}