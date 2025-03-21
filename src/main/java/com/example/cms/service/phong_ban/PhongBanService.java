package com.example.cms.service.phong_ban;

import com.example.cms.common.Constant;
import com.example.cms.common.ResponseCode;
import com.example.cms.common.Utility;
import com.example.cms.config.JwtUtil;
import com.example.cms.dao.PhongBan;
import com.example.cms.dto.base.Result;
import com.example.cms.dto.response.PhongBanResponse;
import com.example.cms.repository.PhongBanRepository;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PhongBanService {

    private final Logger logger = LogManager.getLogger(PhongBanService.class);

    private final PhongBanRepository phongBanRepository;
    private final JwtUtil jwtUtil;
    private final HttpServletRequest request;

    public Map<Object, Object> getAll(String transactionId) {
        Map<Object, Object> resultExecute = new HashMap<>();
        Result result = Result.OK();
        try {
            // Kiểm tra token
            if (!jwtUtil.isAdminToken(jwtUtil.getToken(request))) {
                result = new Result(ResponseCode.TOKEN_INVALID.getCode(), false, ResponseCode.TOKEN_INVALID.getMessage());
                resultExecute.put(Constant.RESPONSE_KEY.RESULT, result);
                return resultExecute;
            }
            List<PhongBan> phongBans = phongBanRepository.findAll();
            List<PhongBanResponse> phongBanResponses = new ArrayList<>();
            for (PhongBan phongBan : phongBans) {
                PhongBanResponse phongBanResponse = new PhongBanResponse();
                phongBanResponse.setId(String.valueOf(phongBan.getId()));
                phongBanResponse.setTenPhongBan(phongBan.getTenPhongBan());
                phongBanResponses.add(phongBanResponse);
            }
            resultExecute.put(Constant.RESPONSE_KEY.DATA, phongBanResponses);
        } catch (Exception ex) {
            logger.error("transactionId: {} - Error getting all PhongBan! Rootcause: {}", transactionId, ex);
            result = new Result(ResponseCode.SYSTEM.getCode(), false, ResponseCode.SYSTEM.getMessage());
        }
        resultExecute.put(Constant.RESPONSE_KEY.RESULT, result);
        return resultExecute;
    }

    public Map<Object, Object> create(String transactionId, String title) {
        Map<Object, Object> resultExecute = new HashMap<>();
        Result result = Result.OK();
        try {
            // Kiểm tra token
            if (!jwtUtil.isAdminToken(jwtUtil.getToken(request))) {
                result = new Result(ResponseCode.TOKEN_INVALID.getCode(), false, ResponseCode.TOKEN_INVALID.getMessage());
                resultExecute.put(Constant.RESPONSE_KEY.RESULT, result);
                return resultExecute;
            }
            PhongBan phongBan = new PhongBan();
            phongBan.setTenPhongBan(title);
            phongBanRepository.save(phongBan);
            PhongBanResponse phongBanResponse = new PhongBanResponse();
            phongBanResponse.setId(String.valueOf(phongBan.getId()));
            phongBanResponse.setTenPhongBan(title);
            resultExecute.put(Constant.RESPONSE_KEY.DATA, phongBanResponse);
        } catch (Exception ex) {
            logger.error("transactionId: {} - Error getting all PhongBan! Rootcause: {}", transactionId, ex);
            result = new Result(ResponseCode.SYSTEM.getCode(), false, ResponseCode.SYSTEM.getMessage());
        }
        resultExecute.put(Constant.RESPONSE_KEY.RESULT, result);
        return resultExecute;
    }

}
