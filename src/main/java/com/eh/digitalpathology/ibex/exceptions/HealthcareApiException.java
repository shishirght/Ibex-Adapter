/**
 * Custom exception class to handle errors related to healthcare API interactions.
 * This exception is thrown when there are issues encountered while communicating
 * with DICOM APIs, encapsulating error messages and underlying causes.
 * Author: Preeti Ankam
 * Date: October 30, 2024
 * */

package com.eh.digitalpathology.ibex.exceptions;

public class HealthcareApiException extends Exception {
    public HealthcareApiException(String message) {
        super(message);
    }

    public HealthcareApiException(String message, Throwable cause) {
        super(message, cause);
    }
}