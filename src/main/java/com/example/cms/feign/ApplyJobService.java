package com.example.cms.feign;

import com.example.cms.common.Constant;
import com.example.cms.config.FeignConfig;
import com.example.cms.dto.base.ResponseData;
import com.example.cms.dto.request.CreateAccountInfoCrmRequest;
import com.example.cms.dto.request.CrmVerifyCreateRequest;
import com.example.cms.dto.request.UpdateApplyJobRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;

@FeignClient(name = "apply-job-client", url = "${spring.vnf_v2.url}")
public interface ApplyJobService {


    @GetMapping(value = "/hiring")
    ResponseData getAllHiringJob(@RequestHeader(Constant.TRANSACTION_ID_KEY) String transactionId,
                                     @RequestParam(value = "pageSize", defaultValue = "100") Integer pageSize,
                                     @RequestParam(value = "pageNum", defaultValue = "0") Integer pageNum,
                                     @RequestParam(value = "industryType", required = false) Integer industryType,
                                     @RequestParam(value = "locationId", required = false) String locationId,
                                     @RequestParam(value = "name", required = false) String name);


    @GetMapping(value = "/apply/{hiringJobId}")
    ResponseData getAllApplyJob(@RequestHeader(Constant.TRANSACTION_ID_KEY) String transactionId,
                                @PathVariable("hiringJobId") String hiringJobId,
                                @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
                                @RequestParam(value = "pageNum", defaultValue = "0") Integer pageNum);

    @GetMapping(value = "/apply/details/{applyJobId}")
    ResponseData getDetailApplyJob(@RequestHeader(Constant.TRANSACTION_ID_KEY) String transactionId,
                                   @PathVariable("applyJobId") String applyJobId);

    @PutMapping(value = "/apply/update/cv-status")
    ResponseData updateApplyStatus(@RequestHeader(Constant.TRANSACTION_ID_KEY) String transactionId,
                                   @RequestBody @Valid UpdateApplyJobRequest request);

    @PutMapping(value = "/apply/update/applicant-review")
    ResponseData applicantReview(@RequestHeader(Constant.TRANSACTION_ID_KEY) String transactionId,
                                   @RequestBody @Valid UpdateApplyJobRequest request);
}