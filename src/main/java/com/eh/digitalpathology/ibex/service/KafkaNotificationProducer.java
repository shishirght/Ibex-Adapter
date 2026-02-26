package com.eh.digitalpathology.ibex.service;

import com.eh.digitalpathology.ibex.model.BarcodeInstanceRequest;
import com.eh.digitalpathology.ibex.model.SlideScanProgressEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Objects;

@Service
public class KafkaNotificationProducer {

    private static final Logger log = LoggerFactory.getLogger(KafkaNotificationProducer.class.getName());
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final DatabaseService databaseService;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Value( "${kafka.topic.scan-progress}" )
    private String scanProgressTopic;

    public KafkaNotificationProducer(KafkaTemplate<String, String> kafkaTemplate, DatabaseService databaseService) {
        this.kafkaTemplate = kafkaTemplate;
        this.databaseService = databaseService;
    }

    public void sendNotification(String topic, String key, String data) {
        try {
            log.info("sendNotification :: send notification :: data :: {}, key :: {}", data, key);
            kafkaTemplate.send(topic, key, data);
            log.info("sendNotification:: Message sent successfully to :: {} ", topic);
        } catch (Exception ex) {
            log.error("sendNotification :: Failed to send message to {} : {} ", topic, ex.getMessage());
        }
    }

    public void notifyScanProgress(SlideScanProgressEvent slideScanProgressEvent) {
        try {
            sendNotification(scanProgressTopic,slideScanProgressEvent.slideBarcode(),objectMapper.writeValueAsString(slideScanProgressEvent));
        } catch (JsonProcessingException e) {
            log.error("notifyScanProgress :: JSON serialization failed | topic={} key={} error={}",
                    scanProgressTopic, slideScanProgressEvent.slideBarcode(), e.getMessage(), e);
        }
    }

    public void notifyScanProgressAndUpdateDicomInstances(SlideScanProgressEvent slideScanProgressEvent, BarcodeInstanceRequest error) {

        if (Objects.nonNull(error) && StringUtils.hasText(error.barcode()) && (StringUtils.hasText(error.seriesId()) || StringUtils.hasText(error.studyId()))) {
            databaseService.updateErrorForSlide(slideScanProgressEvent.scanStatus(), error);
        }
        notifyScanProgress(slideScanProgressEvent);
    }

}
