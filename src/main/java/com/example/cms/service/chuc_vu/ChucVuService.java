package com.example.cms.service.chuc_vu;

import com.example.cms.common.Constant;
import com.example.cms.common.ResponseCode;
import com.example.cms.common.Utility;
import com.example.cms.config.JwtUtil;
import com.example.cms.dao.ChucVu;
import com.example.cms.dto.base.Result;
import com.example.cms.dto.response.ChucVuResponse;
import com.example.cms.repository.ChucVuRepository;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ChucVuService {

    private final Logger logger = LogManager.getLogger(ChucVuService.class);

    private final ChucVuRepository chucVuRepository;
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
            List<ChucVu> chucVus = chucVuRepository.findAll();
            List<ChucVuResponse> chucVuResponses = new ArrayList<>();
            for (ChucVu chucVu : chucVus) {
                ChucVuResponse chucVuResponse = new ChucVuResponse();
                chucVuResponse.setId(String.valueOf(chucVu.getId()));
                chucVuResponse.setTenChucVu(chucVu.getTenChucVu());
                chucVuResponses.add(chucVuResponse);
            }
            resultExecute.put(Constant.RESPONSE_KEY.DATA, chucVuResponses);
        } catch (Exception ex) {
            logger.error("transactionId: {} - Error getting all ChucVu! Rootcause: {}", transactionId, ex);
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
            ChucVu chucVu = new ChucVu();
            chucVu.setTenChucVu(title);
            chucVuRepository.save(chucVu);
            ChucVuResponse chucVuResponse = new ChucVuResponse();
            chucVuResponse.setId(String.valueOf(chucVu.getId()));
            chucVuResponse.setTenChucVu(title);
            resultExecute.put(Constant.RESPONSE_KEY.DATA, chucVuResponse);
        } catch (Exception ex) {
            logger.error("transactionId: {} - Error getting all ChucVu! Rootcause: {}", transactionId, ex);
            result = new Result(ResponseCode.SYSTEM.getCode(), false, ResponseCode.SYSTEM.getMessage());
        }
        resultExecute.put(Constant.RESPONSE_KEY.RESULT, result);
        return resultExecute;
    }

}
