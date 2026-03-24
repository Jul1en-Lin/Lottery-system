package com.julien.lotterysystem.PrizeService;

import com.julien.lotterysystem.common.enums.PrizeTiersEnum;
import com.julien.lotterysystem.entity.request.DrawPrizeRequest;
import com.julien.lotterysystem.service.DrawPrizeService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@SpringBootTest
public class DrawPrizeTest {

    @Autowired
    private DrawPrizeService drawPrizeService;

    @Test
    void drawPrize() {
        DrawPrizeRequest request = new DrawPrizeRequest();
        request.setActivityId(1L);
        request.setPrizeId(1L);
        request.setWinningTime(new Date());
        request.setPrizeTiers(PrizeTiersEnum.TIER_1);
        List<DrawPrizeRequest.Winner> winnerList = new ArrayList<>();
        DrawPrizeRequest.Winner winner = new DrawPrizeRequest.Winner();
        winner.setUserId(1L);
        winner.setUserName("张三");
        winnerList.add(winner);
        request.setWinnerList(winnerList);
        drawPrizeService.drawPrize(request);
        log.info("异步抽奖接口，请求参数：{}", request);
    }
}
