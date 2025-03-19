package com.example.cms.config;

import com.example.cms.common.BaseResponse;
import com.example.cms.common.Constant;
import com.example.cms.common.ResponseCode;
import com.example.cms.dto.base.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
@Component
@RequiredArgsConstructor
public class CustomAccessDenyHandler implements AccessDeniedHandler {
    private final Logger logger= LogManager.getLogger(CustomAccessDenyHandler.class);
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        response.setStatus(HttpStatus.OK.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setResult(new Result(ResponseCode.AUTHOR_NOT_ALLOW.getCode(), false, ResponseCode.AUTHOR_NOT_ALLOW.getMessage()));
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(response.getWriter(), baseResponse);
        logger.warn("transactionId: {} - xảy ra ngoại lệ khi thực hiện thao tác! Rootcause: {}", request.getHeader(Constant.TRANSACTION_ID_KEY), ResponseCode.AUTHOR_NOT_ALLOW.getMessage());
    }
}
