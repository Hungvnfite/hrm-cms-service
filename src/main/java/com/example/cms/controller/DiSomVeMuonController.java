package com.example.cms.controller;

import com.example.cms.common.Constant;
import com.example.cms.dto.base.ResponseData;
import com.example.cms.service.DiSomVeMuonService;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping(Constant.ROOT_PATH + "go-late-and-leave-early")
@RequiredArgsConstructor
@CrossOrigin("*")
public class DiSomVeMuonController {

    private final DiSomVeMuonService diSomVeMuonService;

    @PutMapping(value = "accept/{id}")
    public CompletableFuture<ResponseData> accept(@NonNull @RequestHeader(Constant.TRANSACTION_ID_KEY) String transactionId,
                                                  @PathVariable("id") ObjectId id,
                                                  @RequestParam("type") String type) {

        return CompletableFuture.completedFuture(ResponseData.createResponse(diSomVeMuonService.accept(transactionId, id, type)));
    }
}
