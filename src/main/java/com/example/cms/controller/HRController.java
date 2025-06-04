package com.example.cms.controller;

import com.example.cms.common.Constant;
import com.example.cms.dto.base.ResponseData;
import com.example.cms.dto.request.MailForgetPasswordRequest;

import com.example.cms.service.HRService;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping(Constant.ROOT_PATH + "hr")
@RequiredArgsConstructor
public class HRController {

    private final HRService hrService;

    @GetMapping("get-all")
    public CompletableFuture<ResponseData> getAll(
            @NonNull @RequestHeader(Constant.TRANSACTION_ID_KEY) String transactionId
    ) {
        return CompletableFuture.completedFuture(ResponseData.createResponse(hrService.getAll(transactionId)));
    }

    @PutMapping("send-new-password")
    public CompletableFuture<ResponseData> sendNewPassword(
            @NonNull @RequestHeader(Constant.TRANSACTION_ID_KEY) String transactionId,
            @RequestBody MailForgetPasswordRequest request
    ) {
        return CompletableFuture.completedFuture(ResponseData.createResponse(hrService.sendNewPassword(transactionId,request)));
    }
}
