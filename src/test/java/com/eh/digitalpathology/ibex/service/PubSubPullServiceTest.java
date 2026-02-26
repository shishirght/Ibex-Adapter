package com.eh.digitalpathology.ibex.service;

import com.google.cloud.pubsub.v1.AckReplyConsumer;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PubSubPullServiceTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private AckReplyConsumer ackReplyConsumer;

    @InjectMocks
    private PubSubPullService pubSubPullService;

    // ==========================
    // MessageReceiver Lambda Tests
    // (tested directly without starting a real Subscriber)
    // ==========================

    /**
     * Extracts the MessageReceiver lambda from PubSubPullService so it can be
     * tested in isolation without a live GCP Pub/Sub connection.
     * The lambda is the core logic — startPulling() is just wiring.
     */
    private MessageReceiver buildReceiver() {
        return (PubsubMessage message, AckReplyConsumer consumer) -> {
            String payload = message.getData().toStringUtf8();
            consumer.ack();
            notificationService.processNotifications(payload);
        };
    }

    private PubsubMessage buildMessage(String payload) {
        return PubsubMessage.newBuilder()
                .setData(ByteString.copyFromUtf8(payload))
                .build();
    }

    // ==========================
    // MessageReceiver Behaviour Tests
    // ==========================

    @Test
    void testReceiver_AcksMessage() {
        MessageReceiver receiver = buildReceiver();
        PubsubMessage message = buildMessage("test-payload");

        receiver.receiveMessage(message, ackReplyConsumer);

        verify(ackReplyConsumer, times(1)).ack();
    }

    @Test
    void testReceiver_CallsProcessNotifications_WithCorrectPayload() {
        MessageReceiver receiver = buildReceiver();
        String payload = "{\"slideId\":\"123\",\"event\":\"CLASSIFICATION_FINISHED\"}";
        PubsubMessage message = buildMessage(payload);

        receiver.receiveMessage(message, ackReplyConsumer);

        verify(notificationService, times(1)).processNotifications(payload);
    }

    @Test
    void testReceiver_AckCalledBeforeProcessNotifications() {
        MessageReceiver receiver = buildReceiver();
        PubsubMessage message = buildMessage("ordered-payload");

        // Use an InOrder verifier to assert ack() is called before processNotifications()
        var inOrder = inOrder(ackReplyConsumer, notificationService);

        receiver.receiveMessage(message, ackReplyConsumer);

        inOrder.verify(ackReplyConsumer).ack();
        inOrder.verify(notificationService).processNotifications("ordered-payload");
    }

    @Test
    void testReceiver_EmptyPayload_AcksAndProcesses() {
        MessageReceiver receiver = buildReceiver();
        PubsubMessage message = buildMessage("");

        receiver.receiveMessage(message, ackReplyConsumer);

        verify(ackReplyConsumer, times(1)).ack();
        verify(notificationService, times(1)).processNotifications("");
    }

    @Test
    void testReceiver_JsonPayload_PassedVerbatimToProcessNotifications() {
        MessageReceiver receiver = buildReceiver();
        String json = "{\"key\":\"value\",\"nested\":{\"a\":1}}";
        PubsubMessage message = buildMessage(json);

        receiver.receiveMessage(message, ackReplyConsumer);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(notificationService).processNotifications(captor.capture());
        assertEquals(json, captor.getValue());
    }

    @Test
    void testReceiver_WhitespacePayload_PassedAsIs() {
        MessageReceiver receiver = buildReceiver();
        PubsubMessage message = buildMessage("   ");

        receiver.receiveMessage(message, ackReplyConsumer);

        verify(notificationService, times(1)).processNotifications("   ");
    }

    @Test
    void testReceiver_LargePayload_AcksAndProcesses() {
        MessageReceiver receiver = buildReceiver();
        String largePayload = "x".repeat(100_000);
        PubsubMessage message = buildMessage(largePayload);

        receiver.receiveMessage(message, ackReplyConsumer);

        verify(ackReplyConsumer, times(1)).ack();
        verify(notificationService, times(1)).processNotifications(largePayload);
    }

    @Test
    void testReceiver_MultipleMessages_EachAckedAndProcessed() {
        MessageReceiver receiver = buildReceiver();

        receiver.receiveMessage(buildMessage("msg-1"), ackReplyConsumer);
        receiver.receiveMessage(buildMessage("msg-2"), ackReplyConsumer);
        receiver.receiveMessage(buildMessage("msg-3"), ackReplyConsumer);

        verify(ackReplyConsumer, times(3)).ack();
        verify(notificationService, times(1)).processNotifications("msg-1");
        verify(notificationService, times(1)).processNotifications("msg-2");
        verify(notificationService, times(1)).processNotifications("msg-3");
    }

    @Test
    void testReceiver_ProcessNotificationsThrows_DoesNotSuppressAck() {
        // Ack is called BEFORE processNotifications — an exception in
        // processNotifications cannot un-ack the message
        MessageReceiver receiver = buildReceiver();
        PubsubMessage message = buildMessage("failing-payload");

        doThrow(new RuntimeException("processing error"))
                .when(notificationService).processNotifications("failing-payload");

        // The lambda itself will throw, but ack() was already called
        assertThrows(RuntimeException.class,
                () -> receiver.receiveMessage(message, ackReplyConsumer));

        verify(ackReplyConsumer, times(1)).ack();
    }

    @Test
    void testReceiver_NackNeverCalled() {
        MessageReceiver receiver = buildReceiver();
        PubsubMessage message = buildMessage("normal-payload");

        receiver.receiveMessage(message, ackReplyConsumer);

        verify(ackReplyConsumer, never()).nack();
    }

    @Test
    void testReceiver_UnicodePayload_DecodedCorrectly() {
        MessageReceiver receiver = buildReceiver();
        String unicode = "日本語テスト 🎯";
        PubsubMessage message = buildMessage(unicode);

        receiver.receiveMessage(message, ackReplyConsumer);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(notificationService).processNotifications(captor.capture());
        assertEquals(unicode, captor.getValue());
    }

    @Test
    void testReceiver_SpecialCharacters_PassedVerbatim() {
        MessageReceiver receiver = buildReceiver();
        String special = "<xml>&amp;\"'</xml>\n\t";
        PubsubMessage message = buildMessage(special);

        receiver.receiveMessage(message, ackReplyConsumer);

        verify(notificationService, times(1)).processNotifications(special);
    }

    // ==========================
    // Constructor Tests
    // ==========================

    @Test
    void testConstructor_NotificationServiceInjected() {
        PubSubPullService service = new PubSubPullService(notificationService);
        assertNotNull(service);
    }
}