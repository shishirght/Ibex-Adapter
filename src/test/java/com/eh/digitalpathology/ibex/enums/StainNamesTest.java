package com.eh.digitalpathology.ibex.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StainNamesTest {

    @Test
    void testFromStain_HE_AliasMatch() {
        StainNames result = StainNames.fromStain("Hematoxylin & Eosin");

        assertEquals(StainNames.H_E, result);
    }

    @Test
    void testFromStain_CaseInsensitiveMatch() {
        StainNames result = StainNames.fromStain("pr breast prognostic marker");

        assertEquals(StainNames.PR, result);
    }

    @Test
    void testFromStain_WithTrim() {
        StainNames result = StainNames.fromStain("  Ki-67  ");

        assertEquals(StainNames.KI67, result);
    }

    @Test
    void testFromStain_HER2Alias() {
        StainNames result = StainNames.fromStain("HER-2/neu");

        assertEquals(StainNames.HER2, result);
    }

    @Test
    void testFromStain_UnknownValue_ReturnsOther() {
        StainNames result = StainNames.fromStain("Unknown Stain");

        assertEquals(StainNames.OTHER, result);
    }

    @Test
    void testGetStainValues() {
        assertEquals("H&E", StainNames.H_E.getStain());
        assertEquals("PR", StainNames.PR.getStain());
        assertEquals("HER2", StainNames.HER2.getStain());
        assertEquals("Ki67", StainNames.KI67.getStain());
        assertEquals("Other", StainNames.OTHER.getStain());
    }

    @Test
    void testFromStain_OtherHasNoAliases() {
        // Since OTHER has no aliases, any input that doesn't match must return OTHER
        StainNames result = StainNames.fromStain("Random Value");

        assertEquals(StainNames.OTHER, result);
    }
}