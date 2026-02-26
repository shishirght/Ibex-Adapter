/**
 * Subscription is used to hold the details of subscription.
 * Author: Preeti Ankam
 * Date: November 30, 2024
 */

package com.eh.digitalpathology.ibex.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Subscription {

    @JsonProperty(required = true)
    boolean active;

    @JsonProperty(value = "callback_url", required = true)
    String callbackUrl;

    @JsonProperty("api_key")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    String apiKey;

    @JsonProperty("data_key")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    String dataKey;

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getDataKey() {
        return dataKey;
    }

    public void setDataKey(String dataKey) {
        this.dataKey = dataKey;
    }
}
