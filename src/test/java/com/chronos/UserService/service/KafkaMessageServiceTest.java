package com.chronos.UserService.service;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Properties;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

@ExtendWith(MockitoExtension.class)
class KafkaMessageServiceTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    private KafkaMessageService service;

    @BeforeEach
    void setUp() {
        service = new KafkaMessageService(kafkaTemplate);
    }

    @Test
    void sendMessage_withKey_usesKafkaTemplateSendWithKey() {
        String topic = "test-topic";
        String key = "key1";
        Object payload = "hello";

        // We don't need to stub kafkaTemplate.send since we only verify interaction
        service.sendMessage(topic, key, payload);

        verify(kafkaTemplate).send(topic, key, payload);
    }

    @Test
    void sendMessage_withoutKey_usesKafkaTemplateSendWithoutKey() {
        String topic = "test-topic";
        Object payload = "hello";

        service.sendMessage(topic, payload);

        verify(kafkaTemplate).send(topic, payload);
    }
}

