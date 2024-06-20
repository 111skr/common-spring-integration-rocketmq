package com.bitauto.ep.dujiangyan.common.infrastructure.spring.integration.rocketmq.support;

import org.springframework.messaging.MessageHeaders;

/**
 * @author songzhibo
 * @date 2021/11/2 16:43
 */
public class RocketMqHeaders {
    public static final String CONTENT_TYPE = MessageHeaders.CONTENT_TYPE;
    public static final String PREFIX = "Rocket";
    public static final String TIMESTAMP = "Timestamp";

    private RocketMqHeaders() {
    }
}
