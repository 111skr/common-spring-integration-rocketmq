package com.bitauto.ep.dujiangyan.common.infrastructure.spring.integration.rocketmq.channel;

import com.bitauto.ep.dujiangyan.common.infrastructure.spring.integration.rocketmq.support.MessageConverter;
import com.bitauto.ep.dujiangyan.common.infrastructure.spring.integration.rocketmq.support.RocketMqHeaderMapper;
import lombok.SneakyThrows;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.rocketmq.client.consumer.MQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.producer.MQProducer;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.integration.dispatcher.AbstractDispatcher;
import org.springframework.integration.dispatcher.MessageDispatcher;
import org.springframework.integration.support.AbstractIntegrationMessageBuilder;
import org.springframework.integration.support.MessageBuilderFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.SubscribableChannel;

import java.util.List;
import java.util.Map;

/**
 * 实现SubscribableChannel订阅功能
 *
 * @author songzhibo
 * @date 2021/11/2 23:06
 */
abstract class AbstractSubscribableRocketMqChannel extends AbstractRocketMqChannel implements SubscribableChannel {

    private final MQPushConsumer consumer;

    private final String tagSelectExpress;
    private AbstractDispatcher dispatcher;

    protected AbstractSubscribableRocketMqChannel(String topic, String produceTags, MQProducer producer, MessageConverter messageConverter, RocketMqHeaderMapper outboundHeaderMapper, RocketMqHeaderMapper inboundHeaderMapper, MQPushConsumer consumer, String tagSelectExpress) {
        super(topic, produceTags, producer, messageConverter, outboundHeaderMapper, inboundHeaderMapper);
        this.consumer = consumer;
        this.tagSelectExpress = tagSelectExpress;
    }


    @Override
    public boolean subscribe(MessageHandler handler) {
        return this.dispatcher.addHandler(handler);
    }

    @Override
    public boolean unsubscribe(MessageHandler handler) {
        return this.dispatcher.removeHandler(handler);
    }

    @SneakyThrows
    @Override
    protected void onInit() {
        super.onInit();
        this.dispatcher = this.createDispatcher();

        consumer.subscribe(getTopic(), this.tagSelectExpress);
        if (logger.isInfoEnabled()) {
            logger.info(String.format("channel:%s implement with rocketmq subscribe topic:%s tagSelectExpress:%s", this.getFullChannelName(), getTopic(), this.tagSelectExpress));
        }
        DispatchingMessageListener listener = new DispatchingMessageListener(dispatcher, getMessageConverter(), this, getMessageBuilderFactory(), getInboundHeaderMapper());
        consumer.registerMessageListener(listener);
        consumer.start();
    }

    @Override
    public void destroy() {
        super.destroy();
        this.consumer.shutdown();
    }

    /**
     * create Dispatcher
     *
     * @return
     */
    protected abstract AbstractDispatcher createDispatcher();


    private static final class DispatchingMessageListener implements MessageListenerConcurrently {


        private final Log logger = LogFactory.getLog(this.getClass());

        private final MessageDispatcher dispatcher;

        private final MessageConverter converter;

        private final AbstractSubscribableRocketMqChannel channel;

        private final MessageBuilderFactory messageBuilderFactory;

        private final RocketMqHeaderMapper inboundHeaderMapper;

        private DispatchingMessageListener(MessageDispatcher dispatcher, MessageConverter converter, AbstractSubscribableRocketMqChannel channel, MessageBuilderFactory messageBuilderFactory, RocketMqHeaderMapper inboundHeaderMapper) {
            this.dispatcher = dispatcher;
            this.converter = converter;
            this.channel = channel;
            this.messageBuilderFactory = messageBuilderFactory;
            this.inboundHeaderMapper = inboundHeaderMapper;

        }

        @Override
        public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
            Message<?> messageToSend = null;
            ConsumeConcurrentlyStatus consumeStatus;
            try {
                for (MessageExt msg : msgs) {
                    Object converted = this.converter.fromMessage(msg);
                    messageToSend = (converted instanceof Message<?>) ? (Message<?>) converted
                            : buildMessage(msg, converted);

                    if (logger.isDebugEnabled()) {
                        logger.debug(String.format("channel %s begin dispatch msg:%s", channel.getFullChannelName(), msg.toString()));
                    }

                    this.dispatcher.dispatch(messageToSend);
                }
                consumeStatus = ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            } catch (Exception e) {
                String exceptionMessage = e.getMessage() + " for rocketmq-channel '"
                        + this.channel.getFullChannelName() + "'.";

                if (this.logger.isWarnEnabled()) {
                    this.logger.error(exceptionMessage, e);
                }
                consumeStatus = ConsumeConcurrentlyStatus.RECONSUME_LATER;
            }
            return consumeStatus;
        }

        protected Message<Object> buildMessage(MessageExt message, Object converted) {
            AbstractIntegrationMessageBuilder<Object> messageBuilder =
                    this.messageBuilderFactory.withPayload(converted);

            Map<String, Object> headers =
                    this.inboundHeaderMapper.toHeadersFromRequest(message.getProperties());
            messageBuilder.copyHeaders(headers);

            return messageBuilder.build();
        }
    }
}
