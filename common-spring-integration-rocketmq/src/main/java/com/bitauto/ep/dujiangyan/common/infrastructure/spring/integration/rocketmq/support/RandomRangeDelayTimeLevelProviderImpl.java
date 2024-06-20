package com.bitauto.ep.dujiangyan.common.infrastructure.spring.integration.rocketmq.support;

import org.springframework.util.Assert;

/**
 * 范围内随机
 *
 * @author songzhibo
 * @date 2021/11/3 15:03
 */
public class RandomRangeDelayTimeLevelProviderImpl implements DelayTimeLevelProvider {

    private int begin;
    private int end;


    public RandomRangeDelayTimeLevelProviderImpl(int begin, int end) {

        Assert.isTrue(end > begin, "end must greater than begin");

        this.begin = begin;
        this.end = end;
    }

    @Override
    public int getDelayTimeLevel() {
        double random = Math.random();
        return (int) ((random * (end - begin)) + begin);
    }
}
