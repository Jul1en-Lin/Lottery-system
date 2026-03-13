package com.julien.lotterysystem.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.julien.lotterysystem.entity.dataobject.Prize;
import com.julien.lotterysystem.entity.request.CreatePrizeRequest;
import com.julien.lotterysystem.entity.response.PrizeInfoListResponse;
import com.julien.lotterysystem.entity.response.PrizeInfoResponse;
import com.julien.lotterysystem.service.PictureService;
import com.julien.lotterysystem.service.PrizeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping("/prize")
public class PrizeController {

    @Autowired
    private PrizeService prizeService;
    @Autowired
    private PictureService pictureService;

    /**
     * 分页获取奖品列表。
     */
    @GetMapping("/getList")
    public PrizeInfoListResponse<PrizeInfoResponse> getList(Page<Prize> page) {
        return prizeService.getPrizeInfoList(page);
    }

    /**
     * 创建奖品
     * @return 奖品ID
     */
    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Long createPrize(@RequestPart("params") @Valid CreatePrizeRequest createPrizeRequest,
                            @RequestPart("file") MultipartFile file) {
        // 获取图片索引，后续存储到数据库
        String pictureFileName = pictureService.uploadPicture(file);
        return prizeService.createPrize(createPrizeRequest, pictureFileName);
    }
}
