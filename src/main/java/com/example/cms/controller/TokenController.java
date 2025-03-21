package com.example.cms.controller;

import com.example.cms.common.Constant;
import com.example.cms.dto.base.ResponseData;
import com.example.cms.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping(Constant.ROOT_PATH)
@RequiredArgsConstructor
@CrossOrigin("*")
public class TokenController {

    private final TokenService tokenService;

    @GetMapping(value = "token/check-accessToken")
    public CompletableFuture<ResponseData> checkAccessToken(@NonNull @RequestHeader(Constant.TRANSACTION_ID_KEY) String transactionId,
                                                            @RequestHeader("accessToken") String accessToken,
                                                            @RequestHeader("refreshToken") String refreshToken) {


        return CompletableFuture.completedFuture(ResponseData.createResponse(tokenService.checkAccessToken(transactionId, accessToken, refreshToken)));
    }
}
