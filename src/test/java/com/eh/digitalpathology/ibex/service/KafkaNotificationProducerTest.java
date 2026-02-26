package com.eh.digitalpathology.ibex.service;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KafkaNotificationProducerTest {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @InjectMocks
    private KafkaNotificationProducer kafkaNotificationProducer;

    private static final String TOPIC = "test-topic";
    private static final String KEY = "IBEX_EVENT";
    private static final String DATA = "{\"slideId\":\"123\"}";

    // ==========================
    // sendNotification() Tests
    // ==========================

    @Test
    void testSendNotification_Success() {
        kafkaNotificationProducer.sendNotification(TOPIC, KEY, DATA);

        verify(kafkaTemplate, times(1)).send(TOPIC, KEY, DATA);
    }

    @Test
    void testSendNotification_DoesNotThrow_OnSuccess() {
        assertDoesNotThrow(() ->
                kafkaNotificationProducer.sendNotification(TOPIC, KEY, DATA));
    }

    @Test
    void testSendNotification_ExceptionFromKafkaTemplate_IsSwallowed() {
        doThrow(new RuntimeException("Kafka broker unavailable"))
                .when(kafkaTemplate).send(TOPIC, KEY, DATA);

        assertDoesNotThrow(() ->
                kafkaNotificationProducer.sendNotification(TOPIC, KEY, DATA));

        verify(kafkaTemplate, times(1)).send(TOPIC, KEY, DATA);
    }

    @Test
    void testSendNotification_KafkaCalledWithCorrectArguments() {
        kafkaNotificationProducer.sendNotification(TOPIC, KEY, DATA);

        verify(kafkaTemplate).send(eq(TOPIC), eq(KEY), eq(DATA));
    }

    @Test
    void testSendNotification_NullTopic_IsSwallowed() {
        doThrow(new RuntimeException("null topic"))
                .when(kafkaTemplate).send(null, KEY, DATA);

        assertDoesNotThrow(() ->
                kafkaNotificationProducer.sendNotification(null, KEY, DATA));
    }

    @Test
    void testSendNotification_NullKey_IsSwallowed() {
        doThrow(new RuntimeException("null key"))
                .when(kafkaTemplate).send(TOPIC, null, DATA);

        assertDoesNotThrow(() ->
                kafkaNotificationProducer.sendNotification(TOPIC, null, DATA));
    }

    @Test
    void testSendNotification_NullData_IsSwallowed() {
        doThrow(new RuntimeException("null data"))
                .when(kafkaTemplate).send(TOPIC, KEY, null);

        assertDoesNotThrow(() ->
                kafkaNotificationProducer.sendNotification(TOPIC, KEY, null));
    }

    @Test
    void testSendNotification_AllNullArguments_IsSwallowed() {
        doThrow(new RuntimeException("all nulls"))
                .when(kafkaTemplate).send(null, null, null);

        assertDoesNotThrow(() ->
                kafkaNotificationProducer.sendNotification(null, null, null));
    }

    @Test
    void testSendNotification_EmptyTopic_DoesNotThrow() {
        assertDoesNotThrow(() ->
                kafkaNotificationProducer.sendNotification("", KEY, DATA));

        verify(kafkaTemplate, times(1)).send("", KEY, DATA);
    }

    @Test
    void testSendNotification_EmptyKey_DoesNotThrow() {
        assertDoesNotThrow(() ->
                kafkaNotificationProducer.sendNotification(TOPIC, "", DATA));

        verify(kafkaTemplate, times(1)).send(TOPIC, "", DATA);
    }

    @Test
    void testSendNotification_EmptyData_DoesNotThrow() {
        assertDoesNotThrow(() ->
                kafkaNotificationProducer.sendNotification(TOPIC, KEY, ""));

        verify(kafkaTemplate, times(1)).send(TOPIC, KEY, "");
    }

    @Test
    void testSendNotification_LargePayload_DoesNotThrow() {
        String largeData = "x".repeat(100_000);

        assertDoesNotThrow(() ->
                kafkaNotificationProducer.sendNotification(TOPIC, KEY, largeData));

        verify(kafkaTemplate, times(1)).send(TOPIC, KEY, largeData);
    }

    @Test
    void testSendNotification_CalledMultipleTimes_EachSentToKafka() {
        kafkaNotificationProducer.sendNotification(TOPIC, "KEY_ONE", "data-1");
        kafkaNotificationProducer.sendNotification(TOPIC, "KEY_TWO", "data-2");
        kafkaNotificationProducer.sendNotification(TOPIC, "KEY_THREE", "data-3");

        verify(kafkaTemplate, times(1)).send(TOPIC, "KEY_ONE", "data-1");
        verify(kafkaTemplate, times(1)).send(TOPIC, "KEY_TWO", "data-2");
        verify(kafkaTemplate, times(1)).send(TOPIC, "KEY_THREE", "data-3");
    }

    @Test
    void testSendNotification_KafkaTemplateCalledExactlyOnce_PerInvocation() {
        kafkaNotificationProducer.sendNotification(TOPIC, KEY, DATA);

        verify(kafkaTemplate, times(1)).send(any(), any(), any());
    }

    @Test
    @Disabled
    void testSendNotification_ExceptionDoesNotPreventFutureMessages() {
        doThrow(new RuntimeException("transient error"))
                .doNothing()
                .when(kafkaTemplate).send(TOPIC, KEY, DATA);

        // First call throws internally but is swallowed
        kafkaNotificationProducer.sendNotification(TOPIC, KEY, DATA);
        // Second call succeeds
        kafkaNotificationProducer.sendNotification(TOPIC, KEY, DATA);

        verify(kafkaTemplate, times(2)).send(TOPIC, KEY, DATA);
    }
}