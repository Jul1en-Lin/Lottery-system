package com.julien.lotterysystem.controller;

import com.julien.lotterysystem.service.PictureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/pic")
public class PictureController {
    @Autowired
    private PictureService pictureService;

    /**
     * 接收并上传图片文件。
     */
    @RequestMapping("/upload")
    public String uploadPicture(MultipartFile file){
        return pictureService.uploadPicture(file);
    }
}
