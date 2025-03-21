package com.example.cms.service.log;

import com.example.cms.common.Utility;
import com.google.gson.Gson;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Enumeration;

@Service
@Slf4j
public class LoggingService {
    private static final String REQUEST_ID = "transactionId";

    public static String parseObjectToString(Object object) {
        return new Gson().toJson(object);
    }

    public static <T> T parseStringToObject(String json, Class<T> classObject) {
        try {
            return new Gson().fromJson(json, classObject);
        } catch (Exception e) {
            return null;
        }
    }

    public void logRequest(HttpServletRequest httpServletRequest, Object body) {
        if (httpServletRequest.getRequestURI().contains("medias")) {
            return;
        }
        Object requestId = Utility.getHeaderParam(REQUEST_ID);
        StringBuilder data = new StringBuilder();
        data.append("\nREQUEST START-----------------------------------\n")
                .append("[REQUEST-ID]: ").append(requestId).append("\n")
                .append("[PATH]: ").append(httpServletRequest.getRequestURI()).append("\n");

        Enumeration headerNames = httpServletRequest.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String key = (String) headerNames.nextElement();
            String value = httpServletRequest.getHeader(key);
            data.append("---").append(key).append(" : ").append(value).append("\n");
        }
        data.append("[BODY REQUEST]: ").append("\n")
                .append(parseObjectToString(body))
                .append("\n")
                .append("REQUEST END-----------------------------------\n");

        log.info(data.toString());
    }

    public void logResponse(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object body) {
        if (httpServletRequest.getRequestURI().contains("medias")) {
            return;
        }
        Object requestId = Utility.getHeaderParam(REQUEST_ID);
        StringBuilder data = new StringBuilder();
        data.append("\nRESPONSE START-----------------------------------\n")
                .append("[REQUEST-ID]: ").append(requestId).append("\n")
                .append("[BODY RESPONSE]: ").append("\n")
                .append(parseObjectToString(body))
                .append("\n")
                .append("RESPONSE END-----------------------------------\n");

        log.info(data.toString());
    }
}
