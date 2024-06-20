package com.bitauto.ep.dujiangyan.common.infrastructure.spring.integration.rocketmq.channel;

import com.bitauto.ep.dujiangyan.common.infrastructure.spring.integration.rocketmq.support.MappingUtils;
import com.bitauto.ep.dujiangyan.common.infrastructure.spring.integration.rocketmq.support.MessageConverter;
import com.bitauto.ep.dujiangyan.common.infrastructure.spring.integration.rocketmq.support.RocketMqHeaderMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.MQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.springframework.integration.channel.AbstractMessageChannel;
import org.springframework.messaging.Message;

import java.util.Objects;

/**
 * 延时队列
 *
 * @author songzhibo
 * @date 2021/11/1 19:55
 */
@Slf4j
public abstract class AbstractRocketMqChannel extends AbstractMessageChannel {

    public static final long DEFAULT_TIMEOUT = 10000;

    protected final MessageConverter messageConverter;
    private final String topic;
    private final String produceTags;
    private final MQProducer producer;
    private final RocketMqHeaderMapper outboundHeaderMapper;
    private final RocketMqHeaderMapper inboundHeaderMapper;

    protected AbstractRocketMqChannel(String topic, String produceTags, MQProducer producer, MessageConverter messageConverter, RocketMqHeaderMapper outboundHeaderMapper, RocketMqHeaderMapper inboundHeaderMapper) {
        this.topic = topic;
        this.produceTags = produceTags;
        this.producer = producer;
        this.outboundHeaderMapper = outboundHeaderMapper;
        this.inboundHeaderMapper = inboundHeaderMapper;
        this.messageConverter = messageConverter;
    }

    protected String getTopic() {
        return topic;
    }

    protected RocketMqHeaderMapper getInboundHeaderMapper() {
        return inboundHeaderMapper;
    }

    @Override
    protected boolean doSend(Message<?> message, long timeout) {

        timeout = timeout <= 0 ? DEFAULT_TIMEOUT : timeout;

        final org.apache.rocketmq.common.message.Message rocketMsg;
        try {
            rocketMsg = MappingUtils.mapMessage(message, messageConverter, getOutboundHeaderMapper(), false);
        } catch (Exception exception) {
            log.error("to rocketmq msg error.", exception);
            return false;
        }

        rocketMsg.setTopic(topic);
        rocketMsg.setTags(produceTags);
        try {
            processMessage(rocketMsg);

            final SendResult sendResult = this.producer.send(rocketMsg, timeout);

            final boolean success = Objects.equals(SendStatus.SEND_OK, sendResult.getSendStatus());

            if (!success) {
                log.warn(String.format("send msg status is [%s],msg body:{%s}, msg:{%s}", sendResult.getSendStatus(), new String(rocketMsg.getBody()), rocketMsg.toString()));
            }

            return success;
        } catch (Exception e) {
            log.error(String.format("channel %s send fail, msg body:%s, msg:%s", this.getFullChannelName(), new String(rocketMsg.getBody()), rocketMsg.toString()), e);
        }
        return false;
    }

    /**
     * 修饰消息
     *
     * @param rocketMsg 消息体
     */
    protected void processMessage(org.apache.rocketmq.common.message.Message rocketMsg) {
        // do nothing
    }

    @SneakyThrows
    @Override
    protected void onInit() {
        super.onInit();
        this.producer.start();
    }

    @Override
    public void destroy() {
        super.destroy();
        this.producer.shutdown();
    }

    protected MessageConverter getMessageConverter() {
        return messageConverter;
    }

    protected RocketMqHeaderMapper getOutboundHeaderMapper() {
        return outboundHeaderMapper;
    }
}
