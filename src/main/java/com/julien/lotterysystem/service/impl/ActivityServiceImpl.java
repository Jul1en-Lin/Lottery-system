package com.julien.lotterysystem.service.impl;

import com.baomidou.mybatisplus.core.batch.MybatisBatch;
import com.julien.lotterysystem.common.constants.ErrorConstants;
import com.julien.lotterysystem.common.exception.LotteryException;
import com.julien.lotterysystem.entity.dataobject.Activity;
import com.julien.lotterysystem.entity.dataobject.ActivityPrize;
import com.julien.lotterysystem.entity.dataobject.ActivityUser;
import com.julien.lotterysystem.entity.request.CreateActivityRequest;
import com.julien.lotterysystem.entity.response.CreateActivityResponse;
import com.julien.lotterysystem.mapper.*;
import com.julien.lotterysystem.service.ActivityService;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ActivityServiceImpl implements ActivityService {

    @Autowired
    private ActivityMapper activityMapper;
    @Autowired
    private ActivityUserMapper activityUserMapper;
    @Autowired
    private ActivityPrizeMapper activityPrizeMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private PrizeMapper prizeMapper;
    @Autowired
    private SqlSessionFactory sqlSessionFactory;

    @Override
    @Transactional(rollbackFor = Exception.class) // 添加事务回滚
    public CreateActivityResponse create(CreateActivityRequest request) {
        if (request == null) {
            throw new LotteryException(ErrorConstants.VALIDATION_FAILED);
        }
        // 检验人员是否存在
        request.getActivityUserList().forEach(activityUserList -> {
            Long userId = activityUserList.getUserId();
            if (userMapper.selectById(userId) == null) {
                throw new LotteryException(ErrorConstants.USER_EMPTY);
            }
        });
        // 检验奖品是否存在
        request.getActivityPrizeList().forEach(activityPrizeList -> {
            Long prizeId = activityPrizeList.getPrizeId();
            if (prizeMapper.selectById(prizeId) == null) {
                throw new LotteryException(ErrorConstants.PRIZE_EMPTY);
            }
        });
        // 检验奖品数量与人员数量是否匹配
        int userCount = request.getActivityUserList().size();
        int prizeCount = request.getActivityPrizeList().size();
        if (prizeCount > userCount) {
            throw new LotteryException(ErrorConstants.PRIZE_COUNT_NOT_MATCH);
        }

        // 插入数据
        CreateActivityResponse response = new CreateActivityResponse();
        Long activityId = insertActivity(request);
        response.setActivityId(activityId);
        return response;
    }

    private Long insertActivity(CreateActivityRequest request) {
        // 插入活动表
        Activity activityInfo = new Activity();
        activityInfo.setActivityName(request.getName());
        activityInfo.setDescription(request.getDescription());
        try {
            activityMapper.insert(activityInfo);
        } catch (Exception e) {
            throw new LotteryException(ErrorConstants.INSERT_ERROR);
        }
        Long activityId = activityInfo.getId();

        // 插入活动关联用户表
        List<ActivityUser> activityUsers = request.getActivityUserList()
                .stream()
                .map(activityUserList -> {
                    ActivityUser activityUser = new ActivityUser();
                    activityUser.setUserId(activityUserList.getUserId());
                    activityUser.setUserName(activityUserList.getUserName());
                    activityUser.setActivityId(activityId);
                    return activityUser;
                })
                .collect(Collectors.toList());
        MybatisBatch<ActivityUser> userBatch = new MybatisBatch<>(sqlSessionFactory, activityUsers);
        MybatisBatch.Method<ActivityUser> userMethod = new MybatisBatch.Method<>(ActivityUserMapper.class);
        userBatch.execute(userMethod.insert());

        // 插入活动关联奖品表
        List<ActivityPrize> activityPrizes = request.getActivityPrizeList()
                .stream()
                .map(activityPrizeList -> {
                    ActivityPrize activityPrize = new ActivityPrize();
                    activityPrize.setPrizeId(activityPrizeList.getPrizeId());
                    activityPrize.setPrizeAmount(activityPrizeList.getPrizeAmount());
                    activityPrize.setPrizeTiers(activityPrizeList.getPrizeTiers());
                    activityPrize.setActivityId(activityId);
                    return activityPrize;
                })
                .collect(Collectors.toList());

        MybatisBatch<ActivityPrize> prizeBatch = new MybatisBatch<>(sqlSessionFactory, activityPrizes);
        MybatisBatch.Method<ActivityPrize> prizeMethod = new MybatisBatch.Method<>(ActivityPrizeMapper.class);
        prizeBatch.execute(prizeMethod.insert());

        return activityId;
    }
}
