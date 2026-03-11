package com.julien.lotterysystem.service;

import org.springframework.web.multipart.MultipartFile;

public interface PictureService {

    /**
     * 上传图片
     * @param file
     * @return 索引
     */
    String uploadPicture(MultipartFile file);
}
