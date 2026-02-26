package com.eh.digitalpathology.ibex.constants;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.*;

class AppConstantsTest {

    @Test
    void constants_should_have_expected_values() {
        assertEquals("00080050", AppConstants.ACCESSION_NO_TAG_KEY);
        assertEquals("0020000E", AppConstants.SERIES_INSTANCE_UID);

        assertEquals("X-API-KEY", AppConstants.API_KEY_HEADER);
        assertEquals("Value", AppConstants.VALUE);

        assertEquals("00400560", AppConstants.SPECIMEN_DESCRIPTION_SEQUENCE);
        assertEquals("00080104", AppConstants.CODE_MEANING);
        assertEquals("00080100", AppConstants.CODE_VALUE);

        assertEquals("00082228", AppConstants.PRIMARY_ANATOMIC_STRUCTURE_SEQ);

        assertEquals("400 Bad request", AppConstants.BAD_REQUEST);
        assertEquals(
                "401 Unauthorized: Check api key or authorization details",
                AppConstants.UNAUTHORIZED_ERROR
        );
        assertEquals("500 Internal Server Error", AppConstants.INTERNAL_SERVER_ERROR);
        assertEquals("status code:: ", AppConstants.STATUS_CODE);

        assertEquals("IBEX_SLIDE_CREATION_ERROR", AppConstants.IBEX_SLIDE_CREATION_ERROR);
    }

    @Test
    @Disabled
    void constructor_should_be_private_and_not_instantiable() throws Exception {
        Constructor<AppConstants> constructor =
                AppConstants.class.getDeclaredConstructor();

        assertTrue(Modifier.isPrivate(constructor.getModifiers()));

        constructor.setAccessible(true);

        assertThrows(Exception.class, constructor::newInstance);
    }
}
