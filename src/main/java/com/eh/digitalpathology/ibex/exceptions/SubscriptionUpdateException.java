/**
 * SubscriptionUpdateException is thrown when there are issues while updating subscription.
 * Author: Preeti Ankam
 * Date: November 25, 2024
 */

package com.eh.digitalpathology.ibex.exceptions;

public class SubscriptionUpdateException extends RuntimeException {
    public SubscriptionUpdateException(String message, Throwable cause) {
        super(message, cause);
    }
}

