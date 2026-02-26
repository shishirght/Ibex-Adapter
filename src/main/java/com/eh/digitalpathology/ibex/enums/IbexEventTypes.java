/**
* IbexEventTypes is an enum of ibex suuported events.
 * Author: Preeti Ankam
 * Date:December 23, 2024
 */

package com.eh.digitalpathology.ibex.enums;

public enum IbexEventTypes {
    SLIDE_DOWNLOAD_COMPLETED("slide-download-completed"),
    SLIDE_DOWNLOAD_FAILED("slide-download-failed"),
    INVALID_SLIDE("invalid-slide"),
    CLASSIFICATION_FINISHED("classification-finished"),
    CLASSIFICATION_FAILED("classification-failed"),
    REPORT_SUBMITTED("report-submitted");

    private final String eventType;

    IbexEventTypes(String eventType) {
        this.eventType = eventType;
    }

    public String getEventType() {
        return eventType;
    }

    public static IbexEventTypes fromEvent(String type) {
        for (IbexEventTypes eventType : IbexEventTypes.values()) {
            if (eventType.getEventType().equalsIgnoreCase(type)) {
                return eventType;
            }
        }
        return null;
    }
}
