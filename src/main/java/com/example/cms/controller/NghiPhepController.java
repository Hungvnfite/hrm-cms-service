package com.example.cms.controller;

import com.example.cms.common.Constant;
import com.example.cms.dto.base.ResponseData;
import com.example.cms.service.nghi_phep.NghiPhepService;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping(Constant.ROOT_PATH + "nghi-phep")
@RequiredArgsConstructor
public class NghiPhepController {

    private final NghiPhepService nghiPhepService;

    @GetMapping("get-all")
    public CompletableFuture<ResponseData> getAll(
            @NonNull @RequestHeader(Constant.TRANSACTION_ID_KEY) String transactionId,
            @RequestParam("type") Integer type,
            @RequestParam(value = "pageSize", defaultValue = "20") Integer pageSize,
            @RequestParam(value = "pageNumber", defaultValue = "0") Integer pageNumber
    ) {
        return CompletableFuture.completedFuture(ResponseData.createResponse(nghiPhepService.getAll(transactionId, type, pageSize, pageNumber)));
    }

    @PutMapping("update/{id}")
    public CompletableFuture<ResponseData> acceptOrCancelRequest(
            @NonNull @RequestHeader(Constant.TRANSACTION_ID_KEY) String transactionId,
            @PathVariable("id") ObjectId id,
            @RequestParam("status") String status
    ) {
        return CompletableFuture.completedFuture(ResponseData.createResponse(nghiPhepService.acceptOrCancelRequest(transactionId, id, status)));
    }

}
