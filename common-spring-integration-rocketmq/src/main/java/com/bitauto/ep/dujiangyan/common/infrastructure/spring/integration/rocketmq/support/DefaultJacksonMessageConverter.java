package com.bitauto.ep.dujiangyan.common.infrastructure.spring.integration.rocketmq.support;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageConst;
import org.springframework.integration.mapping.support.JsonHeaders;
import org.springframework.messaging.MessageHeaders;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;

/**
 * 默认的基于Jackson实现的json转换对象
 *
 * @author songzhibo
 * @date 2021/11/2 16:29
 */
@Slf4j
public class DefaultJacksonMessageConverter implements MessageConverter {

    public static final String APPLICATION_JSON = "application/json";
    private final ObjectMapper objectMapper;

    public DefaultJacksonMessageConverter() {
        this.objectMapper = new ObjectMapper()
                .registerModule(new Jdk8Module())
                .registerModule(new JavaTimeModule());
    }

    public DefaultJacksonMessageConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Message toMessage(Object object, Map<String, String> messageProperties) throws MessageConversionException {
        return toMessage(object, messageProperties, null);
    }

    @Override
    public Message toMessage(Object object, Map<String, String> messageProperties, Type genericType) throws MessageConversionException {

        Message message = new Message();
        try {
            message.setBody(objectMapper.writeValueAsBytes(object));
        } catch (JsonProcessingException e) {
            throw new MessageConversionException(e.getMessage(), e);
        }
        if (MapUtils.isNotEmpty(messageProperties)) {
            for (Map.Entry<String, String> entry : messageProperties.entrySet()) {
                if (!MessageConst.STRING_HASH_SET.contains(entry.getKey())
                        && StringUtils.isNotBlank(entry.getKey())
                        && StringUtils.isNotBlank(entry.getValue())) {
                    message.putUserProperty(entry.getKey(), entry.getValue());
                }
            }
        }
        message.putUserProperty(MessageHeaders.CONTENT_TYPE, APPLICATION_JSON);
        message.putUserProperty(JsonHeaders.TYPE_ID, objectMapper.constructType(object.getClass()).getRawClass().getName());
        return message;
    }

    @Override
    public Object fromMessage(Message message) throws MessageConversionException {

        if (!message.getProperties().containsKey(JsonHeaders.TYPE_ID)) {
            return message.getBody();
        }

        final String typeString = message.getProperty(JsonHeaders.TYPE_ID);

        final Class<?> targetClass;
        try {
            targetClass = TypeFactory.defaultInstance().findClass(typeString);
        } catch (ClassNotFoundException e) {
            log.error("string to Class fail.type:{}", typeString, e);
            return new String(message.getBody());
        }
        try {
            return objectMapper.readValue(message.getBody(), targetClass);
        } catch (IOException e) {
            final String s = new String(message.getBody());
            log.error("json to obj fail, return string res,raw json:{}", s, e);
            return s;
        }
    }
}
