package com.example.cms.common;

import com.example.cms.dto.base.Result;
import lombok.Data;

@Data
public class BaseResponse {
    Result result;
    Object data;
}
