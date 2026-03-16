package com.julien.lotterysystem.service.impl;

import com.baomidou.mybatisplus.core.batch.MybatisBatch;
import com.julien.lotterysystem.common.constants.ErrorConstants;
import com.julien.lotterysystem.common.enums.ActivityStatusEnum;
import com.julien.lotterysystem.common.enums.PrizeStatusEnum;
import com.julien.lotterysystem.common.enums.PrizeTiersEnum;
import com.julien.lotterysystem.common.enums.UserStatusEnum;
import com.julien.lotterysystem.common.exception.LotteryException;
import com.julien.lotterysystem.common.utils.JacksonUtil;
import com.julien.lotterysystem.entity.dataobject.Activity;
import com.julien.lotterysystem.entity.dataobject.ActivityPrize;
import com.julien.lotterysystem.entity.dataobject.ActivityUser;
import com.julien.lotterysystem.entity.dto.ActivityDetailDto;
import com.julien.lotterysystem.entity.dto.ActivityPrizeDto;
import com.julien.lotterysystem.entity.dto.ActivityUserDto;
import com.julien.lotterysystem.entity.request.CreateActivityRequest;
import com.julien.lotterysystem.entity.response.CreateActivityResponse;
import com.julien.lotterysystem.mapper.*;
import com.julien.lotterysystem.service.ActivityService;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
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
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    // 活动缓存key前缀
    private final String ACTIVITY_PREFIX = "activity_";
    // 缓存超时时间，单位：秒
    private final Long CACHE_TIMEOUT = 60 * 60L;

    @Override
    @Transactional(rollbackFor = Exception.class) // 添加事务回滚，避免脏数据
    public CreateActivityResponse create(CreateActivityRequest request) {
        // 校验数据
        checkActivityRequest(request);
        // 插入数据
        Long activityId = insertActivity(request);
        // 构造返回
        CreateActivityResponse response = new CreateActivityResponse();
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
     * 整合活动信息，缓存到Redis中
     * @return 活动id
     */
    private Long insertActivity(CreateActivityRequest request) {
        // 插入活动表
        Activity activityInfo = new Activity();
        activityInfo.setActivityName(request.getName());
        activityInfo.setDescription(request.getDescription());
        activityInfo.setStatus(ActivityStatusEnum.START.name());// 初始化状态
        try {
            activityMapper.insert(activityInfo);
        } catch (Exception e) {
            log.error("插入活动表失败，活动名：{}", request.getName(),e);
            throw new LotteryException(ErrorConstants.INSERT_ERROR);
        }

        // 从request中单独抽取出来为活动关联用户对象列表
        List<ActivityUser> activityUsers = request.getActivityUserList()
            .stream()
            .map(activityUserList -> {
                ActivityUser activityUser = new ActivityUser();
                // 属性赋值
                activityUser.setUserId(activityUserList.getUserId());
                activityUser.setUserName(activityUserList.getUserName());
                activityUser.setActivityId(activityInfo.getId());
                activityUser.setStatus(UserStatusEnum.INIT.name()); // 初始化状态
                return activityUser;
            })
            .collect(Collectors.toList());
        // 批量插入活动关联用户表（ActivityUser）
        MybatisBatch<ActivityUser> userBatch = new MybatisBatch<>(sqlSessionFactory, activityUsers);
        MybatisBatch.Method<ActivityUser> userMethod = new MybatisBatch.Method<>(ActivityUserMapper.class);
        userBatch.execute(userMethod.insert());

        // 从request中单独抽取出来为活动关联奖品对象列表
        List<ActivityPrize> activityPrizes = request.getActivityPrizeList()
            .stream()
            .map(activityPrizeList -> {
                ActivityPrize activityPrize = new ActivityPrize();
                // 属性赋值
                activityPrize.setPrizeId(activityPrizeList.getPrizeId());
                activityPrize.setPrizeAmount(activityPrizeList.getPrizeAmount());
                activityPrize.setActivityId(activityInfo.getId());
                activityPrize.setStatus(PrizeStatusEnum.INIT.name());
                // 初始化奖品等级
                try {
                    activityPrize.setPrizeTiers(PrizeTiersEnum.forName(activityPrizeList.getPrizeTiers()).name());
                } catch (Exception e) {
                    log.error("设置奖品等级失败，奖品等级：{}", activityPrizeList.getPrizeTiers(),e);
                    throw new LotteryException(ErrorConstants.SET_TIER_FAILED);
                }
                return activityPrize;
            })
            .collect(Collectors.toList());
        // 批量插入活动关联奖品表（ActivityPrize）
        MybatisBatch<ActivityPrize> prizeBatch = new MybatisBatch<>(sqlSessionFactory, activityPrizes);
        MybatisBatch.Method<ActivityPrize> prizeMethod = new MybatisBatch.Method<>(ActivityPrizeMapper.class);
        prizeBatch.execute(prizeMethod.insert());

        // 整合活动信息，并缓存到Redis中
        ActivityDetailDto detailDto = converterActivityDetailDto(activityInfo, activityUsers, activityPrizes);
        // 缓存到Redis中
        cacheActivityDetail(detailDto);
        // 返回活动id
        return activityInfo.getId();
    }

    /**
     * 缓存活动详情到 Redis 中
     * 缓存失败不应该抛出异常，避免事务回滚，数据入库成功后，缓存失败不影响业务逻辑
     * @param detailDto 活动详情
     */
    private void cacheActivityDetail(ActivityDetailDto detailDto) {
        if (detailDto == null || detailDto.getAcivityId() == null) {
            log.warn("缓存活动详情失败，缓存活动id为空");
            throw new LotteryException(ErrorConstants.CACHE_ID_EMPTY);
        }
        // 缓存逻辑
        try {
             // 这里可以使用RedisTemplate或者其他Redis客户端进行缓存操作
             redisTemplate.opsForValue().set(ACTIVITY_PREFIX + detailDto.getAcivityId(),
                     JacksonUtil.serialize(detailDto),CACHE_TIMEOUT);
        } catch (Exception e) {
             log.error("缓存活动详情失败，缓存活动id：{}", detailDto.getAcivityId(),e);
             throw new LotteryException(ErrorConstants.CACHE_ERROR);
        }
    }

    /**
     * 转换为活动详情DTO
     * @param activityInfo 活动信息（获取活动id、活动名称、活动描述、活动状态）
     * @param activityUsers 活动关联用户列表（获取用户ID、用户名、用户状态）
     * @param activityPrizes 活动关联奖品列表（获取奖品ID、奖品名称、奖品数量、奖品等级、奖品状态）
     * @return 活动详情DTO
     */
    private ActivityDetailDto converterActivityDetailDto(Activity activityInfo,
                                                         List<ActivityUser> activityUsers,
                                                         List<ActivityPrize> activityPrizes) {
        ActivityDetailDto detailDto = new ActivityDetailDto();

        // 设置活动信息
        setActivityInfo(detailDto,activityInfo);
        // 设置活动关联用户Dto列表
        setActivityUserList(detailDto,activityUsers);
        // 设置活动关联奖品Dto列表
        setActivityPrizeList(detailDto,activityPrizes);

        return detailDto;
    }

    // 设置活动信息
    private void setActivityInfo(ActivityDetailDto detailDto, Activity activityInfo) {
        try {
            detailDto.setAcivityId(activityInfo.getId());
            detailDto.setActivityName(activityInfo.getActivityName());
            detailDto.setDescription(activityInfo.getDescription());
            // activityInfo已用过 Enum.forName,故直接获取描述
            detailDto.setStatus(activityInfo.getStatus());
        } catch (Exception e) {
            log.error("设置活动详情失败，活动名：{}", activityInfo.getActivityName(),e);
            throw new LotteryException(ErrorConstants.SET_ACTIVITY_DETAIL_FAIL);
        }
    }

    // 设置活动关联用户Dto列表
    private void setActivityUserList(ActivityDetailDto detailDto, List<ActivityUser> activityUsers) {
        try {
            List<ActivityUserDto> activityUserDtoList = activityUsers.stream()
            .map(activityUser -> {
                ActivityUserDto activityUserDto = new ActivityUserDto();
                // 属性赋值
                activityUserDto.setUserId(activityUser.getUserId());
                activityUserDto.setUserName(activityUser.getUserName());
                // activityUsers 已用过 Enum.forName,故直接获取描述
                activityUserDto.setUserStatus(activityUser.getStatus());
                return activityUserDto;
            })
            .collect(Collectors.toList());
        detailDto.setActivityUserList(activityUserDtoList);
        } catch (Exception e) {
            log.error("设置活动用户列表失败，用户数量：{}", activityUsers.size(),e);
            throw new LotteryException(ErrorConstants.SET_ACTIVITY_USER_LIST_FAIL);
        }

    }
    // 设置活动关联奖品Dto列表
    private void setActivityPrizeList(ActivityDetailDto detailDto, List<ActivityPrize> activityPrizes) {
        try {
            List<ActivityPrizeDto> activityPrizeDtoList = activityPrizes.stream()
            .map(activityPrize -> {
                ActivityPrizeDto activityPrizeDto = new ActivityPrizeDto();
                // 属性赋值
                activityPrizeDto.setPrizeId(activityPrize.getPrizeId());
                activityPrizeDto.setPrizeName(prizeMapper.selectById(activityPrize.getPrizeId()).getName());
                activityPrizeDto.setPrizeAmount(activityPrize.getPrizeAmount());
                // activityPrizes 已用过 Enum.forName,故直接获取描述
                activityPrizeDto.setPrizeTiers(activityPrize.getPrizeTiers());
                activityPrizeDto.setPrizeStatus(activityPrize.getStatus());
                return activityPrizeDto;
            })
            .collect(Collectors.toList());
        detailDto.setActivityPrizeList(activityPrizeDtoList);
        } catch (Exception e) {
            log.error("设置活动奖品列表失败，奖品数量：{}", activityPrizes.size(),e);
            throw new LotteryException(ErrorConstants.SET_ACTIVITY_PRIZE_LIST_FAIL);
        }
    }
}
