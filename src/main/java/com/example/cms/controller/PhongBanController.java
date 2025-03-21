package com.example.cms.controller;

import com.example.cms.common.Constant;
import com.example.cms.dto.base.ResponseData;
import com.example.cms.service.phong_ban.PhongBanService;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
@CrossOrigin("*")
@RequiredArgsConstructor
@RequestMapping(Constant.ROOT_PATH + "phong-ban")
public class PhongBanController {

    private final PhongBanService phongBanService;

    @GetMapping(value = "get-all")
    public CompletableFuture<ResponseData> getAll(
            @NonNull @RequestHeader(Constant.TRANSACTION_ID_KEY) String transactionId
    ) {
        return CompletableFuture.completedFuture(ResponseData.createResponse(phongBanService.getAll(transactionId)));
    }

    @PostMapping(value = "create")
    public CompletableFuture<ResponseData> create(
            @NonNull @RequestHeader(Constant.TRANSACTION_ID_KEY) String transactionId,
            @RequestParam("title") String title
    ) {
        return CompletableFuture.completedFuture(ResponseData.createResponse(phongBanService.create(transactionId, title)));
    }
}
