/**
 * IbexSubscriptionService contains the method to update the subscription.
 * Author: Preeti Ankam
 * Date: November 30, 2024
 */

package com.eh.digitalpathology.ibex.service;

import com.eh.digitalpathology.ibex.client.IbexApiClient;
import com.eh.digitalpathology.ibex.exceptions.IbexApiException;
import com.eh.digitalpathology.ibex.model.Subscription;
import com.eh.digitalpathology.ibex.util.AppUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class IbexSubscriptionService {

    private static final Logger logger = LoggerFactory.getLogger(IbexSubscriptionService.class);
    private final IbexApiClient ibexApiClient;

    public IbexSubscriptionService(IbexApiClient ibexApiClient) {
        this.ibexApiClient = ibexApiClient;
    }

    public void updateSubscription(Subscription subRequest) {
        String jsonRequestBody = AppUtil.convertObjectToString(subRequest);
        if (jsonRequestBody != null) {
            try {
                ibexApiClient.putSubscription(jsonRequestBody);
            } catch (IbexApiException e) {
                logger.error("updateSubscription :: exception occurred :: {}", e.getMessage());
            }
        }
    }
}
