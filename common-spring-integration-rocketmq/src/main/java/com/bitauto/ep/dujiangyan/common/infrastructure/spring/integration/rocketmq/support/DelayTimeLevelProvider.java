package com.bitauto.ep.dujiangyan.common.infrastructure.spring.integration.rocketmq.support;

/**
 * 延时队列level提供对象
 *
 * @author songzhibo
 * @date 2021/11/3 15:01
 */
public interface DelayTimeLevelProvider {
    /**
     * 提供延时leve
     *
     * @return
     */
    int getDelayTimeLevel();
}
