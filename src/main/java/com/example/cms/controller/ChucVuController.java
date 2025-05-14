package com.example.cms.controller;

import com.example.cms.common.Constant;
import com.example.cms.dto.base.ResponseData;
import com.example.cms.service.chuc_vu.ChucVuService;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
@CrossOrigin("*")
@RequiredArgsConstructor
@RequestMapping(Constant.ROOT_PATH + "chuc-vu")
public class ChucVuController {

    private final ChucVuService chucVuService;

    @GetMapping(value = "get-all")
    public CompletableFuture<ResponseData> getAll(
            @NonNull @RequestHeader(Constant.TRANSACTION_ID_KEY) String transactionId
    ) {
        return CompletableFuture.completedFuture(ResponseData.createResponse(chucVuService.getAll(transactionId)));
    }

    @PostMapping(value = "create")
    public CompletableFuture<ResponseData> create(
            @NonNull @RequestHeader(Constant.TRANSACTION_ID_KEY) String transactionId,
            @RequestParam("title") String title
    ) {
        return CompletableFuture.completedFuture(ResponseData.createResponse(chucVuService.create(transactionId, title)));
    }

    @GetMapping(value = "{id}")
    public CompletableFuture<ResponseData> detail(
            @NonNull @RequestHeader(Constant.TRANSACTION_ID_KEY) String transactionId,
            @PathVariable("id") ObjectId id
    ) {
        return CompletableFuture.completedFuture(ResponseData.createResponse(chucVuService.detail(transactionId, id)));
    }

    @PutMapping(value = "update/{id}")
    public CompletableFuture<ResponseData> update(
            @NonNull @RequestHeader(Constant.TRANSACTION_ID_KEY) String transactionId,
            @PathVariable("id") ObjectId id,
            @RequestParam("title") String title
    ) {
        return CompletableFuture.completedFuture(ResponseData.createResponse(chucVuService.update(transactionId, id, title)));
    }

    @DeleteMapping(value = "{id}")
    public CompletableFuture<ResponseData> delete(
            @NonNull @RequestHeader(Constant.TRANSACTION_ID_KEY) String transactionId,
            @PathVariable("id") ObjectId id
    ) {
        return CompletableFuture.completedFuture(ResponseData.createResponse(chucVuService.delete(transactionId, id)));
    }

}
