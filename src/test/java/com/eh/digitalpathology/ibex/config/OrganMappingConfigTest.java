package com.eh.digitalpathology.ibex.config;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class OrganMappingConfigTest {

    @Test
    void testConstructor_ShouldSetAllowedList() {
        List<String> allowedList = Arrays.asList("LUNG", "LIVER");

        OrganMappingConfig config = new OrganMappingConfig(allowedList);

        assertEquals(allowedList, config.getAllowed());
    }

    @Test
    void testSetAndGetMapping() {
        OrganMappingConfig config =
                new OrganMappingConfig(Arrays.asList("TEST"));

        Map<String, String> mapping = new HashMap<>();
        mapping.put("LUNG", "Pulmonary");
        mapping.put("LIVER", "Hepatic");

        config.setMapping(mapping);

        assertNotNull(config.getMapping());
        assertEquals("Pulmonary", config.getMapping().get("LUNG"));
        assertEquals("Hepatic", config.getMapping().get("LIVER"));
    }

    @Test
    void testSetAllowed_ShouldOverrideConstructorValue() {
        OrganMappingConfig config =
                new OrganMappingConfig(Arrays.asList("OLD"));

        List<String> newAllowed = Arrays.asList("BRAIN", "HEART");
        config.setAllowed(newAllowed);

        assertEquals(newAllowed, config.getAllowed());
    }

    @Test
    void testObjectCreation() {
        OrganMappingConfig config =
                new OrganMappingConfig(Arrays.asList("ANY"));

        assertNotNull(config);
    }
}