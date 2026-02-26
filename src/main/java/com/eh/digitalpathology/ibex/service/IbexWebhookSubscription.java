/**
 * SubscriptionInitializer is used to update the subscription , setting callback URL for IBEX.
 * Author: Preeti Ankam
 * Date:November 30, 2024
 */

package com.eh.digitalpathology.ibex.service;

import com.eh.digitalpathology.ibex.exceptions.SubscriptionUpdateException;
import com.eh.digitalpathology.ibex.model.Subscription;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

@RefreshScope
@Service
public class IbexWebhookSubscription {
    private static final Logger logger = LoggerFactory.getLogger(IbexWebhookSubscription.class);

    private final IbexSubscriptionService subscriptionService;

    @Value("${ibex.callback-url}")
    private String callbackUrl;

    public IbexWebhookSubscription(IbexSubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @PostConstruct
    public void init() {
        logger.info("init :: Updating Subscription...");
        Subscription subReq = new Subscription();
        subReq.setActive(true);
        try {
            subReq.setCallbackUrl(callbackUrl + "/webhook/digitalpathology/system");
        } catch (Exception e) {
            throw new SubscriptionUpdateException("Failed to update subscription callback URL", e);
        }
        subscriptionService.updateSubscription(subReq);
    }
}
