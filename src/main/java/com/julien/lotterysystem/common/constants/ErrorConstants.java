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



    // ------- 参数校验 --------- //
    // 参数校验失败
    ErrorCode VALIDATION_FAILED = new ErrorCode(40001,"参数校验失败");
}
