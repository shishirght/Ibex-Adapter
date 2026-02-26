package com.eh.digitalpathology.ibex.service;

import com.eh.digitalpathology.ibex.exceptions.SubscriptionUpdateException;
import com.eh.digitalpathology.ibex.model.Subscription;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IbexWebhookSubscriptionTest {

    @Mock
    private IbexSubscriptionService subscriptionService;

    @InjectMocks
    private IbexWebhookSubscription webhookSubscription;

    @BeforeEach
    void setUp() {
        // Inject @Value field manually
        ReflectionTestUtils.setField(
                webhookSubscription,
                "callbackUrl",
                "http://localhost:8080"
        );
    }

    @Test
    void init_shouldCreateAndUpdateSubscriptionSuccessfully() {
        // Act
        webhookSubscription.init();

        // Assert
        ArgumentCaptor<Subscription> captor =
                ArgumentCaptor.forClass(Subscription.class);

        verify(subscriptionService).updateSubscription(captor.capture());

        Subscription sub = captor.getValue();
        assertTrue(sub.isActive());
        assertEquals(
                "http://localhost:8080/webhook/digitalpathology/system",
                sub.getCallbackUrl()
        );
    }

    @Test
    @Disabled
    void init_shouldThrowSubscriptionUpdateException_whenCallbackUrlFails() {
        // Force invalid callbackUrl
        ReflectionTestUtils.setField(
                webhookSubscription,
                "callbackUrl",
                null
        );

        // Act + Assert
        SubscriptionUpdateException ex = assertThrows(
                SubscriptionUpdateException.class,
                () -> webhookSubscription.init()
        );

        assertTrue(ex.getMessage().contains("Failed to update subscription"));
        verify(subscriptionService, never()).updateSubscription(any());
    }
}
