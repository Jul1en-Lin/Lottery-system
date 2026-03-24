package com.julien.lotterysystem.service.impl;

import com.julien.lotterysystem.common.utils.JacksonUtil;
import com.julien.lotterysystem.entity.request.DrawPrizeRequest;
import com.julien.lotterysystem.service.DrawPrizeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.julien.lotterysystem.common.config.RabbitConfig.EXCHANGE_NAME;
import static com.julien.lotterysystem.common.config.RabbitConfig.ROUTING_KEY;

@Slf4j
@Service
public class DrawPrizeServiceImpl implements DrawPrizeService {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 异步抽奖接口，无需返回结果
     */
    @Override
    public void drawPrize(DrawPrizeRequest request) {
        Map<String,String> map = new HashMap<>();
        map.put("messageId", UUID.randomUUID().toString());
        map.put("messageBody", JacksonUtil.serialize(request));
        // 发送消息到队列
        // 需要指定交换机、与队列绑定的路由键、包装后的消息体
        rabbitTemplate.convertAndSend(EXCHANGE_NAME,ROUTING_KEY,map);
        log.info("成功发送抽奖消息到队列，消息体：{}", map);
    }
}
