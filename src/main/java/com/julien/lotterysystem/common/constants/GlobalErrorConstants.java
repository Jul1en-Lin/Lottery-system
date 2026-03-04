package com.julien.lotterysystem.common.constants;

import com.julien.lotterysystem.entity.errorcode.ErrorCode;

public interface GlobalErrorConstants {
    ErrorCode SUCCESS = new ErrorCode(200,"成功");
    ErrorCode INTERNAL_SERVER_ERROR = new ErrorCode(500,"服务器错误");
    ErrorCode UNKNOWN_ERROR = new ErrorCode(400,"客户端成功");
}
