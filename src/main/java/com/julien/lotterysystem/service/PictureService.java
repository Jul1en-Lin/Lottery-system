package com.julien.lotterysystem.service;

import org.springframework.web.multipart.MultipartFile;

public interface PictureService {

    /**
     * 上传图片到本机并返回生成后的文件名
     * @return 索引——用于后续存储到数据库
     */
    String uploadPicture(MultipartFile file);
}
