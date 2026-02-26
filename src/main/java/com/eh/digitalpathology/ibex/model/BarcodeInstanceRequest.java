package com.eh.digitalpathology.ibex.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BarcodeInstanceRequest(String barcode, String studyId, String seriesId, String errorMessage) {
}

