package com.eh.digitalpathology.ibex.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class KafkaConfigTest {

    private KafkaConfig kafkaConfig;

    @BeforeEach
    void setUp() {
        kafkaConfig = new KafkaConfig();

        // Inject @Value fields manually
        ReflectionTestUtils.setField(kafkaConfig, "bootstrapServers", "localhost:9092");
        ReflectionTestUtils.setField(kafkaConfig, "acksConfig", "all");
        ReflectionTestUtils.setField(kafkaConfig, "enableIdempotenceConfig", "true");
    }

    @Test
    void testProducerFactoryConfiguration() {
        DefaultKafkaProducerFactory<String, String> factory =
                (DefaultKafkaProducerFactory<String, String>) kafkaConfig.producerFactory();

        assertNotNull(factory);

        Map<String, Object> configs = factory.getConfigurationProperties();

        assertEquals("localhost:9092",
                configs.get(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG));
        assertEquals(StringSerializer.class,
                configs.get(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG));
        assertEquals(StringSerializer.class,
                configs.get(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG));
        assertEquals("all",
                configs.get(ProducerConfig.ACKS_CONFIG));
        assertEquals("true",
                configs.get(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG));
    }

    @Test
    void testCustomKafkaTemplateCreation() {
        KafkaTemplate<String, String> kafkaTemplate =
                kafkaConfig.customKafkaTemplate();

        assertNotNull(kafkaTemplate);
        assertNotNull(kafkaTemplate.getProducerFactory());
    }

    @Test
    void testErrorHandlerCreation() {
        CommonErrorHandler errorHandler = kafkaConfig.errorHandler();

        assertNotNull(errorHandler);
    }

    @Test
    void testKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                kafkaConfig.kafkaListenerContainerFactory();

        assertNotNull(factory);
        //factory.setCommonErrorHandler();
    }
}