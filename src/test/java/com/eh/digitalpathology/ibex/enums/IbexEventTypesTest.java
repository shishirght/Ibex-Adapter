package com.eh.digitalpathology.ibex.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IbexEventTypesTest {

    @Test
    void shouldReturnCorrectEventTypeValue() {
        assertEquals("slide-download-completed",
                IbexEventTypes.SLIDE_DOWNLOAD_COMPLETED.getEventType());

        assertEquals("classification-finished",
                IbexEventTypes.CLASSIFICATION_FINISHED.getEventType());
    }

    @Test
    void shouldReturnEnumFromValidEvent() {

        IbexEventTypes event =
                IbexEventTypes.fromEvent("slide-download-completed");

        assertEquals(IbexEventTypes.SLIDE_DOWNLOAD_COMPLETED, event);
    }

    @Test
    void shouldMatchCaseInsensitiveEvent() {

        IbexEventTypes event =
                IbexEventTypes.fromEvent("SLIDE-DOWNLOAD-COMPLETED");

        assertEquals(IbexEventTypes.SLIDE_DOWNLOAD_COMPLETED, event);
    }

    @Test
    void shouldReturnNullForInvalidEvent() {

        IbexEventTypes event =
                IbexEventTypes.fromEvent("unknown-event");

        assertNull(event);
    }

    @Test
    void shouldReturnAllEnumValuesFromFromEvent() {

        for (IbexEventTypes type : IbexEventTypes.values()) {
            IbexEventTypes result =
                    IbexEventTypes.fromEvent(type.getEventType());

            assertEquals(type, result);
        }
    }
}