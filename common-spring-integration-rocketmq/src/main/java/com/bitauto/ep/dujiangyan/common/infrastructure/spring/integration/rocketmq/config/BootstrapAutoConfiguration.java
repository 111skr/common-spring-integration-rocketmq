package com.bitauto.ep.dujiangyan.common.infrastructure.spring.integration.rocketmq.config;

import com.bitauto.ep.dujiangyan.common.infrastructure.spring.integration.rocketmq.support.DefaultJacksonMessageConverter;
import com.bitauto.ep.dujiangyan.common.infrastructure.spring.integration.rocketmq.support.MessageConverter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Objects;

/**
 * spring boot 自动配置入口类
 *
 * @author songzhibo
 * @date 2021/11/8 10:16
 */
@Configuration
public class BootstrapAutoConfiguration {


    @Bean
    @ConditionalOnMissingBean
    public MessageConverter rocketmqMessageConverter(ObjectProvider<ObjectMapper> objectProvider) {
        final ObjectMapper objectMapper = objectProvider.getIfAvailable();

        if (Objects.isNull(objectMapper)) {
            return new DefaultJacksonMessageConverter();
        } else {
            return new DefaultJacksonMessageConverter(objectMapper);
        }

    }
}
