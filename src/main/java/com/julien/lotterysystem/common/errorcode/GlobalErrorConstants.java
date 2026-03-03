package com.julien.lotterysystem.common.errorcode;

public interface GlobalErrorConstants {
    ErrorCode SUCCESS = new ErrorCode(200,"成功");
    ErrorCode INTERNAL_SERVER_ERROR = new ErrorCode(500,"服务器错误");
    ErrorCode UNKNOWN_ERROR = new ErrorCode(400,"客户端成功");
}
