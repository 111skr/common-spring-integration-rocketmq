package com.bitauto.ep.dujiangyan.common.infrastructure.spring.integration.rocketmq.support;

import org.apache.rocketmq.common.message.Message;
import org.springframework.lang.Nullable;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * rocketmq 消息转换对象
 *
 * @author songzhibo
 * @date 2021/11/2 16:27
 */
public interface MessageConverter {

    /**
     * Convert a Java object to a Message.
     *
     * @param object            the object to convert
     * @param messageProperties The message properties.
     * @return the Message
     * @throws MessageConversionException in case of conversion failure
     */
    Message toMessage(Object object, Map<String, String> messageProperties) throws MessageConversionException;

    /**
     * Convert a Java object to a Message.
     * The default implementation calls {@link #toMessage(Object, Map<String,String>)}.
     *
     * @param object            the object to convert
     * @param messageProperties The message properties.
     * @param genericType       the type to use to populate type headers.
     * @return the Message
     * @throws MessageConversionException in case of conversion failure
     * @since 2.1
     */
    default Message toMessage(Object object, Map<String, String> messageProperties, @Nullable Type genericType)
            throws MessageConversionException {

        return toMessage(object, messageProperties);
    }

    /**
     * Convert from a Message to a Java object.
     *
     * @param message the message to convert
     * @return the converted Java object
     * @throws MessageConversionException in case of conversion failure
     */
    Object fromMessage(Message message) throws MessageConversionException;
}
