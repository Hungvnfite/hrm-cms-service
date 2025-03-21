package com.example.cms.controller;

import com.example.cms.common.Constant;
import com.example.cms.dto.base.ResponseData;
import com.example.cms.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping(Constant.ROOT_PATH)
@RequiredArgsConstructor
@CrossOrigin("*")
public class AccountController {

    private final AccountService accountService;

    @PostMapping(value = "account/create")
    public CompletableFuture<ResponseData> createAccount(
            @NonNull @RequestHeader(Constant.TRANSACTION_ID_KEY) String transactionId,
            @RequestPart("userRequest") String request,
            @RequestPart("avatar") MultipartFile avatar,
            @RequestPart("files") List<MultipartFile> files
    ) {
        return CompletableFuture.completedFuture(ResponseData.createResponse(accountService.createAccount(transactionId, request, avatar, files)));
    }

    @PutMapping(value = "account/update/{accountId}")
    public CompletableFuture<ResponseData> updateAccount(
            @NonNull @RequestHeader(Constant.TRANSACTION_ID_KEY) String transactionId,
            @PathVariable("accountId") ObjectId accountId,
            @RequestPart("userRequest") String request,
            @Nullable @RequestPart("avatar") MultipartFile avatar,
            @Nullable @RequestPart("files") List<MultipartFile> files
    ) {
        return CompletableFuture.completedFuture(ResponseData.createResponse(accountService.updateAccount(transactionId, accountId, request, avatar, files)));
    }

    @GetMapping(value = "account/info-web/{accountId}")
    public CompletableFuture<ResponseData> getInfoWeb(
            @NonNull @RequestHeader(Constant.TRANSACTION_ID_KEY) String transactionId,
            @PathVariable("accountId") String accountId
    ) {
        return CompletableFuture.completedFuture(ResponseData.createResponse(accountService.getInfoWeb(transactionId, accountId)));
    }

    @GetMapping(value = "account/list")
    public CompletableFuture<ResponseData> getList(
            @NonNull @RequestHeader(Constant.TRANSACTION_ID_KEY) String transactionId,
            @Nullable @RequestHeader("tokenCRM") String token,
            @RequestParam(value = "pageSize", defaultValue = "20") Integer pageSize,
            @RequestParam(value = "pageNumber", defaultValue = "0") Integer pageNumber,
            @RequestParam(value = "isDelete") Boolean isDelete,
            @Nullable @RequestParam(value = "maNv") String maNv,
            @Nullable @RequestParam(value = "fullName") String fullName,
            @Nullable @RequestParam(value = "soCCCD") String soCCCD,
            @Nullable @RequestParam(value = "gender") Boolean gender,
            @Nullable @RequestParam(value = "phone") String phone,
            @Nullable @RequestParam(value = "email") String email,
            @Nullable @RequestParam(value = "phongBan") ObjectId phongBan,
            @Nullable @RequestParam(value = "chucVu") ObjectId chucVu,
            @Nullable @RequestParam(value = "fromDate") @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate fromDate,
            @Nullable @RequestParam(value = "toDate") @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate toDate
    ) {
        return CompletableFuture.completedFuture(ResponseData.createResponse(accountService.getList(transactionId, pageSize, pageNumber, token, isDelete, maNv, fullName, soCCCD, gender, phone, email, phongBan, chucVu, fromDate, toDate)));
    }

}
