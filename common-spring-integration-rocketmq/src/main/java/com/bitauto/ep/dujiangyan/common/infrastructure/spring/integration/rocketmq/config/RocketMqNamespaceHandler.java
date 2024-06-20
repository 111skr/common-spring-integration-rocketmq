package com.bitauto.ep.dujiangyan.common.infrastructure.spring.integration.rocketmq.config;

import org.springframework.integration.config.xml.AbstractIntegrationNamespaceHandler;

/**
 * xml配置处理器
 *
 * @author songzhibo
 * @date 2021/11/1 19:39
 */
public class RocketMqNamespaceHandler extends AbstractIntegrationNamespaceHandler {
    @Override
    public void init() {
        this.registerBeanDefinitionParser("delay-channel", new DelayChannelParser());
    }
}
