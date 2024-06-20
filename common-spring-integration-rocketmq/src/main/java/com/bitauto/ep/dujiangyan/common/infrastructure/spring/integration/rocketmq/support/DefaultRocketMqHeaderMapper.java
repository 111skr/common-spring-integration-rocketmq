package com.bitauto.ep.dujiangyan.common.infrastructure.spring.integration.rocketmq.support;

import com.google.common.collect.ImmutableMap;
import com.google.common.primitives.Ints;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.common.message.MessageConst;
import org.springframework.integration.IntegrationMessageHeaderAccessor;
import org.springframework.integration.mapping.AbstractHeaderMapper;
import org.springframework.integration.mapping.support.JsonHeaders;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Default implementation of {@link RocketMqHeaderMapper}.
 *
 * @author songzhibo
 */
public class DefaultRocketMqHeaderMapper extends AbstractHeaderMapper<Map<String, String>> implements RocketMqHeaderMapper {

    private static final List<String> STANDARD_HEADER_NAMES = new ArrayList<>();

    static {
        STANDARD_HEADER_NAMES.add(RocketMqHeaders.CONTENT_TYPE);
        STANDARD_HEADER_NAMES.add(JsonHeaders.TYPE_ID);
        STANDARD_HEADER_NAMES.add(JsonHeaders.CONTENT_TYPE_ID);
        STANDARD_HEADER_NAMES.add(JsonHeaders.KEY_TYPE_ID);
    }

    protected DefaultRocketMqHeaderMapper(String[] requestHeaderNames, String[] replyHeaderNames) {
        super(RocketMqHeaders.PREFIX, STANDARD_HEADER_NAMES, STANDARD_HEADER_NAMES);
        if (requestHeaderNames != null) {
            setRequestHeaderNames(requestHeaderNames);
        }
        if (replyHeaderNames != null) {
            setReplyHeaderNames(replyHeaderNames);
        }
    }

    /**
     * Construct a default inbound header mapper.
     *
     * @return the mapper.
     * @see #inboundRequestHeaders()
     * @see #inboundReplyHeaders()
     * @since 4.3
     */
    public static DefaultRocketMqHeaderMapper inboundMapper() {
        return new DefaultRocketMqHeaderMapper(inboundRequestHeaders(), inboundReplyHeaders());
    }

    /**
     * Construct a default outbound header mapper.
     *
     * @return the mapper.
     * @see #outboundRequestHeaders()
     * @see #outboundReplyHeaders()
     * @since 4.3
     */
    public static DefaultRocketMqHeaderMapper outboundMapper() {
        return new DefaultRocketMqHeaderMapper(outboundRequestHeaders(), outboundReplyHeaders());
    }

    /**
     * @return the default request headers for an inbound mapper.
     * @since 4.3
     */
    public static String[] inboundRequestHeaders() {
        return new String[]{"*"};
    }

    /**
     * @return the default reply headers for an inbound mapper.
     * @since 4.3
     */
    public static String[] inboundReplyHeaders() {
        return safeOutboundHeaders();
    }

    /**
     * @return the default request headers for an outbound mapper.
     * @since 4.3
     */
    public static String[] outboundRequestHeaders() {
        return safeOutboundHeaders();
    }

    /**
     * @return the default reply headers for an outbound mapper.
     * @since 4.3
     */
    public static String[] outboundReplyHeaders() {
        return new String[]{"*"};
    }

    private static String[] safeOutboundHeaders() {
        return new String[]{"!x-*", "*"};
    }

    /**
     * Extract "standard" headers from an AMQP MessageProperties instance.
     */
    @Override
    protected Map<String, Object> extractStandardHeaders(Map<String, String> rocketmqMessageProperties) {
        return null;
        /*final ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
        try {
            for (String jsonHeader : JsonHeaders.HEADERS) {
                String value = rocketmqMessageProperties.get(jsonHeader.replaceFirst(JsonHeaders.PREFIX, ""));
                if (Objects.nonNull(value) && StringUtils.hasText(value)) {
                    builder.put(jsonHeader, value);
                }
            }
        } catch (Exception e) {
            this.logger.warn("error occurred while mapping from AMQP properties to MessageHeaders", e);
        }
        return builder.build();*/
    }

    /**
     * Extract user-defined headers from an AMQP MessageProperties instance.
     */
    @Override
    protected Map<String, Object> extractUserDefinedHeaders(Map<String, String> rocketmqMessageProperties) {
        if (MapUtils.isEmpty(rocketmqMessageProperties)) {
            return null;
        }

        final ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();

        String headerName;
        for (Map.Entry<String, String> entry : rocketmqMessageProperties.entrySet()) {
            headerName = entry.getKey();
            if (!MessageConst.STRING_HASH_SET.contains(headerName)
                    && StringUtils.isNoneBlank(headerName)
                    && StringUtils.isNotBlank(entry.getValue())) {

                if (IntegrationMessageHeaderAccessor.SEQUENCE_NUMBER.equals(headerName)
                        || IntegrationMessageHeaderAccessor.SEQUENCE_SIZE.equals(headerName)
                        || IntegrationMessageHeaderAccessor.PRIORITY.equals(headerName)
                ) {
                    builder.put(entry.getKey(), Ints.tryParse(entry.getValue()));
                } else if (IntegrationMessageHeaderAccessor.ROUTING_SLIP.equals(headerName)) {
                    logger.warn(String.format("[%s] convert do not implement", IntegrationMessageHeaderAccessor.ROUTING_SLIP));
                } else if (IntegrationMessageHeaderAccessor.DUPLICATE_MESSAGE.equals(headerName)) {
                    builder.put(entry.getKey(), BooleanUtils.toBoolean(entry.getValue()));
                } else {
                    builder.put(entry.getKey(), entry.getValue());
                }
            }
        }

        return builder.build();
    }

    /**
     * Maps headers from a Spring Integration MessageHeaders instance to the MessageProperties
     * of an AMQP Message.
     */
    @Override
    protected void populateStandardHeaders(Map<String, Object> headers, Map<String, String> rocketmqMessageProperties) {
        // do nothing
        //        populateStandardHeaders(null, headers, rocketmqMessageProperties);
    }

    /**
     * Maps headers from a Spring Integration MessageHeaders instance to the MessageProperties
     * of an AMQP Message.
     */
    @Override
    protected void populateStandardHeaders(@Nullable Map<String, Object> allHeaders, Map<String, Object> headers,
                                           Map<String, String> rocketmqMessageProperties) {
        // do nothing
        /*if (MapUtils.isNotEmpty(headers)) {
            for (Map.Entry<String, Object> entry : headers.entrySet()) {
                if (!rocketmqMessageProperties.containsKey(entry.getKey())
                        && !MessageConst.STRING_HASH_SET.contains(entry.getKey())) {
                    final String val = getHeaderIfAvailable(headers, entry.getKey(), String.class);
                    if (Objects.nonNull(val)) {
                        rocketmqMessageProperties.put(entry.getKey(), val);
                    }

                }
            }
        }*/
    }

    @Override
    protected void populateUserDefinedHeader(String headerName, Object headerValue,
                                             Map<String, String> rocketmqMessageProperties) {
        if (!MessageConst.STRING_HASH_SET.contains(headerName)) {
            rocketmqMessageProperties.put(headerName, headerValue.toString());
        }
    }

}
