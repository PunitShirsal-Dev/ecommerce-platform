package com.example.ecommerce.events;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CommonsEventsSanityTest {

    @Test
    void sagaTopicConstant() {
        assertEquals("saga-events", KafkaTopics.SAGA_EVENTS);
    }
}
