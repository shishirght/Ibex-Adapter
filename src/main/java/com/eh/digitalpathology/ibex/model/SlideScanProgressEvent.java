package com.eh.digitalpathology.ibex.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SlideScanProgressEvent(String slideBarcode, String scanStatus, String errorMessage,
                                     String sourceService) {

    private static final String DEFAULT_SOURCE = "eh-ibex-adapter";

    public SlideScanProgressEvent(String slideBarcode, String scanStatus, String errorMessage) {
        this(slideBarcode, scanStatus, errorMessage, DEFAULT_SOURCE);
    }

    public SlideScanProgressEvent(String slideBarcode, String scanStatus) {
        this(slideBarcode, scanStatus, null, DEFAULT_SOURCE);
    }
}