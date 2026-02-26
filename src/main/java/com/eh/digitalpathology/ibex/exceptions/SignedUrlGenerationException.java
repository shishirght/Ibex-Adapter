/**
 * SignedUrlGenerationException is thrown when there are issues encountered while generating signed url.
 * Author: Preeti Ankam
 * Date: December 01, 2024
 */

package com.eh.digitalpathology.ibex.exceptions;

public class SignedUrlGenerationException extends RuntimeException {
    public SignedUrlGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}