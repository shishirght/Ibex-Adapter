/**

 * Custom checked exception to handle errors related to the IBEX API.
 * This exception is thrown when an error occurs while interacting with the IBEX API,
 * including network issues, invalid responses, or any other API-related errors.
 * Author: Preeti Ankam
 *  Date: October 30, 2024

 */

package com.eh.digitalpathology.ibex.exceptions;

public class IbexApiException extends Exception {

    public IbexApiException(String message) {
        super(message);
    }


    public IbexApiException(String message, Throwable cause) {
        super(message, cause);
    }

    public IbexApiException(Throwable cause) {
        super(cause);
    }

}

 