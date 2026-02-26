package com.eh.digitalpathology.ibex.model;

public record ApiResponse<T>(String status, T content, String errorCode, String errorMessage) {}


