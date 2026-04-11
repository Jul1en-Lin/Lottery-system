package com.julien.lotterysystem.controller;

import com.julien.lotterysystem.entity.dataobject.WinningRecord;
import com.julien.lotterysystem.entity.request.DrawPrizeRequest;
import com.julien.lotterysystem.entity.request.GetWinningRecordsRequest;
import com.julien.lotterysystem.entity.response.WinningRecordResponse;
import com.julien.lotterysystem.service.DrawPrizeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class DrawPrizeController {

    @Autowired
    private DrawPrizeService drawPrizeService;

    /**
     * 抽奖接口
     */
    @PostMapping("/drawPrize")
    public Boolean drawPrize(@RequestBody @Valid DrawPrizeRequest request) {
        drawPrizeService.drawPrize(request);
        return true;
    }

    /**
     * 查询中奖记录
     */
    @PostMapping("/getWinningRecords")
    public List<WinningRecordResponse> getWinningRecords(@RequestBody @Valid GetWinningRecordsRequest request) {
        return drawPrizeService.getWinningRecords(request);
    }

}
