package com.example.cms.controller;

import com.example.cms.common.Constant;
import com.example.cms.dto.base.ResponseData;
import com.example.cms.dto.request.LoginRequest;
import com.example.cms.dto.request.ResetPasswordRequest;
import com.example.cms.service.auth.AuthService;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping(Constant.ROOT_PATH)
@RequiredArgsConstructor
@CrossOrigin("*")
public class AuthController {

    private final AuthService authService;

    @PostMapping(value = "auth")
    public CompletableFuture<ResponseData> doLogin(@NonNull @RequestHeader(Constant.TRANSACTION_ID_KEY) String transactionId,
                                                   @RequestBody LoginRequest request) {

        return CompletableFuture.completedFuture(ResponseData.createResponse(authService.login(transactionId, request)));
    }

    @PutMapping(value = "reset-password")
    public CompletableFuture<ResponseData> resetPassword(@NonNull @RequestHeader(Constant.TRANSACTION_ID_KEY) String transactionId,
                                                         @PathVariable("id") ObjectId id) {

        return CompletableFuture.completedFuture(ResponseData.createResponse(authService.checkResetPassword(transactionId, id)));
    }
}
