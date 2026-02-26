package com.eh.digitalpathology.ibex.util;

import com.eh.digitalpathology.ibex.constants.AppConstants;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class AppUtilTest {

    // ==========================
    // getTagValue() Tests
    // ==========================

    @Test
    void testGetTagValue_ReturnsValue_WhenTagExists() {
        Map<String, Object> metadata = buildTagMap("0008103E", "SlideDescription");
        String result = AppUtil.getTagValue(metadata, "0008103E");
        assertEquals("SlideDescription", result);
    }

    @Test
    void testGetTagValue_ReturnsNull_WhenTagNotFound() {
        Map<String, Object> metadata = new HashMap<>();
        String result = AppUtil.getTagValue(metadata, "0008103E");
        assertNull(result);
    }

    @Test
    void testGetTagValue_ReturnsNull_WhenMetadataEmpty() {
        String result = AppUtil.getTagValue(Collections.emptyMap(), "0008103E");
        assertNull(result);
    }

    @Test
    void testGetTagValue_FindsTagInNestedMap() {
        Map<String, Object> inner = buildTagMap("00102160", "EthnicGroup");
        Map<String, Object> outer = new HashMap<>();
        outer.put("nested", inner);

        String result = AppUtil.getTagValue(outer, "00102160");
        assertEquals("EthnicGroup", result);
    }

    @Test
    void testGetTagValue_FindsTagInNestedList() {
        Map<String, Object> tagMap = buildTagMap("00400A124", "UID-value");
        List<Object> list = new ArrayList<>();
        list.add(tagMap);

        Map<String, Object> outer = new HashMap<>();
        outer.put("items", list);

        String result = AppUtil.getTagValue(outer, "00400A124");
        assertEquals("UID-value", result);
    }

    @Test
    void testGetTagValue_ReturnsNull_WhenValueMapMissing() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("0008103E", "not-a-map");

        String result = AppUtil.getTagValue(metadata, "0008103E");
        assertNull(result);
    }

    @Test
    void testGetTagValue_ReturnsNull_WhenValueListEmpty() {
        Map<String, Object> valueMap = new HashMap<>();
        valueMap.put(AppConstants.VALUE, Collections.emptyList());

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("0008103E", valueMap);

        String result = AppUtil.getTagValue(metadata, "0008103E");
        assertNull(result);
    }

    // ==========================
    // getStainValue() Tests
    // ==========================

    @Test
    void testGetStainValue_ReturnsStain_WhenUsingSubstancePresent() {
        Map<String, Object> metadataMap = buildStainMetadata("Hematoxylin");
        Optional<String> result = AppUtil.getStainValue(metadataMap);
        assertTrue(result.isPresent());
        assertEquals("Hematoxylin", result.get());
    }

    @Test
    void testGetStainValue_ReturnsEmpty_WhenNo0040A043Key() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("someOtherKey", "someValue");

        Optional<String> result = AppUtil.getStainValue(metadata);
        assertFalse(result.isPresent());
    }

    @Test
    void testGetStainValue_ReturnsEmpty_When0040A043ValueNotMap() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("0040A043", "not-a-map");

        Optional<String> result = AppUtil.getStainValue(metadata);
        assertFalse(result.isPresent());
    }

    @Test
    void testGetStainValue_ReturnsEmpty_WhenConceptMeaningNotUsingSubstance() {
        Map<String, Object> metadataMap = buildStainMetadataWithMeaning("SomethingElse", "Eosin");
        Optional<String> result = AppUtil.getStainValue(metadataMap);
        assertFalse(result.isPresent());
    }

    @Test
    void testGetStainValue_ReturnsEmpty_WhenEmptyMap() {
        Optional<String> result = AppUtil.getStainValue(Collections.emptyMap());
        assertFalse(result.isPresent());
    }

    @Test
    void testGetStainValue_FindsStainInNestedMap() {
        Map<String, Object> stainMap = buildStainMetadata("Eosin");
        Map<String, Object> outer = new HashMap<>();
        outer.put("level1", stainMap);

        Optional<String> result = AppUtil.getStainValue(outer);
        assertTrue(result.isPresent());
        assertEquals("Eosin", result.get());
    }

    // ==========================
    // convertStringToJson() Tests
    // ==========================

    @Test
    void testConvertStringToJson_ReturnsMap_WhenValidJsonObject() {
        String json = "{\"key\":\"value\"}";
        Map<String, Object> result = AppUtil.convertStringToJson(json);
        assertFalse(result.isEmpty());
        assertEquals("value", result.get("key"));
    }

    @Test
    void testConvertStringToJson_ReturnsFirstElement_WhenValidJsonArray() {
        String json = "[{\"key\":\"first\"},{\"key\":\"second\"}]";
        Map<String, Object> result = AppUtil.convertStringToJson(json);
        assertFalse(result.isEmpty());
        assertEquals("first", result.get("key"));
    }

    @Test
    void testConvertStringToJson_ReturnsEmptyMap_WhenEmptyArray() {
        String json = "[]";
        Map<String, Object> result = AppUtil.convertStringToJson(json);
        assertTrue(result.isEmpty());
    }

    @Test
    void testConvertStringToJson_ReturnsEmptyMap_WhenInvalidJson() {
        String json = "not-valid-json";
        Map<String, Object> result = AppUtil.convertStringToJson(json);
        assertTrue(result.isEmpty());
    }

    @Test
    void testConvertStringToJson_ReturnsEmptyMap_WhenPrimitiveJson() {
        String json = "\"justAString\"";
        Map<String, Object> result = AppUtil.convertStringToJson(json);
        assertTrue(result.isEmpty());
    }

    // ==========================
    // convertObjectToString() Tests
    // ==========================

    @Test
    void testConvertObjectToString_ReturnsJsonString_WhenValidObject() {
        Map<String, String> obj = Map.of("key", "value");
        String result = AppUtil.convertObjectToString(obj);
        assertNotNull(result);
        assertTrue(result.contains("key"));
        assertTrue(result.contains("value"));
    }

    @Test
    void testConvertObjectToString_ReturnsNull_WhenObjectNotSerializable() {
        // ObjectMapper cannot serialize objects with circular references, but
        // a simple unserializable stub is enough to trigger the catch block.
        Object unserializable = new Object() {
            public final Object self = this; // circular reference causes serialization failure
        };
        String result = AppUtil.convertObjectToString(unserializable);
        assertNull(result);
    }

    @Test
    void testConvertObjectToString_ReturnsPrettyPrintedJson() {
        Map<String, String> obj = Map.of("hello", "world");
        String result = AppUtil.convertObjectToString(obj);
        assertNotNull(result);
        assertTrue(result.contains("\n")); // pretty-printed output contains newlines
    }

    // ==========================
    // validate() Tests
    // ==========================

    @Test
    void testValidate_ReturnsTrue_WhenAlphanumeric() {
        assertTrue(AppUtil.validate("abc123"));
    }

    @Test
    void testValidate_ReturnsTrue_WhenContainsDash() {
        assertTrue(AppUtil.validate("slide-id-001"));
    }

    @Test
    void testValidate_ReturnsTrue_WhenContainsUnderscore() {
        assertTrue(AppUtil.validate("slide_id_001"));
    }

    @Test
    void testValidate_ReturnsTrue_WhenContainsDotAndSlash() {
        assertTrue(AppUtil.validate("path/to/file.svs"));
    }

    @Test
    void testValidate_ReturnsFalse_WhenContainsSpace() {
        assertFalse(AppUtil.validate("invalid id"));
    }

    @Test
    void testValidate_ReturnsFalse_WhenContainsSpecialCharacters() {
        assertFalse(AppUtil.validate("id@#!"));
    }

    @Test
    void testValidate_ReturnsFalse_WhenNull() {
        assertFalse(AppUtil.validate(null));
    }

    @Test
    void testValidate_ReturnsFalse_WhenEmptyString() {
        assertFalse(AppUtil.validate(""));
    }

    @Test
    void testValidate_ReturnsTrue_WhenOnlyDot() {
        assertTrue(AppUtil.validate("."));
    }

    // ==========================
    // extractOrganType() Tests
    // ==========================

    @Test
    void testExtractOrganType_ReturnsOrganMap_WhenValidStructure() {
        String specimenDescSeq = "00400560";
        String primaryAnatomicSeq = "00082228";
        String codeValueKey = "00080100";
        String codeMeaningKey = "00080104";

        Map<String, Object> structureItem = new HashMap<>();
        structureItem.put(codeValueKey, buildValueMap("T-62000"));
        structureItem.put(codeMeaningKey, buildValueMap("Prostate"));

        Map<String, Object> structureMap = new HashMap<>();
        structureMap.put(AppConstants.VALUE, List.of(structureItem));

        Map<String, Object> specimenItem = new HashMap<>();
        specimenItem.put(primaryAnatomicSeq, structureMap);

        Map<String, Object> specimenSeqMap = new HashMap<>();
        specimenSeqMap.put(AppConstants.VALUE, List.of(specimenItem));

        Map<String, Object> metadata = new HashMap<>();
        metadata.put(specimenDescSeq, specimenSeqMap);

        Map<String, String> result = AppUtil.extractOrganType(metadata, specimenDescSeq, primaryAnatomicSeq, codeValueKey, codeMeaningKey);

        assertFalse(result.isEmpty());
        assertEquals("Prostate", result.get("T-62000"));
    }

    @Test
    void testExtractOrganType_ReturnsEmptyMap_WhenSpecimenDescSeqMissing() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("someOtherKey", "value");

        Map<String, String> result = AppUtil.extractOrganType(metadata, "00400560", "00082228", "00080100", "00080104");
        assertTrue(result.isEmpty());
    }

    @Test
    void testExtractOrganType_ReturnsEmptyMap_WhenEmptyMetadata() {
        Map<String, String> result = AppUtil.extractOrganType(Collections.emptyMap(), "00400560", "00082228", "00080100", "00080104");
        assertTrue(result.isEmpty());
    }

    @Test
    void testExtractOrganType_ReturnsEmptyMap_WhenPrimaryAnatomicSeqMissing() {
        String specimenDescSeq = "00400560";

        Map<String, Object> specimenItem = new HashMap<>();
        specimenItem.put("someOtherKey", "value");

        Map<String, Object> specimenSeqMap = new HashMap<>();
        specimenSeqMap.put(AppConstants.VALUE, List.of(specimenItem));

        Map<String, Object> metadata = new HashMap<>();
        metadata.put(specimenDescSeq, specimenSeqMap);

        Map<String, String> result = AppUtil.extractOrganType(metadata, specimenDescSeq, "00082228", "00080100", "00080104");
        assertTrue(result.isEmpty());
    }

    // ==========================
    // Helpers
    // ==========================

    /**
     * Builds a minimal metadata map with a single tag key pointing to a value list.
     */
    private Map<String, Object> buildTagMap(String tagKey, String tagValue) {
        Map<String, Object> valueMap = new HashMap<>();
        valueMap.put(AppConstants.VALUE, List.of(tagValue));

        Map<String, Object> metadata = new HashMap<>();
        metadata.put(tagKey, valueMap);
        return metadata;
    }

    /**
     * Builds a VALUE-wrapped single-value map for use inside nested structures.
     */
    private Map<String, Object> buildValueMap(String value) {
        Map<String, Object> map = new HashMap<>();
        map.put(AppConstants.VALUE, List.of(value));
        return map;
    }

    /**
     * Builds a full stain metadata structure with "Using substance" as the concept meaning.
     */
    private Map<String, Object> buildStainMetadata(String stainValue) {
        return buildStainMetadataWithMeaning("Using substance", stainValue);
    }

    /**
     * Builds a stain metadata structure with a configurable concept meaning.
     */
    private Map<String, Object> buildStainMetadataWithMeaning(String meaning, String stainValue) {
        // 0040A160 (stain text value)
        Map<String, Object> stainValueMap = new HashMap<>();
        stainValueMap.put(AppConstants.VALUE, List.of(stainValue));

        // 00080104 (concept code meaning)
        Map<String, Object> conceptCodeName = new HashMap<>();
        conceptCodeName.put(AppConstants.VALUE, List.of(meaning));

        // item inside 0040A043 value list
        Map<String, Object> item = new HashMap<>();
        item.put("00080104", conceptCodeName);

        // 0040A043
        Map<String, Object> tag043 = new HashMap<>();
        tag043.put(AppConstants.VALUE, List.of(item));

        // root map
        Map<String, Object> root = new HashMap<>();
        root.put("0040A043", tag043);
        root.put("0040A160", stainValueMap);

        return root;
    }
}