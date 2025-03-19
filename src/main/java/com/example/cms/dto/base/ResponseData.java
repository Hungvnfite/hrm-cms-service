package com.example.cms.dto.base;

import com.example.cms.common.Constant;
import com.example.cms.common.Utility;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;

import java.util.Map;
import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponseData<T> {
    @NonNull
    private String transactionId;
    @NonNull
    private T result;
    private T data;

    public static <T> ResponseData<T> createResponse(Map<Object, Object> mapData) {
        ResponseData responseData = new ResponseData();
        responseData.setTransactionId(Objects.requireNonNull(Utility.getHeaderParam(Constant.TRANSACTION_ID_KEY)));
        responseData.setResult(mapData.getOrDefault(Constant.RESPONSE_KEY.RESULT, Result.SYSTEM_ERR()));
        responseData.setData(mapData.getOrDefault(Constant.RESPONSE_KEY.DATA, null));
        return responseData;
    }
}
