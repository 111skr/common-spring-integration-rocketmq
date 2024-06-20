package com.bitauto.ep.dujiangyan.common.infrastructure.spring.integration.rocketmq.support;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * @author songzhibo
 * @date 2021/11/2 16:28
 */
public class MessageConversionException extends Exception {

    public MessageConversionException(String message, Exception e) {
        super(message, e);
    }

    public MessageConversionException(String message, JsonProcessingException e) {
        super(message, e);
    }
}
