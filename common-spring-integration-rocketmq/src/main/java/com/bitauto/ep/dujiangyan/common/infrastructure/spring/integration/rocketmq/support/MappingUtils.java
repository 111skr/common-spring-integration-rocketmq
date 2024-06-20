/*
 * Copyright 2016-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bitauto.ep.dujiangyan.common.infrastructure.spring.integration.rocketmq.support;

import lombok.SneakyThrows;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.util.MimeType;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility methods used during message mapping.
 *
 * @author Gary Russell
 * @author Artem Bilan
 * @since 4.3
 */
public final class MappingUtils {

    private MappingUtils() {
    }

    /**
     * Map an o.s.m.Message to an o.s.a.core.Message. When using a
     * {@link MessageHeaders#CONTENT_TYPE} will be used for the selection, with the AMQP
     * header taking precedence.
     *
     * @param requestMessage    the request message.
     * @param converter         the message converter to use.
     * @param headerMapper      the header mapper to use.
     * @param headersMappedLast true if headers are mapped after conversion.
     * @return the mapped Message.
     */
    public static org.apache.rocketmq.common.message.Message mapMessage(Message<?> requestMessage,
                                                                        MessageConverter converter, RocketMqHeaderMapper headerMapper,
                                                                        boolean headersMappedLast) {

        return doMapMessage(requestMessage, converter, headerMapper, headersMappedLast, false);
    }

    /**
     * Map a reply o.s.m.Message to an o.s.a.core.Message. When using a
     * {@link MessageHeaders#CONTENT_TYPE} will be used for the selection, with the AMQP
     * header taking precedence.
     *
     * @param replyMessage      the reply message.
     * @param converter         the message converter to use.
     * @param headerMapper      the header mapper to use.
     * @param headersMappedLast true if headers are mapped after conversion.
     * @return the mapped Message.
     * @since 5.1.9
     */
    public static org.apache.rocketmq.common.message.Message mapReplyMessage(Message<?> replyMessage,
                                                                             MessageConverter converter, RocketMqHeaderMapper headerMapper,
                                                                             boolean headersMappedLast) {

        return doMapMessage(replyMessage, converter, headerMapper, headersMappedLast, true);
    }

    @SneakyThrows
    private static org.apache.rocketmq.common.message.Message doMapMessage(Message<?> message,
                                                                           MessageConverter converter, RocketMqHeaderMapper headerMapper,
                                                                           boolean headersMappedLast, boolean reply) {

        Map<String, String> rocketMqProperties = new HashMap<>(8);

        org.apache.rocketmq.common.message.Message rocketmqMessage;
        if (!headersMappedLast) {
            mapHeaders(message.getHeaders(), rocketMqProperties, headerMapper, reply);
        }
        rocketmqMessage = converter.toMessage(message.getPayload(), rocketMqProperties);
        if (headersMappedLast) {
            mapHeaders(message.getHeaders(), rocketMqProperties, headerMapper, reply);
        }
        return rocketmqMessage;
    }

    private static void mapHeaders(MessageHeaders messageHeaders, Map<String, String> rocketMqMessageProperties,
                                   RocketMqHeaderMapper headerMapper, boolean reply) {

        if (reply) {
            headerMapper.fromHeadersToReply(messageHeaders, rocketMqMessageProperties);
        } else {
            headerMapper.fromHeadersToRequest(messageHeaders, rocketMqMessageProperties);
        }
    }

    private static String contentTypeAsString(MessageHeaders headers) {
        Object contentType = headers.get(RocketMqHeaders.CONTENT_TYPE);
        if (contentType instanceof MimeType) {
            contentType = contentType.toString();
        }
        if (contentType instanceof String) {
            return (String) contentType;
        } else if (contentType != null) {
            throw new IllegalArgumentException(RocketMqHeaders.CONTENT_TYPE
                    + " header must be a MimeType or String, found: " + contentType.getClass().getName());
        }
        return null;
    }


}
