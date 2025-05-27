package com.example.cms.service;

import com.example.cms.common.Constant;
import com.example.cms.common.DateUtil;
import com.example.cms.common.ResponseCode;
import com.example.cms.config.CustomAuthenticationFilter;
import com.example.cms.config.JwtUtil;
import com.example.cms.dao.ChamCong;
import com.example.cms.dto.base.Result;
import com.example.cms.repository.ChamCongRepository;
import com.example.cms.repository.DiSomVeMuonRepository;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DiSomVeMuonService {

    private final Logger logger = LogManager.getLogger(DiSomVeMuonService.class);

    private final DiSomVeMuonRepository diSomVeMuonRepository;
    private final JwtUtil jwtUtil;
    private final ChamCongRepository chamCongRepository;

    public Map<Object, Object> accept(String transactionId, ObjectId id, String type) {
        Map<Object, Object> resultExecute = new HashMap<>();
        Result result = Result.OK();
        try {
            // Kiểm tra token
            if (!jwtUtil.isAdminToken(jwtUtil.getToken(CustomAuthenticationFilter.REQUEST))) {
                result = new Result(ResponseCode.TOKEN_INVALID.getCode(), false, ResponseCode.TOKEN_INVALID.getMessage());
                resultExecute.put(Constant.RESPONSE_KEY.RESULT, result);
                return resultExecute;
            }
            diSomVeMuonRepository.findById(String.valueOf(id)).ifPresent(dsvm -> {
                if (!type.equalsIgnoreCase("0") && dsvm.getStatusResultLateEarly().equalsIgnoreCase("0")) {
                    ChamCong chamCong = chamCongRepository.findByAccountIdAndIsDeleteAndCreatedAt(dsvm.getAccountId(), false, dsvm.getCreatedAt());
                    if (chamCong != null) {
                        if (type.equalsIgnoreCase("1")) {
                            if (chamCong.getStatusLateEarly().equalsIgnoreCase("3") || chamCong.getStatusLateEarly().equalsIgnoreCase("4")) {
                                chamCong.setStatusLateEarly("0");
                            }
                            if (chamCong.getStatusLateEarly().equalsIgnoreCase("1")) {
                                if (dsvm.getStatusLateEarly().equalsIgnoreCase("1")) {
                                    chamCong.setStatusLateEarly("3");
                                }
                                if (dsvm.getStatusLateEarly().equalsIgnoreCase("2")) {
                                    chamCong.setStatusLateEarly("4");
                                }
                            }
                            if (chamCong.getStatusLateEarly().equalsIgnoreCase("5")) {
                                if (dsvm.getStatusLateEarly().equalsIgnoreCase("2")) {
                                    chamCong.setStatusLateEarly("8");
                                }
                            }
                            if (chamCong.getStatusLateEarly().equalsIgnoreCase("6")) {
                                if (dsvm.getStatusLateEarly().equalsIgnoreCase("1")) {
                                    chamCong.setStatusLateEarly("7");
                                }
                            }
                        }
                        if (type.equalsIgnoreCase("2")) {
                            if (chamCong.getStatusLateEarly().equalsIgnoreCase("3")) {
                                chamCong.setStatusLateEarly("7");
                            } else if (chamCong.getStatusLateEarly().equalsIgnoreCase("4")) {
                                chamCong.setStatusLateEarly("8");
                            } else if (chamCong.getStatusLateEarly().equalsIgnoreCase("1")) {
                                if (dsvm.getStatusLateEarly().equalsIgnoreCase("1")) {
                                    chamCong.setStatusLateEarly("5");
                                }
                                if (dsvm.getStatusLateEarly().equalsIgnoreCase("2")) {
                                    chamCong.setStatusLateEarly("6");
                                }
                            } else if (chamCong.getStatusLateEarly().equalsIgnoreCase("5") || chamCong.getStatusLateEarly().equalsIgnoreCase("6")) {
                                chamCong.setStatusLateEarly("1");
                            }
                        }
                        chamCongRepository.save(chamCong);
                    }
                    dsvm.setStatusResultLateEarly(type);
                    dsvm.setUpdatedAt(DateUtil.genCreatedAt(null));
                    diSomVeMuonRepository.save(dsvm);
                    resultExecute.put(Constant.RESPONSE_KEY.DATA, dsvm);
                }
            });
        } catch (Exception ex) {
            logger.error("transactionId: {} - Lỗi khi lấy thông tin tài khoản! Root cause: {}", transactionId, ex);
            result = new Result(ResponseCode.SYSTEM.getCode(), false, ResponseCode.SYSTEM.getMessage());
        }

        resultExecute.put(Constant.RESPONSE_KEY.RESULT, result);
        return resultExecute;
    }
}
