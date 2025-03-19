package com.example.cms.common;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

public class Utility {

    public static String getHeaderParam(String headerKey) {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes == null) return null;
        javax.servlet.http.HttpServletRequest request
                = ((ServletRequestAttributes) attributes).getRequest();
        return request.getHeader(headerKey);
    }

    public static String getSessionId(String... defaults) {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes == null) return null;
        HttpServletRequest request
                = ((ServletRequestAttributes) attributes).getRequest();
        String sessionId = (String) request.getAttribute("sessionId");
        return sessionId == null ? ((defaults == null || defaults.length == 0) ? null : defaults[0]) : sessionId;
    }
}
