package com.example.ecommerce.events;

public final class KafkaTopics {

    /** Choreographed saga — partition key: orderId */
    public static final String SAGA_EVENTS = "saga-events";

    public static final String ORDERS = "orders";
    public static final String INVENTORY = "inventory";
    public static final String PAYMENTS = "payments";
    public static final String NOTIFICATIONS = "notifications";
    public static final String PRODUCT_INDEX = "product-index";
    public static final String DLQ_SUFFIX = ".DLQ";

    private KafkaTopics() {
    }
}
