package com.julien.lotterysystem.controller;

import com.julien.lotterysystem.entity.request.DrawPrizeRequest;
import com.julien.lotterysystem.service.DrawPrizeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DrawPrizeController {

    @Autowired
    private DrawPrizeService drawPrizeService;

    @RequestMapping("/drawPrize")
    public Boolean drawPrize(@RequestBody @Valid DrawPrizeRequest request) {
        drawPrizeService.drawPrize(request);
        return true;
    }

}
