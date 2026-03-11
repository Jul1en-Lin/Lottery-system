package com.julien.lotterysystem.service.impl;

import com.julien.lotterysystem.common.constants.ErrorConstants;
import com.julien.lotterysystem.common.exception.LotteryException;
import com.julien.lotterysystem.service.PictureService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Slf4j
@Service
public class PictureServiceImpl implements PictureService {

    @Value("${picture.destPath}")
    public String destPath;

    /**
     * 保存上传图片并返回生成后的文件名。
     */
    @Override
    public String uploadPicture(MultipartFile file) {
        File dest = new File(destPath);
        if (!dest.exists()) {
            dest.mkdirs();
        }
        assert dest.exists();
        String fileName = file.getOriginalFilename();
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        fileName = UUID.randomUUID() + suffix;

        // 保存图片到指定目录
        try {
            file.transferTo(new File(destPath + "/" + fileName));
            log.info("上传图片路径为：{}", destPath + "/" + fileName);
        } catch (IOException e) {
            throw new LotteryException(ErrorConstants.UPLOAD_PICTURE_FAILED, "上传图片失败");
        }
        return fileName;
    }
}
