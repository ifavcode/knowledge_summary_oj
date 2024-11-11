package cn.guet.ksmvcmain.consumer;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Service;

@RocketMQMessageListener(topic = "test-topic", consumerGroup = "testGroup")
@Slf4j
@Service
public class TestConsumer implements RocketMQListener<String> {
    @Override
    public void onMessage(String message) {
        System.out.println("深度学习" + message);
    }
}
