package com.julien.lotterysystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.julien.lotterysystem.common.constants.ErrorConstants;
import com.julien.lotterysystem.common.exception.LotteryException;
import com.julien.lotterysystem.entity.dataobject.Prize;
import com.julien.lotterysystem.entity.request.CreatePrizeRequest;
import com.julien.lotterysystem.entity.response.PrizeInfoListResponse;
import com.julien.lotterysystem.entity.response.PrizeInfoResponse;
import com.julien.lotterysystem.mapper.PrizeMapper;
import com.julien.lotterysystem.service.PrizeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@Service
public class PrizeServiceImpl implements PrizeService {

    @Autowired
    private PrizeMapper prizeMapper;

    /**
     * 分页查询奖品列表并转换为响应对象
     */
    @Override
    public PrizeInfoListResponse<PrizeInfoResponse> getPrizeInfoList(Page<Prize> page) {
        Page<Prize> prizeInfoList = prizeMapper.selectPage(page, new LambdaQueryWrapper<Prize>()
                .select(Prize::getId, Prize::getName, Prize::getImageUrl, Prize::getPrice, Prize::getDescription)
                .orderByDesc(Prize::getId));
        // 把每个 Prize 对象都交给 toPrizeInfoResponse 方法处理，转换成 PrizeInfoResponse
        List<PrizeInfoResponse> records = prizeInfoList.getRecords()
                .stream().map(this::toPrizeInfoResponse).toList();
        // 构造响应对象，包含总记录数和当前页的奖品列表
        PrizeInfoListResponse<PrizeInfoResponse> result = new PrizeInfoListResponse<>(
                Math.toIntExact(prizeInfoList.getTotal()), records);
        return result;
    }

    /**
     * 将奖品实体转换为列表响应对象
     */
    private PrizeInfoResponse toPrizeInfoResponse (Prize prize){
        PrizeInfoResponse response = new PrizeInfoResponse();
        response.setId(prize.getId());
        response.setName(prize.getName());
        response.setImageUrl(prize.getImageUrl());
        response.setPrice(prize.getPrice());
        response.setDescription(prize.getDescription());
        return response;
    }

    /**
     * 创建奖品，并返回奖品id
     */
    @Override
    public Long createPrize(CreatePrizeRequest createPrizeRequest, String pictureFileName) {
        if (!StringUtils.hasLength(pictureFileName)) {
            throw new LotteryException(ErrorConstants.PICTURE_PATH_EMPTY,"图片路径不能为空");
        }
        // 构建奖品实体
        Prize prize = new Prize();
        prize.setName(createPrizeRequest.getName());
        prize.setDescription(createPrizeRequest.getDescription());
        prize.setPrice(createPrizeRequest.getPrice());
        prize.setImageUrl(pictureFileName);
        // 插入数据库
        prizeMapper.insert(prize);
        return prize.getId();
    }
}
