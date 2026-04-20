package com.thacbao.common.constant;

/**
 * RabbitMQ exchange, queue, and routing key constants shared across services.
 */
public class RabbitMQConstants {

    // ===== Exchanges =====
    public static final String ORDER_EXCHANGE = "neki.order.exchange";
    public static final String PAYMENT_EXCHANGE = "neki.payment.exchange";
    public static final String USER_EXCHANGE = "neki.user.exchange";

    // ===== Routing Keys =====
    // Order
    public static final String ORDER_CREATED_KEY = "order.created";
    public static final String ORDER_CANCELLED_KEY = "order.cancelled";
    public static final String ORDER_STATUS_UPDATED_KEY = "order.status.updated";

    // Payment
    public static final String PAYMENT_COMPLETED_KEY = "payment.completed";
    public static final String PAYMENT_FAILED_KEY = "payment.failed";

    // User
    public static final String USER_REGISTERED_KEY = "user.registered";
    public static final String USER_FORGOT_PASSWORD_KEY = "user.forgot.password";

    // ===== Queues =====
    // Notification queues
    public static final String NOTIFICATION_ORDER_CREATED_QUEUE = "notification.order.created";
    public static final String NOTIFICATION_ORDER_CANCELLED_QUEUE = "notification.order.cancelled";
    public static final String NOTIFICATION_ORDER_UPDATED_QUEUE = "notification.order.updated";
    public static final String NOTIFICATION_PAYMENT_QUEUE = "notification.payment";
    public static final String NOTIFICATION_USER_REGISTERED_QUEUE = "notification.user.registered";
    public static final String NOTIFICATION_USER_PASSWORD_QUEUE = "notification.user.password";

    // Product queues (inventory restore on cancellation)
    public static final String PRODUCT_ORDER_CANCELLED_QUEUE = "product.order.cancelled";
    public static final String PRODUCT_ORDER_CREATED_QUEUE = "product.order.created";

    // Order queues (payment results)
    public static final String ORDER_PAYMENT_COMPLETED_QUEUE = "order.payment.completed";
    public static final String ORDER_PAYMENT_FAILED_QUEUE = "order.payment.failed";

    // Recommendation queues
    public static final String RECOMMENDATION_ORDER_CREATED_QUEUE = "recommendation.order.created";
}
