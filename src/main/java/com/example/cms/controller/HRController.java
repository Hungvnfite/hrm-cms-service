package com.example.cms.controller;

import com.example.cms.common.Constant;
import com.example.cms.dto.base.ResponseData;
import com.example.cms.dto.request.MailForgetPasswordRequest;

import com.example.cms.dto.request.UpdateApplyJobRequest;
import com.example.cms.feign.ApplyJobService;
import com.example.cms.service.HRService;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Nullable;
import javax.validation.Valid;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping(Constant.ROOT_PATH + "hr")
@RequiredArgsConstructor
public class HRController {

    private final HRService hrService;
    private final ApplyJobService feignClient;

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

    @GetMapping(value = "/all-hiring")
    public CompletableFuture<ResponseData> getAllHiringJob(
            @RequestParam(value = "pageSize", defaultValue = "100") Integer pageSize,
            @RequestParam(value = "pageNum", defaultValue = "0") Integer pageNum,
            @Nullable @RequestParam(value = "industryType", required = false) Integer industryType,
            @Nullable @RequestParam(value = "locationId", required = false) String locationId,
            @Nullable @RequestParam(value = "name", required = false) String name) {
        return CompletableFuture.completedFuture(feignClient.getAllHiringJob(
                UUID.randomUUID().toString(),
                pageSize,
                pageNum,
                industryType,
                locationId,
                name
        ));
    }

    @GetMapping("/apply-job")
    public CompletableFuture<ResponseData> getAllApplyJob(
            @NonNull @RequestHeader(Constant.TRANSACTION_ID_KEY) String transactionId,
            @RequestParam String hiringJobId) {
        return CompletableFuture.completedFuture(feignClient.getAllApplyJob(transactionId, hiringJobId, 10, 0));
    }

    @GetMapping("/detail-apply-job")
    public CompletableFuture<ResponseData> getDetailApplyJob(
            @NonNull @RequestHeader(Constant.TRANSACTION_ID_KEY) String transactionId,
            @RequestParam String applyJobId) {
        return CompletableFuture.completedFuture(feignClient.getDetailApplyJob(transactionId, applyJobId));
    }

    @PutMapping("/apply-job/update/cv-status")
    public CompletableFuture<ResponseData> updateApplyStatus(
            @NonNull @RequestHeader(Constant.TRANSACTION_ID_KEY) String transactionId,
            @RequestBody @Valid UpdateApplyJobRequest request) {
        return CompletableFuture.completedFuture(feignClient.updateApplyStatus(transactionId, request));
    }

    @PutMapping("/apply-job/applicant-review")
    public CompletableFuture<ResponseData> applicantReview(
            @NonNull @RequestHeader(Constant.TRANSACTION_ID_KEY) String transactionId,
            @RequestBody @Valid UpdateApplyJobRequest request) {
//        return null;
        return CompletableFuture.completedFuture(ResponseData.createResponse(hrService.sendInviteToInterview(transactionId,request)));
    }

}
