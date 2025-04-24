package com.example.cms.controller;

import com.example.cms.common.Constant;
import com.example.cms.dto.base.ResponseData;
import com.example.cms.service.cham_cong.ChamCongService;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping(Constant.ROOT_PATH)
@RequiredArgsConstructor
@CrossOrigin("*")
public class ChamCongController {

    private final ChamCongService chamCongService;

    @GetMapping(value = "cham-cong/list/{id}")
    public CompletableFuture<ResponseData> getList(
            @NonNull @RequestHeader(Constant.TRANSACTION_ID_KEY) String transactionId,
            @Nullable @PathVariable("id") ObjectId id,
            @Nullable @RequestParam("month") String month,
            @Nullable @RequestParam("year") String year
    ) {
        return CompletableFuture.completedFuture(ResponseData.createResponse(chamCongService.getList(transactionId, id, month, year)));
    }
}
