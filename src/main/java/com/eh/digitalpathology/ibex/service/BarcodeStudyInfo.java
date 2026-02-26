package com.eh.digitalpathology.ibex.service;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class BarcodeStudyInfo {

    private final Map<String, String> barcodeToStudyMap = new ConcurrentHashMap<>();

    public void put(String barcode, String studyUid) {
        barcodeToStudyMap.put(barcode, studyUid);
    }

    public String get(String barcode) {
        return barcodeToStudyMap.get(barcode);
    }

    public String remove(String barcode) {
        return barcodeToStudyMap.remove(barcode);
    }

    public String getBarcode(String studyId) {
        if (studyId == null) return null;
        return barcodeToStudyMap.entrySet()
                .stream()
                .filter(e -> e.getValue() != null && e.getValue().equalsIgnoreCase(studyId))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

}

