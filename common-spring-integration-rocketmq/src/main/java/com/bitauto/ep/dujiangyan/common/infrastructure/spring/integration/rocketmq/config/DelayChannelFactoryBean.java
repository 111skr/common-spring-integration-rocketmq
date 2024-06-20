package com.bitauto.ep.dujiangyan.common.infrastructure.spring.integration.rocketmq.config;

import com.bitauto.ep.dujiangyan.common.infrastructure.spring.integration.rocketmq.channel.DelayChannel;
import com.bitauto.ep.dujiangyan.common.infrastructure.spring.integration.rocketmq.support.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.rocketmq.client.consumer.MQPushConsumer;
import org.apache.rocketmq.client.producer.MQProducer;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.context.SmartLifecycle;
import org.springframework.messaging.support.ChannelInterceptor;

import java.util.List;
import java.util.Objects;

/**
 * @author songzhibo
 * @date 2021/11/5 0:50
 */
public class DelayChannelFactoryBean extends AbstractFactoryBean<DelayChannel>
        implements SmartLifecycle, BeanNameAware {

    private String topic;
    private String produceTags;
    private MQProducer producer;
    private MQPushConsumer consumer;
    private MessageConverter rocketmqMessageConverter;
    private RocketMqHeaderMapper outboundHeaderMapper = DefaultRocketMqHeaderMapper.outboundMapper();
    private RocketMqHeaderMapper inboundHeaderMapper = DefaultRocketMqHeaderMapper.inboundMapper();
    private String tagSelectExpress = "*";
    private int delayTimeLevel = 1;
    private DelayTimeLevelProvider delayTimeLevelProvider;
    private List<ChannelInterceptor> interceptors;

    private DelayChannel channel;
    private boolean isRunning = false;
    private String beanName;

    @Override
    public Class<?> getObjectType() {
        return DelayChannel.class;
    }

    @Override
    protected DelayChannel createInstance() {

        BeanFactory beanFactory = getBeanFactory();
        if (Objects.isNull(rocketmqMessageConverter) && beanFactory != null) {
            this.rocketmqMessageConverter = new DefaultJacksonMessageConverter(beanFactory.getBean(ObjectMapper.class));
        }

        this.channel = new DelayChannel(topic, produceTags, producer, rocketmqMessageConverter, outboundHeaderMapper, inboundHeaderMapper, consumer, tagSelectExpress, delayTimeLevelProvider, delayTimeLevel);
        channel.setComponentName(this.beanName);
        channel.setInterceptors(interceptors);
        channel.afterPropertiesSet();
        return channel;
    }

    @Override
    public void start() {
        this.isRunning = true;
    }

    @Override
    public void stop() {
        this.channel.destroy();
        this.isRunning = false;
    }

    @Override
    public boolean isRunning() {
        return this.isRunning;
    }


    public void setTopic(String topic) {
        this.topic = topic;
    }

    public void setProduceTags(String produceTags) {
        this.produceTags = produceTags;
    }

    public void setProducer(MQProducer producer) {
        this.producer = producer;
    }

    public void setRocketmqMessageConverter(MessageConverter rocketmqMessageConverter) {
        this.rocketmqMessageConverter = rocketmqMessageConverter;
    }

    public void setOutboundHeaderMapper(RocketMqHeaderMapper outboundHeaderMapper) {
        this.outboundHeaderMapper = outboundHeaderMapper;
    }

    public void setInboundHeaderMapper(RocketMqHeaderMapper inboundHeaderMapper) {
        this.inboundHeaderMapper = inboundHeaderMapper;
    }

    public void setConsumer(MQPushConsumer consumer) {
        this.consumer = consumer;
    }

    public void setTagSelectExpress(String tagSelectExpress) {
        this.tagSelectExpress = tagSelectExpress;
    }

    public void setInterceptors(List<ChannelInterceptor> interceptors) {
        this.interceptors = interceptors;
    }

    public void setDelayTimeLevel(int delayTimeLevel) {
        this.delayTimeLevel = delayTimeLevel;
    }

    public void setDelayTimeLevelProvider(DelayTimeLevelProvider delayTimeLevelProvider) {
        this.delayTimeLevelProvider = delayTimeLevelProvider;
    }

    @Override
    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }
}
