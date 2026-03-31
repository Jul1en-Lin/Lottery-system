package com.julien.lotterysystem.service.drawprize;

import com.julien.lotterysystem.common.enums.PrizeTiersEnum;
import com.julien.lotterysystem.entity.dataobject.WinningRecord;
import com.julien.lotterysystem.entity.request.DrawPrizeRequest;
import com.julien.lotterysystem.entity.request.GetWinningRecordsRequest;
import com.julien.lotterysystem.entity.response.WinningRecordResponse;
import com.julien.lotterysystem.service.DrawPrizeService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
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
        request.setActivityId(25L);
        request.setPrizeId(6L);
        request.setWinningTime(new Date());
        request.setPrizeTiers(PrizeTiersEnum.TIER_SPECIAL);
        List<DrawPrizeRequest.Winner> winnerList = new ArrayList<>();
        DrawPrizeRequest.Winner winner = new DrawPrizeRequest.Winner();
        winner.setUserId(7L);
        winner.setUserName("zhangsan");
        winnerList.add(winner);
        request.setWinnerList(winnerList);
        drawPrizeService.drawPrize(request);
        log.info("异步抽奖接口，请求参数：{}", request);
    }

    @Test
    void getWinningRecord() {
        GetWinningRecordsRequest request = new GetWinningRecordsRequest();
        request.setActivityId(2L);
        List<WinningRecordResponse> winningRecords = drawPrizeService.getWinningRecords(request);
        log.info("中奖记录：{}", winningRecords);
    }
}
