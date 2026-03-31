package com.julien.lotterysystem.service.mq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

import static com.julien.lotterysystem.common.config.RabbitConfig.*;

@Slf4j
@Configuration
@RabbitListener(queues = DLX_QUEUE_NAME)
public class DlxReceiver {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @RabbitHandler
    public void process(Map<String, String> message) {
        log.warn("死信队列收到消息: " + message);
        // rabbitTemplate.convertAndSend(EXCHANGE_NAME, ROUTING_KEY, message);
        // 正确的流程（扩展）：
        // 1、接收到异常消息，可以将异常消息存放到数据库表中
        // 2、存放后，当前异常消息消费完成，死信队列消息处理完成，但异常消息被我们持久化存储到表中了
        // 3、解决异常
        // 4、完成脚本任务，判断异常消息表中是否存在数据，如果存在，表示有消息未完成，此时处理消息
        // 5、处理消息：将消息发送给普通队列进行处理
    }
}
