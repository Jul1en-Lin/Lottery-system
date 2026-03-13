package com.julien.lotterysystem.common.constants;

public interface ErrorConstants {

    // ------- 用户模块 --------- //
    // 密码错误
    Integer PASSWORD_CANNOT_BE_EMPTY = 10001;

    // ------- 奖品模块 --------- //
    // 图片上传失败
    Integer UPLOAD_PICTURE_FAILED = 200;
    // 图片路径不能为空
    Integer PICTURE_PATH_EMPTY = 201;

    // ------- 参数校验 --------- //
    // 参数校验失败
    Integer VALIDATION_FAILED = 40001;
}
