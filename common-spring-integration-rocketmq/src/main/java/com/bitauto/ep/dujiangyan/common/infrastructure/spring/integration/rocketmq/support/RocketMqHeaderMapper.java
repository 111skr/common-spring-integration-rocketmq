package com.bitauto.ep.dujiangyan.common.infrastructure.spring.integration.rocketmq.support;

import org.springframework.integration.mapping.RequestReplyHeaderMapper;

import java.util.Map;

/**
 * @author songzhibo
 * @date 2021/11/2 16:32
 */
public interface RocketMqHeaderMapper extends RequestReplyHeaderMapper<Map<String, String>> {
}
