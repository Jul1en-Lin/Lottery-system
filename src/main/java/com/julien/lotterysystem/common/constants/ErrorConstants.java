package com.julien.lotterysystem.common.constants;

import com.julien.lotterysystem.entity.errorcode.ErrorCode;

public interface ErrorConstants {

    // ------- 用户模块 --------- //
    // 密码错误
    Integer PASSWORD_CANNOT_BE_EMPTY = 10001;


    // ------- 奖品模块 --------- //
    // 图片上传失败
    Integer UPLOAD_PICTURE_FAILED = 200;
    // 图片路径不能为空
    Integer PICTURE_PATH_EMPTY = 201;

    ErrorCode PRIZE_COUNT_NOT_MATCH = new ErrorCode(202,"奖品数量与人员数量不匹配");
    ErrorCode PRIZE_EMPTY = new ErrorCode(203,"奖品为空");
    ErrorCode USER_EMPTY = new ErrorCode(204,"用户为空");
    ErrorCode INSERT_ERROR = new ErrorCode(205,"插入数据失败");
    ErrorCode SET_TIER_FAILED = new ErrorCode(206,"设置奖品等级失败");
    ErrorCode SET_ACTIVITY_DETAIL_FAIL = new ErrorCode(207,"设置活动详情DTO失败");
    ErrorCode SET_ACTIVITY_USER_LIST_FAIL = new ErrorCode(208,"设置活动用户列表DTO失败");
    ErrorCode SET_ACTIVITY_PRIZE_LIST_FAIL = new ErrorCode(209,"设置活动奖品列表DTO失败");
    ErrorCode CACHE_ID_EMPTY = new ErrorCode(210,"缓存活动id为空");
    ErrorCode CACHE_ERROR = new ErrorCode(211,"缓存活动详情失败");

    // ------- 参数校验 --------- //
    ErrorCode VALIDATION_FAILED = new ErrorCode(40001,"参数校验失败");

    // ------- 抽奖模块 --------- //
    ErrorCode ACTIVITY_NOT_EXIST = new ErrorCode(500,"活动不存在");
    ErrorCode ACTIVITY_PRIZE_NOT_EXIST = new ErrorCode(501,"活动关联奖品不存在");
    ErrorCode ACTIVITY_USER_NOT_EXIST = new ErrorCode(502,"活动关联人员不存在");
    ErrorCode ACTIVITY_OR_PRISE_EMPTY = new ErrorCode(503,"活动或奖品不存在，无法抽奖");
    ErrorCode ACTIVITY_STATUS_ERROR = new ErrorCode(504,"活动状态错误或不存在，无法抽奖");
    ErrorCode PRIZE_STATUS_ERROR = new ErrorCode(505,"奖品状态错误或不存在，无法抽奖");
    ErrorCode WINNERS_OR_PRIZES_AMOUNT_MISMATCH_ERROR = new ErrorCode(506,"中奖者数量与奖品数量不匹配，无法抽奖");
    ErrorCode OPERATOR_MAP_IS_NULL = new ErrorCode(507,"操作器映射为空");
    ErrorCode CONVERT_STATUS_FAILED = new ErrorCode(508,"状态扭转失败");
    ErrorCode PARAMETER_EMPTY = new ErrorCode(509,"中奖请求参数为空");
    ErrorCode GET_CACHE_ERROR = new ErrorCode(510,"获取缓存中奖信息失败");
}
