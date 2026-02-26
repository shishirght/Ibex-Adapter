package com.eh.digitalpathology.ibex.model;

public record HttpResponseWrapper(int statusCode, String body) {
}