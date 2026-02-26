package com.eh.digitalpathology.ibex.util;

import com.eh.digitalpathology.ibex.constants.AppConstants;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AppUtil {

    private AppUtil() {
    }

    private static final Logger log = LoggerFactory.getLogger(AppUtil.class.getName());

    public static String getTagValue(Map<String, Object> metadata, String tag) {
        return findValueByTag(metadata, tag).orElse(null);
    }

    private static Optional<String> findValueByTag(Object data, String tag) {
        if (data instanceof Map<?, ?> map) {
            Optional<String> directMatch = extractTagValue((Map<String, Object>) map, tag);
            if (directMatch.isPresent()) return directMatch;

            return searchNestedValues(map.values(), tag);
        }

        if (data instanceof List<?> list) {
            return searchNestedValues(list, tag);
        }

        return Optional.empty();
    }

    private static Optional<String> extractTagValue(Map<String, Object> map, String tag) {
        String value = extractValue(map, tag);
        return Optional.ofNullable(value);
    }

    private static String extractValue(Map<String, Object> map, String key) {
        if (!map.containsKey(key)) return null;

        Object valueObj = map.get(key);
        if (!(valueObj instanceof Map<?, ?> valueMap)) return null;

        Object listObj = valueMap.get(AppConstants.VALUE);
        if (!(listObj instanceof List<?> list) || list.isEmpty()) return null;

        return list.get(0).toString();
    }


    private static Optional<String> searchNestedValues(Collection<?> values, String tag) {
        for (Object value : values) {
            Optional<String> result = findValueByTag(value, tag);
            if (result.isPresent()) return result;
        }
        return Optional.empty();
    }

    public static Optional<String> getStainValue(Map<String, Object> metadataMap) {
        return findStainRecursive(metadataMap);
    }

    private static Optional<String> findStainRecursive(Map<String, Object> map) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            Optional<String> stain = checkForStain(map, key, value);
            if (stain.isPresent()) return stain;

            Optional<String> nestedResult = findStainRecursiveInNested(value);
            if (nestedResult.isPresent()) return nestedResult;
        }
        return Optional.empty();
    }

    private static Optional<String> checkForStain(Map<String, Object> map, String key, Object value) {
        if (!"0040A043".equals(key) || !(value instanceof Map)) return Optional.empty();

        Map<String, Object> tag043 = (Map<String, Object>) value;
        Object valueListObj = tag043.get(AppConstants.VALUE);

        if (!(valueListObj instanceof List<?> valueList)) return Optional.empty();

        for (Object item : valueList) {
            Optional<String> stain = extractStainIfUsingSubstance(map, item);
            if (stain.isPresent()) return stain;
        }

        return Optional.empty();
    }

    private static Optional<String> extractStainIfUsingSubstance(Map<String, Object> map, Object item) {
        if (!(item instanceof Map<?, ?> itemMap)) return Optional.empty();

        Object conceptObj = itemMap.get("00080104");
        if (!(conceptObj instanceof Map<?, ?> conceptCodeName)) return Optional.empty();

        Object meaningObj = conceptCodeName.get(AppConstants.VALUE);
        if (!(meaningObj instanceof List<?> meaningList) || meaningList.isEmpty()) return Optional.empty();

        if (!"Using substance".equalsIgnoreCase(meaningList.get(0).toString())) return Optional.empty();

        return extractStain(map);
    }

    private static Optional<String> findStainRecursiveInNested(Object value) {
        if (value instanceof Map<?, ?> nestedMap) {
            return findStainRecursive((Map<String, Object>) nestedMap);
        }

        if (value instanceof List<?> list) {
            for (Object item : list) {
                if (item instanceof Map<?, ?> itemMap) {
                    Optional<String> result = findStainRecursive((Map<String, Object>) itemMap);
                    if (result.isPresent()) return result;
                }
            }
        }

        return Optional.empty();
    }

    private static Optional<String> extractStain(Map<String, Object> map) {
        return Optional.ofNullable(extractValue(map, "0040A160"));
    }

    public static Map<String, Object> convertStringToJson(String data) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode node = mapper.readTree(data);
            if (node.isArray() && !node.isEmpty()) {
                return mapper.convertValue(node.get(0), new TypeReference<>() {
                });
            } else if (node.isObject()) {
                return mapper.convertValue(node, new TypeReference<>() {
                });
            } else {
                log.error("Unexpected json structure.");
                return Map.of();
            }
        } catch (JsonProcessingException ex) {
            log.error("convertStringToJson :: error while parsing the data:: {}", ex.getMessage());
            return Map.of();
        }
    }

    public static Map<String, String> extractOrganType(
            Map<String, Object> metadataMap,
            String specimenDescriptionSeq,
            String primaryAnatomicStructureSeq,
            String codeValueKey,
            String codeMeaningKey) {

        Map<String, String> organMap = new HashMap<>();
        traverseForOrgan(metadataMap, specimenDescriptionSeq, primaryAnatomicStructureSeq, codeValueKey, codeMeaningKey, organMap);
        return organMap;
    }

    private static void traverseForOrgan(
            Object data,
            String specimenDescriptionSeq,
            String primaryAnatomicStructureSeq,
            String codeValueKey,
            String codeMeaningKey,
            Map<String, String> organMap) {

        if (data instanceof Map<?, ?> map) {
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                String key = entry.getKey().toString();
                Object value = entry.getValue();

                if (specimenDescriptionSeq.equals(key)) {
                    processSpecimenEntry(value, primaryAnatomicStructureSeq, codeValueKey, codeMeaningKey, organMap);
                }

                traverseForOrgan(value, specimenDescriptionSeq, primaryAnatomicStructureSeq, codeValueKey, codeMeaningKey, organMap);
            }
        } else if (data instanceof List<?> list) {
            for (Object item : list) {
                traverseForOrgan(item, specimenDescriptionSeq, primaryAnatomicStructureSeq, codeValueKey, codeMeaningKey, organMap);
            }
        }
    }

    private static void processSpecimenEntry(
            Object value,
            String primaryAnatomicStructureSeq,
            String codeValueKey,
            String codeMeaningKey,
            Map<String, String> organMap) {

        if (!(value instanceof Map<?, ?> specimenMap)) return;

        Object listObj = specimenMap.get(AppConstants.VALUE);
        if (!(listObj instanceof List<?> specimenList)) return;

        for (Object specimenItem : specimenList) {
            if (specimenItem instanceof Map<?, ?> specimenItemMap) {
                extractOrganFromSpecimen((Map<String, Object>) specimenItemMap, primaryAnatomicStructureSeq, codeValueKey, codeMeaningKey, organMap);
            }
        }
    }

    private static void extractOrganFromSpecimen(
            Map<String, Object> specimenItemMap,
            String primaryAnatomicStructureSeq,
            String codeValueKey,
            String codeMeaningKey,
            Map<String, String> organMap) {

        if (!specimenItemMap.containsKey(primaryAnatomicStructureSeq)) return;

        Object structureObj = specimenItemMap.get(primaryAnatomicStructureSeq);
        if (!(structureObj instanceof Map<?, ?> structureMap)) return;

        Object structureListObj = structureMap.get(AppConstants.VALUE);
        if (!(structureListObj instanceof List<?> structureList)) return;

        for (Object structureItem : structureList) {
            if (structureItem instanceof Map<?, ?> structureItemMap) {
                String codeValue = extractValue((Map<String, Object>) structureItemMap, codeValueKey);
                String codeMeaning = extractValue((Map<String, Object>) structureItemMap, codeMeaningKey);

                if (codeValue != null && codeMeaning != null) {
                    organMap.put(codeValue, codeMeaning);
                }
            }
        }
    }

    public static String convertObjectToString(Object object) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
        } catch (JsonProcessingException ex) {
            log.error("convertObjectToJson :: error while parsing the data:: {}", ex.getMessage());
        }
        return null;
    }

    public static boolean validate(String id) {
        if (Objects.nonNull(id)) {
            String regex = "^[a-zA-Z0-9-_./]+$";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(id);
            return matcher.matches();
        }
        return false;
    }
}
