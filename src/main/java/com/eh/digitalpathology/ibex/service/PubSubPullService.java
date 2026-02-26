package com.eh.digitalpathology.ibex.service;

import com.google.cloud.pubsub.v1.AckReplyConsumer;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.PubsubMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
public class PubSubPullService {
    private static final String PROJECT_ID = "prj-d-path-integration-cs1h";
    private static final String SUBSCRIPTION_ID = "ibex-sub";
    private static final Logger logger = LoggerFactory.getLogger( PubSubPullService.class );
    private final NotificationService notifiationService;

    @Autowired
    public PubSubPullService ( NotificationService notifiationService ) {
        this.notifiationService = notifiationService;
    }


    @EventListener( ApplicationReadyEvent.class )
    public void startPulling ( ) {
        ProjectSubscriptionName subscriptionName = ProjectSubscriptionName.of( PROJECT_ID, SUBSCRIPTION_ID );
        Subscriber subscriber = Subscriber.newBuilder( subscriptionName, ( PubsubMessage message, AckReplyConsumer consumer ) -> {
            String payload = message.getData( ).toStringUtf8( );
            logger.info( "startPulling :: Received pull message:{} ", payload );
            consumer.ack( );
            notifiationService.processNotifications( payload );
        } ).build( );

        subscriber.startAsync( ).awaitRunning( );
        logger.info( "startPulling :: Started pulling messages..." );
    }

}
