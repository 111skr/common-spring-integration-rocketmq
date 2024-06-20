package com.bitauto.ep.dujiangyan.common.infrastructure.spring.integration.rocketmq.channel;

import com.bitauto.ep.dujiangyan.common.infrastructure.spring.integration.rocketmq.support.DelayTimeLevelProvider;
import com.bitauto.ep.dujiangyan.common.infrastructure.spring.integration.rocketmq.support.MessageConverter;
import com.bitauto.ep.dujiangyan.common.infrastructure.spring.integration.rocketmq.support.RocketMqHeaderMapper;
import org.apache.rocketmq.client.consumer.MQPushConsumer;
import org.apache.rocketmq.client.producer.MQProducer;
import org.apache.rocketmq.common.message.Message;
import org.springframework.integration.dispatcher.AbstractDispatcher;
import org.springframework.integration.dispatcher.RoundRobinLoadBalancingStrategy;
import org.springframework.integration.dispatcher.UnicastingDispatcher;

import java.util.Objects;

/**
 * 延时队列
 *
 * @author songzhibo
 * @date 2021/11/3 14:43
 */
public class DelayChannel extends AbstractSubscribableRocketMqChannel {


    private int delayTimeLevel;
    private DelayTimeLevelProvider delayTimeLevelProvider;

    public DelayChannel(String topic, String produceTags, MQProducer producer, MessageConverter messageConverter, RocketMqHeaderMapper outboundHeaderMapper, RocketMqHeaderMapper inboundHeaderMapper, MQPushConsumer consumer, String tagSelectExpress, DelayTimeLevelProvider delayTimeLevelProvider, int delayTimeLevel) {
        super(topic, produceTags, producer, messageConverter, outboundHeaderMapper, inboundHeaderMapper, consumer, tagSelectExpress);
        this.delayTimeLevelProvider = delayTimeLevelProvider;
        this.delayTimeLevel = delayTimeLevel;
    }

    @Override
    protected AbstractDispatcher createDispatcher() {
        UnicastingDispatcher unicastingDispatcher = new UnicastingDispatcher();
        unicastingDispatcher.setLoadBalancingStrategy(new RoundRobinLoadBalancingStrategy());
        return unicastingDispatcher;
    }

    @Override
    protected void processMessage(Message rocketMsg) {
        rocketMsg.setDelayTimeLevel(Objects.isNull(delayTimeLevelProvider) ? delayTimeLevel : delayTimeLevelProvider.getDelayTimeLevel());
    }


}
