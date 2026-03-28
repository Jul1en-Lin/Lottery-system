package com.julien.lotterysystem.service.impl;

import com.baomidou.mybatisplus.core.batch.MybatisBatch;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.julien.lotterysystem.common.constants.ErrorConstants;
import com.julien.lotterysystem.common.enums.ActivityStatusEnum;
import com.julien.lotterysystem.common.enums.PrizeStatusEnum;
import com.julien.lotterysystem.common.enums.PrizeTiersEnum;
import com.julien.lotterysystem.common.enums.UserStatusEnum;
import com.julien.lotterysystem.common.exception.LotteryException;

import com.julien.lotterysystem.entity.dataobject.Activity;
import com.julien.lotterysystem.entity.dataobject.ActivityPrize;
import com.julien.lotterysystem.entity.dataobject.ActivityUser;
import com.julien.lotterysystem.entity.dataobject.Prize;
import com.julien.lotterysystem.entity.dto.ActivityDetailDto;
import com.julien.lotterysystem.entity.dto.ActivityPrizeDto;
import com.julien.lotterysystem.entity.dto.ActivityUserDto;
import com.julien.lotterysystem.entity.dto.ConvertActivityStatusDTO;
import com.julien.lotterysystem.entity.request.CreateActivityRequest;
import com.julien.lotterysystem.entity.response.ActivityListResponse;
import com.julien.lotterysystem.entity.response.CreateActivityResponse;
import com.julien.lotterysystem.mapper.*;
import com.julien.lotterysystem.service.ActivityService;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ActivityServiceImpl implements ActivityService {

    @Autowired
    private ActivityMapper activityMapper;
    @Autowired
    private ActivityUserMapper activityUserMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private PrizeMapper prizeMapper;
    @Autowired
    private SqlSessionFactory sqlSessionFactory;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // 活动缓存key前缀
    private final String ACTIVITY_PREFIX = "activity_";
    // 缓存超时时间，单位：秒
    private final Long CACHE_TIMEOUT = 60 * 60L;
    @Autowired
    private ActivityPrizeMapper activityPrizeMapper;

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
        cacheActivity(detailDto);
        // 返回活动id
        return activityInfo.getId();
    }

    /**
     * 入库成功后，缓存活动详情到 Redis 中
     * 缓存失败不应该抛出异常，避免事务回滚，数据入库成功后，缓存失败不影响业务逻辑
     * Redis: key = ACTIVITY_PREFIX + activityId
     *      : data = detailDto 活动详情（包含成功入库后的奖品url和price）
     */
    @Override
    public void cacheActivity(ActivityDetailDto detailDto) {
        if (null == detailDto || null == detailDto.getActivityId()) {
            log.error("缓存活动详情失败，缓存活动id为空");
            return;
        }
        // 完善detailDto（添加奖品图片地址和价格），便于后续直接取出奖品信息
        try {
            // 查奖品图片地址和价格是否已存在,存在则无需完善DTO，直接缓存
            List<ActivityPrizeDto> activityPrizeList = detailDto.getActivityPrizeList();
            boolean allPrizeInfoExists = activityPrizeList != null && activityPrizeList.stream()
                    .allMatch(activityPrizeDto ->
                            StringUtils.hasText(activityPrizeDto.getImageUrl())
                            && activityPrizeDto.getPrice() != null);
            if (allPrizeInfoExists) {
                // 奖品图片和价格均已存在，直接缓存
                redisTemplate.opsForValue()
                        .set(ACTIVITY_PREFIX + detailDto.getActivityId(),
                        detailDto, CACHE_TIMEOUT, TimeUnit.SECONDS);
                return;
            }
            // 查询活动奖品表
            List<ActivityPrize> activityPrize = activityPrizeMapper.selectList(new LambdaQueryWrapper<ActivityPrize>()
                    .eq(ActivityPrize::getActivityId, detailDto.getActivityId()));
            // 根据和活动id查询活动关联奖品ids
            List<Long> prizeIdsList = activityPrize.stream()
                   .map(ActivityPrize::getPrizeId)
                   .toList();
            // 赋值奖品价格和图片地址
            prizeIdsList.forEach(prizeId -> {
                Prize prize = prizeMapper.selectById(prizeId);
                detailDto.getActivityPrizeList()
                       .forEach(activityPrizeDto -> {
                           activityPrizeDto.setPrice(prize.getPrice());
                           activityPrizeDto.setImageUrl(prize.getImageUrl());
                       });
            });
             // 使用RedisTemplate客户端进行缓存操作，直接存入对象，由 GenericJackson2JsonRedisSerializer 自动序列化
             redisTemplate.opsForValue()
                     .set(ACTIVITY_PREFIX + detailDto.getActivityId(),
                     detailDto, CACHE_TIMEOUT, TimeUnit.SECONDS);
        } catch (LotteryException e) {
             log.error("缓存活动详情失败，缓存活动id：{}", detailDto.getActivityId(),e);
             // 缓存失败不应该抛出异常，避免事务回滚
        } catch (Exception e) {
             log.error("缓存活动详情失败", e);
             // 缓存失败不应该抛出异常，避免事务回滚
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
            detailDto.setActivityId(activityInfo.getId());
            detailDto.setActivityName(activityInfo.getActivityName());
            detailDto.setDescription(activityInfo.getDescription());
            detailDto.setStatus(ActivityStatusEnum.forName(activityInfo.getStatus()));
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
                activityUserDto.setUserStatus(UserStatusEnum.forName(activityUser.getStatus()));
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
                activityPrizeDto.setPrizeTiers(PrizeTiersEnum.forName(activityPrize.getPrizeTiers()));
                activityPrizeDto.setPrizeStatus(PrizeStatusEnum.forName(activityPrize.getStatus()));
                return activityPrizeDto;
            })
            .collect(Collectors.toList());
        detailDto.setActivityPrizeList(activityPrizeDtoList);
        } catch (Exception e) {
            log.error("设置活动奖品列表失败，奖品数量：{}", activityPrizes.size(),e);
            throw new LotteryException(ErrorConstants.SET_ACTIVITY_PRIZE_LIST_FAIL);
        }
    }

    /**
     * 翻页查询活动列表
     */
    @Override
    public ActivityListResponse queryActivityList(Page<Activity> page) {
        log.info("分页查询活动列表，page: {}", page);
        Page<Activity> activityList = activityMapper.selectPage(page,new LambdaQueryWrapper<Activity>()
                .select(Activity::getId,
                        Activity::getActivityName,
                        Activity::getDescription,
                        Activity::getStatus));
        // 构造当前页活动列表record
        List<ActivityListResponse.ActivityInfo> activityInfoList = activityList.getRecords()
                .stream()
                .map(activity -> {
                    ActivityListResponse.ActivityInfo activityInfo = new ActivityListResponse.ActivityInfo();
                    activityInfo.setActivityId(activity.getId());
                    activityInfo.setActivityName(activity.getActivityName());
                    activityInfo.setDescription(activity.getDescription());
                    activityInfo.setValid(ActivityStatusEnum.START.name().equals(activity.getStatus()));
                    return activityInfo;
                })
                .toList();
        // 构造活动总数total
        Long total = activityList.getTotal();

        // 构造返回
        ActivityListResponse response = new ActivityListResponse();
        response.setTotal(total);
        response.setRecords(activityInfoList);
        return response;
    }

    /**
     * 查询活动详情
     * 尝试从Redis获取活动详情，若不存在则从数据库查询并缓存
     * 复用了从converterActivityDetailDto、cacheActivityDetail方法
     * @see #converterActivityDetailDto,#cacheActivityDetail
     * @param activityId 活动id
     */
    @Override
    public ActivityDetailDto getActivityDetail(Long activityId) {
        if (activityId == null) {
            return null;
        }
        ActivityDetailDto activityDetail = new ActivityDetailDto();
        // 从Redis获取活动详情
        activityDetail = getActivityDetailFromCache(activityId);
        if (activityDetail != null) {
            log.info("从Redis获取活动详情成功，activityDetailDto：{}",activityDetail);
            return activityDetail;
        }
        // 从Redis获取失败，从数据库查询
        log.info("从Redis获取活动详情失败,查询数据库，activityId：{}", activityId);
        activityDetail = getActivityDetailFromDb(activityId);
        // 缓存活动详情到Redis
        cacheActivity(activityDetail);
        return activityDetail;
    }



    /**
     * 从 Redis 获取活动详情
     * @param activityId 活动id
     * @return 活动详情
     */
    private ActivityDetailDto getActivityDetailFromCache(Long activityId) {
        String key = ACTIVITY_PREFIX + activityId;
        try {
            Object cached = redisTemplate.opsForValue().get(key);
            if (cached == null) {
                log.info("从Redis获取活动详情失败，key：{}", key);
                return null;
            }
            // GenericJackson2JsonRedisSerializer 自动反序列化为目标对象
            if (cached instanceof ActivityDetailDto dto) {
                return dto;
            }
            log.warn("Redis缓存类型不匹配，key：{}，实际类型：{}", key, cached.getClass().getName());
            redisTemplate.delete(key);
            return null;
        } catch (Exception e) {
            log.error("从Redis获取活动详情异常，key：{}", key, e);
            // 删除坏缓存，避免重复命中同样的错误
            try {
                redisTemplate.delete(key);
            } catch (Exception ex) {
                log.warn("删除坏缓存失败，key：{}", key, ex);
            }
            return null;
        }
    }

    /**
     * 从数据库查询活动详情
     * @param activityId 活动id
     * @return 活动详情
     */
    private ActivityDetailDto getActivityDetailFromDb(Long activityId) {
        // 查询活动表
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            log.info("从数据库查询活动详情失败，activityId：{}", activityId);
            throw new LotteryException(ErrorConstants.ACTIVITY_NOT_EXIST);
        }

        // 查询活动人员表
        List<ActivityUser> activityUser = activityUserMapper.selectList(new LambdaQueryWrapper<ActivityUser>()
                .eq(ActivityUser::getActivityId, activityId));
        if (activityUser.isEmpty()) {
            log.warn("从数据库查询活动人员详情失败，activityId：{}", activityId);
            throw new LotteryException(ErrorConstants.ACTIVITY_USER_NOT_EXIST);
        }

        // 查询活动奖品表
        List<ActivityPrize> activityPrize = activityPrizeMapper.selectList(new LambdaQueryWrapper<ActivityPrize>()
                .eq(ActivityPrize::getActivityId, activityId));
        if (activityPrize.isEmpty()) {
            log.warn("从数据库查询活动奖品详情失败，activityId：{}", activityId);
            throw new LotteryException(ErrorConstants.ACTIVITY_PRIZE_NOT_EXIST);
        }

        ActivityDetailDto detailDto = converterActivityDetailDto(activity,
                        activityUser, activityPrize);

        // 根据和活动id查询活动关联奖品ids
        List<Long> prizeIdsList = activityPrize.stream()
               .map(ActivityPrize::getPrizeId)
               .toList();
        // 查询每个奖品的价格和图片地址并赋值活动详情DTO
        prizeIdsList.forEach(prizeId -> {
            Prize prize = prizeMapper.selectById(prizeId);
            detailDto.getActivityPrizeList()
                   .forEach(activityPrizeDto -> {
                       activityPrizeDto.setPrice(prize.getPrice());
                       activityPrizeDto.setImageUrl(prize.getImageUrl());
                   });
        });
        return detailDto;
    }

    @Override
    public void cacheActivityStatus(ConvertActivityStatusDTO activityStatusDTO) {
        Long activityId = activityStatusDTO.getActivityId();
        if (null == activityId) {
            log.warn("缓存活动状态失败，活动ID为空");
            return;
        }
        try {
            ActivityDetailDto activityDetailDto = getActivityDetailFromDb(activityId);
            cacheActivity(activityDetailDto);
        } catch (LotteryException e) {
            log.error("缓存活动状态失败，活动ID：{},原因:{}",
                    activityStatusDTO.getActivityId(), e.getErrMsg());
        } catch (Exception e) {
            log.error("缓存活动状态失败，活动ID：{}", activityStatusDTO.getActivityId(),e);
        }
    }
}
