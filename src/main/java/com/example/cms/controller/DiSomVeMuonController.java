package com.example.cms.controller;

import com.example.cms.common.Constant;
import com.example.cms.dto.base.ResponseData;
import com.example.cms.service.DiSomVeMuonService;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping(Constant.ROOT_PATH + "late-early")
@RequiredArgsConstructor
@CrossOrigin("*")
public class DiSomVeMuonController {

    private final DiSomVeMuonService diSomVeMuonService;

    @GetMapping("get-all")
    public CompletableFuture<ResponseData> getAll(
            @NonNull @RequestHeader(Constant.TRANSACTION_ID_KEY) String transactionId,
            @RequestParam("type") String type,
            @RequestParam(value = "pageSize", defaultValue = "20") Integer pageSize,
            @RequestParam(value = "pageNumber", defaultValue = "0") Integer pageNumber
    ) {
        return CompletableFuture.completedFuture(ResponseData.createResponse(diSomVeMuonService.getAll(transactionId, type, pageSize, pageNumber)));
    }

    @PutMapping(value = "/update/{id}")
    public CompletableFuture<ResponseData> accept(
            @NonNull @RequestHeader(Constant.TRANSACTION_ID_KEY) String transactionId,
            @PathVariable("id") ObjectId id,
            @RequestParam("type") String type)
    {
        return CompletableFuture.completedFuture(ResponseData.createResponse(diSomVeMuonService.update(transactionId, id, type)));
    }


}
