package com.julien.lotterysystem.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.julien.lotterysystem.entity.dataobject.Prize;
import com.julien.lotterysystem.entity.response.PrizeInfoListResponse;
import com.julien.lotterysystem.entity.response.PrizeInfoResponse;
import com.julien.lotterysystem.service.PrizeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/prize")
public class PrizeController {

    @Autowired
    private PrizeService prizeService;

    /**
     * 分页获取奖品列表。
     */
    @GetMapping("/getList")
    public PrizeInfoListResponse<PrizeInfoResponse> getList(Page<Prize> page) {
        return prizeService.getPrizeInfoList(page);
    }
}
