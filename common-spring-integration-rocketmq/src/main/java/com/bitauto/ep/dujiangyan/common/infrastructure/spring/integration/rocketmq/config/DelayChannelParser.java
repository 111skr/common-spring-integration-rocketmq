package com.bitauto.ep.dujiangyan.common.infrastructure.spring.integration.rocketmq.config;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.integration.config.xml.AbstractChannelParser;
import org.springframework.integration.config.xml.IntegrationNamespaceUtils;
import org.w3c.dom.Element;

/**
 * 延时队列xml配置解析器
 *
 * @author songzhibo
 * @date 2021/11/1 19:42
 */
public class DelayChannelParser extends AbstractChannelParser {

    @Override
    protected BeanDefinitionBuilder buildBeanDefinition(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(DelayChannelFactoryBean.class);

        builder.addPropertyValue("beanName", element.getAttribute("id"));
        builder.addPropertyValue("topic", element.getAttribute("topic"));
        builder.addPropertyReference("producer", element.getAttribute("producer"));
        builder.addPropertyReference("consumer", element.getAttribute("consumer"));


        String[] valuesToPopulate = {
                "produce-tags", "tag-select-express", "delay-time-level"
        };

        for (String attribute : valuesToPopulate) {
            IntegrationNamespaceUtils.setValueIfAttributeDefined(builder, element, attribute);
        }

        String[] referencesToPopulate = {
                "rocketmq-message-converter", "outbound-header-mapper", "inbound-header-mapper", "delay-time-level-provider"
        };

        for (String attribute : referencesToPopulate) {
            IntegrationNamespaceUtils.setReferenceIfAttributeDefined(builder, element, attribute);
        }

        return builder;
    }
}
