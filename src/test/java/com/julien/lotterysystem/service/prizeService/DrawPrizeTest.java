package com.julien.lotterysystem.service.prizeService;

import com.julien.lotterysystem.common.enums.PrizeTiersEnum;
import com.julien.lotterysystem.entity.dataobject.WinningRecord;
import com.julien.lotterysystem.entity.request.DrawPrizeRequest;
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
        winner.setUserId(6L);
        winner.setUserName("zhangsan");
        winnerList.add(winner);
        request.setWinnerList(winnerList);
        drawPrizeService.drawPrize(request);
        log.info("异步抽奖接口，请求参数：{}", request);
    }

    /**
     * 测试用例：saveWinningRecord-正常保存单条中奖记录
     * 输入数据状态：活动id=1、奖品id=1、中奖者userId=1均存在于数据库
     * 预期输出：返回包含1条WinningRecord的列表，中奖时间正确设置
     * 验证目的：检查中奖记录各字段（activityId、prizeId、winnerId、winningTime等）是否正确映射
     */
    @Test
    void saveWinningRecord_SingleWinner_Success() {
        DrawPrizeRequest request = new DrawPrizeRequest();
        request.setActivityId(21L);
        request.setPrizeId(1L);
        request.setWinningTime(new Date());
        request.setPrizeTiers(PrizeTiersEnum.TIER_2);

        List<DrawPrizeRequest.Winner> winnerList = new ArrayList<>();
        DrawPrizeRequest.Winner winner = new DrawPrizeRequest.Winner();
        winner.setUserId(1L);
        winner.setUserName("hello");
        winnerList.add(winner);
        request.setWinnerList(winnerList);

        List<WinningRecord> result = drawPrizeService.saveWinningRecord(request);

        // 验证返回列表长度为1
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.size());

        // 验证中奖记录字段映射正确
        WinningRecord record = result.get(0);
        Assertions.assertEquals(21L, record.getActivityId());
        Assertions.assertEquals(1L, record.getPrizeId());
        Assertions.assertEquals(1L, record.getWinnerId());
        Assertions.assertEquals("hello", record.getWinnerName());
        Assertions.assertNotNull(record.getWinningTime());

        log.info("保存单条中奖记录成功，记录：{}", record);
    }

    /**
     * 测试用例：saveWinningRecord-正常保存多条中奖记录
     * 输入数据状态：活动id=1、奖品id=1、中奖者userId=1和userId=2均存在于数据库
     * 预期输出：返回包含2条WinningRecord的列表，每条记录对应不同中奖者
     * 验证目的：检查批量保存时每条记录的中奖者信息是否独立正确
     */
    @Test
    void saveWinningRecord_MultipleWinners_Success() {
        DrawPrizeRequest request = new DrawPrizeRequest();
        request.setActivityId(21L);
        request.setPrizeId(1L);
        request.setWinningTime(new Date());
        request.setPrizeTiers(PrizeTiersEnum.TIER_2);

        List<DrawPrizeRequest.Winner> winnerList = new ArrayList<>();

        DrawPrizeRequest.Winner winner1 = new DrawPrizeRequest.Winner();
        winner1.setUserId(4L);
        winner1.setUserName("test");
        winnerList.add(winner1);

        DrawPrizeRequest.Winner winner2 = new DrawPrizeRequest.Winner();
        winner2.setUserId(6L);
        winner2.setUserName("111");
        winnerList.add(winner2);

        request.setWinnerList(winnerList);

        List<WinningRecord> result = drawPrizeService.saveWinningRecord(request);

        // 验证返回列表长度为2
        Assertions.assertNotNull(result);
        Assertions.assertEquals(2, result.size());

        // 验证每条中奖记录的中奖者信息独立
        Assertions.assertEquals(4L, result.get(0).getWinnerId());
        Assertions.assertEquals("test", result.get(0).getWinnerName());
        Assertions.assertEquals(6L, result.get(1).getWinnerId());
        Assertions.assertEquals("111", result.get(1).getWinnerName());

        // 验证奖品和活动信息一致
        Assertions.assertEquals(21L, result.get(0).getActivityId());
        Assertions.assertEquals(1L, result.get(0).getPrizeId());
        Assertions.assertEquals(21L, result.get(1).getActivityId());
        Assertions.assertEquals(1L, result.get(1).getPrizeId());

        log.info("批量保存中奖记录成功，记录列表：{}", result);
    }
}
