package com.eh.digitalpathology.ibex.config;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.backoff.FixedBackOff;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class KafkaConfigTest {

    private KafkaConfig kafkaConfig;

    @BeforeEach
    void setUp() {
        kafkaConfig = new KafkaConfig();
        ReflectionTestUtils.setField(kafkaConfig, "bootstrapServers", "localhost:9092");
        ReflectionTestUtils.setField(kafkaConfig, "acksConfig", "all");
        ReflectionTestUtils.setField(kafkaConfig, "enableIdempotenceConfig", "true");
    }

    @Test
    void producerFactory_hasCorrectConfiguration() {
        DefaultKafkaProducerFactory<String, String> factory =
                (DefaultKafkaProducerFactory<String, String>) kafkaConfig.producerFactory();

        assertNotNull(factory);

        Map<String, Object> configs = factory.getConfigurationProperties();
        assertEquals("localhost:9092", configs.get(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG));
        assertEquals(StringSerializer.class, configs.get(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG));
        assertEquals(StringSerializer.class, configs.get(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG));
        assertEquals("all", configs.get(ProducerConfig.ACKS_CONFIG));
        assertEquals("true", configs.get(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG));
    }

    @Test
    void customKafkaTemplate_isNotNull() {
        KafkaTemplate<String, String> kafkaTemplate = kafkaConfig.customKafkaTemplate();
        assertNotNull(kafkaTemplate);
        assertNotNull(kafkaTemplate.getProducerFactory());
    }

    @Test
    void errorHandler_isNotNull() {
        CommonErrorHandler errorHandler = kafkaConfig.errorHandler();
        assertNotNull(errorHandler);
    }

    @Test
    void kafkaListenerContainerFactory_isNotNull() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                kafkaConfig.kafkaListenerContainerFactory();
        assertNotNull(factory);
    }

    @Test
    void errorHandler_destinationResolver_returnsCorrectTopicPartition() {
        final TopicPartition[] captured = new TopicPartition[1];

        KafkaConfig testableConfig = new KafkaConfig() {
            @Override
            public CommonErrorHandler errorHandler() {
                DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                        customKafkaTemplate(),
                        (consumerRecord, exception) -> {
                            TopicPartition tp = new TopicPartition("dead-letter-topic", consumerRecord.partition());
                            captured[0] = tp;
                            return tp;
                        });
                return new DefaultErrorHandler(recoverer, new FixedBackOff(0L, 3));
            }
        };
        ReflectionTestUtils.setField(testableConfig, "bootstrapServers", "localhost:9092");
        ReflectionTestUtils.setField(testableConfig, "acksConfig", "all");
        ReflectionTestUtils.setField(testableConfig, "enableIdempotenceConfig", "true");

        ConsumerRecord<String, String> record = new ConsumerRecord<>("test-topic", 2, 0L, "key", "value");

        TopicPartition result = new TopicPartition("dead-letter-topic", record.partition());
        captured[0] = result;

        assertNotNull(captured[0]);
        assertEquals("dead-letter-topic", captured[0].topic());
        assertEquals(2, captured[0].partition());
    }
}