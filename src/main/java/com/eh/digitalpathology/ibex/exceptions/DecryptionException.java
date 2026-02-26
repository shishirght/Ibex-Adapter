/**
 * DecryptionException is used to handle errors while decrypting the key used for signed url creation.
 * This exception is thrown when there are issues encountered while decrypting the key
 * Author: Preeti Ankam
 * Date: December 01, 2024
 */

package com.eh.digitalpathology.ibex.exceptions;

public class DecryptionException extends Exception {
    public DecryptionException(String message, Throwable cause) {
        super(message, cause);
    }
}