package com.example.cms.service.trang_thai_lam_viec;

import com.example.cms.common.Constant;
import com.example.cms.common.ResponseCode;
import com.example.cms.config.JwtUtil;
import com.example.cms.dao.TrangThaiLamViec;
import com.example.cms.dto.base.Result;
import com.example.cms.dto.response.TrangThaiLamViecResponse;
import com.example.cms.repository.TrangThaiLamViecRepository;
import com.example.cms.service.chuc_vu.ChucVuService;
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
public class TrangThaiLamViecService {

    private final Logger logger = LogManager.getLogger(ChucVuService.class);

    private final TrangThaiLamViecRepository trangThaiLamViecRepository;
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
            List<TrangThaiLamViec> trangThaiLamViecs = trangThaiLamViecRepository.findAll();
            List<TrangThaiLamViecResponse> trangThaiLamViecResponses = new ArrayList<>();
            for (TrangThaiLamViec trangThaiLamViec : trangThaiLamViecs) {
                TrangThaiLamViecResponse trangThaiLamViecResponse = new TrangThaiLamViecResponse();
                trangThaiLamViecResponse.setId(String.valueOf(trangThaiLamViec.getId()));
                trangThaiLamViecResponse.setTenTrangThai(trangThaiLamViec.getTenTrangThai());
                trangThaiLamViecResponses.add(trangThaiLamViecResponse);
            }
            resultExecute.put(Constant.RESPONSE_KEY.DATA, trangThaiLamViecResponses);
        } catch (Exception ex) {
            logger.error("transactionId: {} - Error getting all TrangThaiLamViec! Rootcause: {}", transactionId, ex);
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
            TrangThaiLamViec trangThaiLamViec = new TrangThaiLamViec();
            trangThaiLamViec.setTenTrangThai(title);
            trangThaiLamViecRepository.save(trangThaiLamViec);
            TrangThaiLamViecResponse trangThaiLamViecResponse = new TrangThaiLamViecResponse();
            trangThaiLamViecResponse.setId(String.valueOf(trangThaiLamViec.getId()));
            trangThaiLamViecResponse.setTenTrangThai(title);
            resultExecute.put(Constant.RESPONSE_KEY.DATA, trangThaiLamViecResponse);
        } catch (Exception ex) {
            logger.error("transactionId: {} - Error getting all ChucVu! Rootcause: {}", transactionId, ex);
            result = new Result(ResponseCode.SYSTEM.getCode(), false, ResponseCode.SYSTEM.getMessage());
        }
        resultExecute.put(Constant.RESPONSE_KEY.RESULT, result);
        return resultExecute;
    }
}
