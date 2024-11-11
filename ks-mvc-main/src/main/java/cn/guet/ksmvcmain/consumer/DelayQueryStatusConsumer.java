package cn.guet.ksmvcmain.consumer;

import cn.guet.ksmvcmain.entity.vo.Submissions;
import cn.guet.ksmvcmain.service.ExecResultService;
import com.example.kscommon.constant.MQConstant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Service;

@RocketMQMessageListener(topic = MQConstant.DELAY_CODE_CHECK, consumerGroup = "execCodeGroup")
@Slf4j
@Service
@RequiredArgsConstructor
public class DelayQueryStatusConsumer implements RocketMQListener<Submissions> {

    private final ExecResultService execResultService;

    @Override
    public void onMessage(Submissions message) {
        execResultService.updateExecResult(message);
    }
}
