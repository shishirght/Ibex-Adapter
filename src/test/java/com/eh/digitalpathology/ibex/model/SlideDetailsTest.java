package com.eh.digitalpathology.ibex.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SlideDetailsTest {

    private SlideDetails slideDetails;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private Validator validator;

    @BeforeEach
    void setUp() {
        slideDetails = new SlideDetails();
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    // ==========================
    // Constructor Tests
    // ==========================

    @Test
    void testDefaultConstructor_AllFieldsNull() {
        assertNull(slideDetails.getOrganType());
        assertNull(slideDetails.getStain());
        assertNull(slideDetails.getStainName());
        assertNull(slideDetails.getProcedure());
        assertNull(slideDetails.getSubTissue());
    }

    // ==========================
    // Getter & Setter Tests
    // ==========================

    @Test
    void testSetAndGetOrganType() {
        slideDetails.setOrganType("Prostate");
        assertEquals("Prostate", slideDetails.getOrganType());
    }

    @Test
    void testSetAndGetStain() {
        slideDetails.setStain("HE");
        assertEquals("HE", slideDetails.getStain());
    }

    @Test
    void testSetStain_ReturnsSlideDetailsInstance() {
        SlideDetails returned = slideDetails.setStain("HE");
        assertSame(slideDetails, returned);
    }

    @Test
    void testSetStain_SupportsMethodChaining() {
        SlideDetails result = new SlideDetails().setStain("HE");
        assertEquals("HE", result.getStain());
    }

    @Test
    void testSetAndGetStainName() {
        slideDetails.setStainName("Hematoxylin and Eosin");
        assertEquals("Hematoxylin and Eosin", slideDetails.getStainName());
    }

    @Test
    void testSetAndGetProcedure() {
        slideDetails.setProcedure("Biopsy");
        assertEquals("Biopsy", slideDetails.getProcedure());
    }

    @Test
    void testSetAndGetSubTissue() {
        slideDetails.setSubTissue("Glandular");
        assertEquals("Glandular", slideDetails.getSubTissue());
    }

    @Test
    void testSetOrganType_ToNull() {
        slideDetails.setOrganType("Prostate");
        slideDetails.setOrganType(null);
        assertNull(slideDetails.getOrganType());
    }

    @Test
    void testSetStainName_ToNull() {
        slideDetails.setStainName("HE");
        slideDetails.setStainName(null);
        assertNull(slideDetails.getStainName());
    }

    @Test
    void testSetProcedure_ToNull() {
        slideDetails.setProcedure("Biopsy");
        slideDetails.setProcedure(null);
        assertNull(slideDetails.getProcedure());
    }

    @Test
    void testSetSubTissue_ToNull() {
        slideDetails.setSubTissue("Glandular");
        slideDetails.setSubTissue(null);
        assertNull(slideDetails.getSubTissue());
    }

    @Test
    void testSetOrganType_ToEmptyString() {
        slideDetails.setOrganType("");
        assertEquals("", slideDetails.getOrganType());
    }

    @Test
    void testSetStain_ToEmptyString() {
        slideDetails.setStain("");
        assertEquals("", slideDetails.getStain());
    }

    // ==========================
    // @Size Validation Tests
    // ==========================

    @Test
    void testValidation_StainName_WithinLimit_NoViolations() {
        slideDetails.setStainName("A".repeat(255));
        Set<ConstraintViolation<SlideDetails>> violations = validator.validate(slideDetails);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testValidation_StainName_ExceedsLimit_HasViolation() {
        slideDetails.setStainName("A".repeat(256));
        Set<ConstraintViolation<SlideDetails>> violations = validator.validate(slideDetails);
        assertFalse(violations.isEmpty());
    }

    @Test
    void testValidation_StainName_ExactlyAtLimit_NoViolations() {
        slideDetails.setStainName("A".repeat(255));
        Set<ConstraintViolation<SlideDetails>> violations = validator.validate(slideDetails);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testValidation_StainName_Null_NoViolations() {
        slideDetails.setStainName(null);
        Set<ConstraintViolation<SlideDetails>> violations = validator.validate(slideDetails);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testValidation_StainName_Empty_NoViolations() {
        slideDetails.setStainName("");
        Set<ConstraintViolation<SlideDetails>> violations = validator.validate(slideDetails);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testValidation_ViolationIsOnCorrectField() {
        slideDetails.setStainName("A".repeat(256));
        Set<ConstraintViolation<SlideDetails>> violations = validator.validate(slideDetails);

        assertEquals(1, violations.size());
        ConstraintViolation<SlideDetails> violation = violations.iterator().next();
        assertEquals("stainName", violation.getPropertyPath().toString());
    }

    // ==========================
    // JSON Deserialization Tests
    // ==========================

    @Test
    void testDeserialization_AllFields() throws Exception {
        String json = """
                {
                    "organ_type": "Prostate",
                    "stain": "HE",
                    "stain_name": "Hematoxylin and Eosin",
                    "procedure": "Biopsy",
                    "sub_tissue": "Glandular"
                }
                """;

        SlideDetails deserialized = objectMapper.readValue(json, SlideDetails.class);

        assertEquals("Prostate", deserialized.getOrganType());
        assertEquals("HE", deserialized.getStain());
        assertEquals("Hematoxylin and Eosin", deserialized.getStainName());
        assertEquals("Biopsy", deserialized.getProcedure());
        assertEquals("Glandular", deserialized.getSubTissue());
    }

    @Test
    void testDeserialization_OrganTypeSnakeCaseKey() throws Exception {
        String json = "{\"organ_type\": \"Kidney\"}";
        SlideDetails deserialized = objectMapper.readValue(json, SlideDetails.class);
        assertEquals("Kidney", deserialized.getOrganType());
    }

    @Test
    void testDeserialization_StainNameSnakeCaseKey() throws Exception {
        String json = "{\"stain_name\": \"Periodic Acid Schiff\"}";
        SlideDetails deserialized = objectMapper.readValue(json, SlideDetails.class);
        assertEquals("Periodic Acid Schiff", deserialized.getStainName());
    }

    @Test
    void testDeserialization_SubTissueSnakeCaseKey() throws Exception {
        String json = "{\"sub_tissue\": \"Cortex\"}";
        SlideDetails deserialized = objectMapper.readValue(json, SlideDetails.class);
        assertEquals("Cortex", deserialized.getSubTissue());
    }

    @Test
    void testDeserialization_MissingFields_DefaultToNull() throws Exception {
        String json = "{\"stain\": \"IHC\"}";
        SlideDetails deserialized = objectMapper.readValue(json, SlideDetails.class);

        assertEquals("IHC", deserialized.getStain());
        assertNull(deserialized.getOrganType());
        assertNull(deserialized.getStainName());
        assertNull(deserialized.getProcedure());
        assertNull(deserialized.getSubTissue());
    }

    @Test
    void testDeserialization_EmptyJson_AllFieldsNull() throws Exception {
        String json = "{}";
        SlideDetails deserialized = objectMapper.readValue(json, SlideDetails.class);

        assertNull(deserialized.getOrganType());
        assertNull(deserialized.getStain());
        assertNull(deserialized.getStainName());
        assertNull(deserialized.getProcedure());
        assertNull(deserialized.getSubTissue());
    }

    @Test
    void testDeserialization_NullValues_InJson() throws Exception {
        String json = """
                {
                    "organ_type": null,
                    "stain": null,
                    "stain_name": null
                }
                """;

        SlideDetails deserialized = objectMapper.readValue(json, SlideDetails.class);

        assertNull(deserialized.getOrganType());
        assertNull(deserialized.getStain());
        assertNull(deserialized.getStainName());
    }

    // ==========================
    // JSON Serialization Tests
    // ==========================

    @Test
    void testSerialization_OrganTypeUsesSnakeCaseKey() throws Exception {
        slideDetails.setOrganType("Liver");
        String json = objectMapper.writeValueAsString(slideDetails);

        assertTrue(json.contains("organ_type"));
        assertFalse(json.contains("organType"));
    }

    @Test
    void testSerialization_StainNameUsesSnakeCaseKey() throws Exception {
        slideDetails.setStainName("HE");
        String json = objectMapper.writeValueAsString(slideDetails);

        assertTrue(json.contains("stain_name"));
        assertFalse(json.contains("stainName"));
    }

    @Test
    void testSerialization_SubTissueUsesSnakeCaseKey() throws Exception {
        slideDetails.setSubTissue("Cortex");
        String json = objectMapper.writeValueAsString(slideDetails);

        assertTrue(json.contains("sub_tissue"));
        assertFalse(json.contains("subTissue"));
    }

    @Test
    void testSerialization_StainNameOmitted_WhenNull() throws Exception {
        slideDetails.setStainName(null);
        String json = objectMapper.writeValueAsString(slideDetails);

        assertFalse(json.contains("stain_name"));
    }

    @Test
    void testSerialization_StainNameOmitted_WhenEmpty() throws Exception {
        slideDetails.setStainName("");
        String json = objectMapper.writeValueAsString(slideDetails);

        assertFalse(json.contains("stain_name"));
    }

    @Test
    void testSerialization_StainNameIncluded_WhenPresent() throws Exception {
        slideDetails.setStainName("Masson Trichrome");
        String json = objectMapper.writeValueAsString(slideDetails);

        assertTrue(json.contains("stain_name"));
        assertTrue(json.contains("Masson Trichrome"));
    }

    @Test
    void testSerialization_ProcedureOmitted_WhenNull() throws Exception {
        slideDetails.setProcedure(null);
        String json = objectMapper.writeValueAsString(slideDetails);

        assertFalse(json.contains("procedure"));
    }

    @Test
    void testSerialization_ProcedureOmitted_WhenEmpty() throws Exception {
        slideDetails.setProcedure("");
        String json = objectMapper.writeValueAsString(slideDetails);

        assertFalse(json.contains("procedure"));
    }

    @Test
    void testSerialization_ProcedureIncluded_WhenPresent() throws Exception {
        slideDetails.setProcedure("Resection");
        String json = objectMapper.writeValueAsString(slideDetails);

        assertTrue(json.contains("procedure"));
        assertTrue(json.contains("Resection"));
    }

    @Test
    void testSerialization_SubTissueOmitted_WhenNull() throws Exception {
        slideDetails.setSubTissue(null);
        String json = objectMapper.writeValueAsString(slideDetails);

        assertFalse(json.contains("sub_tissue"));
    }

    @Test
    void testSerialization_SubTissueOmitted_WhenEmpty() throws Exception {
        slideDetails.setSubTissue("");
        String json = objectMapper.writeValueAsString(slideDetails);

        assertFalse(json.contains("sub_tissue"));
    }

    @Test
    void testSerialization_SubTissueIncluded_WhenPresent() throws Exception {
        slideDetails.setSubTissue("Medulla");
        String json = objectMapper.writeValueAsString(slideDetails);

        assertTrue(json.contains("sub_tissue"));
        assertTrue(json.contains("Medulla"));
    }

    @Test
    void testSerialization_AllFields() throws Exception {
        slideDetails.setOrganType("Prostate");
        slideDetails.setStain("HE");
        slideDetails.setStainName("Hematoxylin and Eosin");
        slideDetails.setProcedure("Biopsy");
        slideDetails.setSubTissue("Glandular");

        String json = objectMapper.writeValueAsString(slideDetails);

        assertTrue(json.contains("Prostate"));
        assertTrue(json.contains("HE"));
        assertTrue(json.contains("Hematoxylin and Eosin"));
        assertTrue(json.contains("Biopsy"));
        assertTrue(json.contains("Glandular"));
    }

    // ==========================
    // Roundtrip Tests
    // ==========================

    @Test
    void testRoundtrip_SerializeAndDeserialize_AllFields() throws Exception {
        slideDetails.setOrganType("Kidney");
        slideDetails.setStain("PAS");
        slideDetails.setStainName("Periodic Acid Schiff");
        slideDetails.setProcedure("Nephrectomy");
        slideDetails.setSubTissue("Cortex");

        String json = objectMapper.writeValueAsString(slideDetails);
        SlideDetails deserialized = objectMapper.readValue(json, SlideDetails.class);

        assertEquals(slideDetails.getOrganType(), deserialized.getOrganType());
        assertEquals(slideDetails.getStain(), deserialized.getStain());
        assertEquals(slideDetails.getStainName(), deserialized.getStainName());
        assertEquals(slideDetails.getProcedure(), deserialized.getProcedure());
        assertEquals(slideDetails.getSubTissue(), deserialized.getSubTissue());
    }

    @Test
    void testRoundtrip_WithNullOptionalFields() throws Exception {
        slideDetails.setOrganType("Liver");
        slideDetails.setStain("HE");

        String json = objectMapper.writeValueAsString(slideDetails);
        SlideDetails deserialized = objectMapper.readValue(json, SlideDetails.class);

        assertEquals("Liver", deserialized.getOrganType());
        assertEquals("HE", deserialized.getStain());
        assertNull(deserialized.getStainName());
        assertNull(deserialized.getProcedure());
        assertNull(deserialized.getSubTissue());
    }
}