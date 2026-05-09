package com.chronos.UserService.service;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;
import org.springframework.stereotype.Service;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Optional;


@Service
public class KafkaMessageService {


    // Inject KafkaTemplate when running with Spring Kafka configured.
    // Keep non-final so the existing constructor remains unchanged for backward compatibility.
    private KafkaTemplate<String, Object> kafkaTemplate;

    // Backwards-compatible constructor used by unit tests and other callers that pass a KafkaTemplate directly
    public KafkaMessageService(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    // Use Optional injection so the application context can start when no KafkaTemplate bean is present
    // (e.g., in unit tests or environments without Kafka). If present, we'll use it.
    @Autowired
    public KafkaMessageService(Optional<KafkaTemplate<String, Object>> kafkaTemplateOpt) {
        this.kafkaTemplate = kafkaTemplateOpt.orElse(null);
    }

    public void sendMessage(String topic, Object message) {
        // keep existing single-arg signature for compatibility; delegate to two-arg with null key
        sendMessage(topic, null, message);
    }

    // New overload: send a message with topic, key and object using KafkaTemplate if available.
    public void sendMessage(String topic, String key, Object message) {
        if (kafkaTemplate != null) {
            if (key == null) {
                kafkaTemplate.send(topic, message);
            } else {
                kafkaTemplate.send(topic, key, message);
            }
        }
    }
}
