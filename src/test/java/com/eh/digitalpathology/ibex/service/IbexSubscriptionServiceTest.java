package com.eh.digitalpathology.ibex.service;

import com.eh.digitalpathology.ibex.client.IbexApiClient;
import com.eh.digitalpathology.ibex.exceptions.IbexApiException;
import com.eh.digitalpathology.ibex.model.Subscription;
import com.eh.digitalpathology.ibex.util.AppUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IbexSubscriptionServiceTest {

    @Mock
    private IbexApiClient ibexApiClient;

    @InjectMocks
    private IbexSubscriptionService ibexSubscriptionService;

    private Subscription subscription;

    @BeforeEach
    void setUp() {
        subscription = new Subscription();
    }

    @Test
    void updateSubscription_shouldCallApi_whenJsonIsNotNull() throws Exception {
        String jsonBody = "{ \"test\": \"value\" }";

        try (MockedStatic<AppUtil> appUtil = mockStatic(AppUtil.class)) {
            // Arrange
            appUtil.when(() -> AppUtil.convertObjectToString(subscription))
                    .thenReturn(jsonBody);

            // Act
            ibexSubscriptionService.updateSubscription(subscription);

            // Assert
            verify(ibexApiClient).putSubscription(jsonBody);
        }
    }

    @Test
    void updateSubscription_shouldNotCallApi_whenJsonIsNull() throws IbexApiException {
        try (MockedStatic<AppUtil> appUtil = mockStatic(AppUtil.class)) {
            // Arrange
            appUtil.when(() -> AppUtil.convertObjectToString(subscription))
                    .thenReturn(null);

            // Act
            ibexSubscriptionService.updateSubscription(subscription);

            // Assert
            verify(ibexApiClient, never()).putSubscription(anyString());
        }
    }

    @Test
    void updateSubscription_shouldHandleException_whenApiThrowsError() throws Exception {
        String jsonBody = "{ \"test\": \"value\" }";

        try (MockedStatic<AppUtil> appUtil = mockStatic(AppUtil.class)) {
            // Arrange
            appUtil.when(() -> AppUtil.convertObjectToString(subscription))
                    .thenReturn(jsonBody);

            doThrow(new IbexApiException("API error"))
                    .when(ibexApiClient).putSubscription(jsonBody);

            // Act (no exception should escape)
            ibexSubscriptionService.updateSubscription(subscription);

            // Assert
            verify(ibexApiClient).putSubscription(jsonBody);
        }
    }
}
