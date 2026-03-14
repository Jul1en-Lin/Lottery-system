package com.julien.lotterysystem.service.impl;

import com.baomidou.mybatisplus.core.batch.MybatisBatch;
import com.julien.lotterysystem.common.constants.ErrorConstants;
import com.julien.lotterysystem.common.enums.ActivityStatusEnum;
import com.julien.lotterysystem.common.enums.PrizeStatusEnum;
import com.julien.lotterysystem.common.enums.PrizeTiersEnum;
import com.julien.lotterysystem.common.enums.UserStatusEnum;
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
    private UserMapper userMapper;
    @Autowired
    private PrizeMapper prizeMapper;
    @Autowired
    private SqlSessionFactory sqlSessionFactory;

    @Override
    @Transactional(rollbackFor = Exception.class) // 添加事务回滚
    public CreateActivityResponse create(CreateActivityRequest request) {
        // 校验数据
        checkActivityRequest(request);

        // 插入数据
        CreateActivityResponse response = new CreateActivityResponse();
        Long activityId = insertActivity(request);
        response.setActivityId(activityId);
        return response;
    }

    /**
     * 校验活动请求参数
     */
    private void checkActivityRequest(CreateActivityRequest request) {
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
    }

    /**
     * 插入活动表、活动关联用户表、活动关联奖品表
     * @return 活动id
     */
    private Long insertActivity(CreateActivityRequest request) {
        // 插入活动表
        Activity activityInfo = new Activity();
        activityInfo.setActivityName(request.getName());
        activityInfo.setDescription(request.getDescription());
        activityInfo.setStatus(ActivityStatusEnum.START.name());
        try {
            activityMapper.insert(activityInfo);
        } catch (Exception e) {
            throw new LotteryException(ErrorConstants.INSERT_ERROR);
        }
        Long activityId = activityInfo.getId();
        if (activityId == null) {
            throw new LotteryException(ErrorConstants.VALIDATION_FAILED);
        }

        // 转换为活动关联用户对象列表
        List<ActivityUser> activityUsers = request.getActivityUserList()
                .stream()
                .map(activityUserList -> {
                    ActivityUser activityUser = new ActivityUser();
                    // 属性赋值
                    activityUser.setUserId(activityUserList.getUserId());
                    activityUser.setUserName(activityUserList.getUserName());
                    activityUser.setActivityId(activityId);
                    activityUser.setStatus(UserStatusEnum.INIT.name());
                    return activityUser;
                })
                .collect(Collectors.toList());
        // 批量插入活动关联用户表（List<ActivityUser>）
        MybatisBatch<ActivityUser> userBatch = new MybatisBatch<>(sqlSessionFactory, activityUsers);
        MybatisBatch.Method<ActivityUser> userMethod = new MybatisBatch.Method<>(ActivityUserMapper.class);
        userBatch.execute(userMethod.insert());

        // 转换为活动关联奖品对象列表
        List<ActivityPrize> activityPrizes = request.getActivityPrizeList()
                .stream()
                .map(activityPrizeList -> {
                    ActivityPrize activityPrize = new ActivityPrize();
                    // 属性赋值
                    activityPrize.setPrizeId(activityPrizeList.getPrizeId());
                    activityPrize.setPrizeAmount(activityPrizeList.getPrizeAmount());
                    activityPrize.setActivityId(activityId);
                    activityPrize.setStatus(PrizeStatusEnum.INIT.name());

                    try {
                        activityPrize.setPrizeTiers(PrizeTiersEnum.forName(activityPrizeList.getPrizeTiers()).name());
                    } catch (Exception e) {
                        throw new LotteryException(ErrorConstants.SET_TIER_FAILED);
                    }

                    return activityPrize;
                })
                .collect(Collectors.toList());
        // 批量插入活动关联奖品表（List<ActivityPrize>）
        MybatisBatch<ActivityPrize> prizeBatch = new MybatisBatch<>(sqlSessionFactory, activityPrizes);
        MybatisBatch.Method<ActivityPrize> prizeMethod = new MybatisBatch.Method<>(ActivityPrizeMapper.class);
        prizeBatch.execute(prizeMethod.insert());

        return activityId;
    }
}
