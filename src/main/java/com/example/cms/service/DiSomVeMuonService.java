package com.example.cms.service;

import com.example.cms.common.Constant;
import com.example.cms.common.DateUtil;
import com.example.cms.common.ResponseCode;
import com.example.cms.config.CustomAuthenticationFilter;
import com.example.cms.config.JwtUtil;
import com.example.cms.dao.ChamCong;
import com.example.cms.dao.DiSomVeMuon;
import com.example.cms.dao.UserInfo;
import com.example.cms.dto.base.Result;
import com.example.cms.dto.response.DiSomVeMuonResponse;
import com.example.cms.dto.response.DiSomVeMuonsResponse;
import com.example.cms.repository.*;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DiSomVeMuonService {

    private final Logger logger = LogManager.getLogger(DiSomVeMuonService.class);

    private final DiSomVeMuonRepository diSomVeMuonRepository;
    private final AccountRepository accountRepository;
    private final ChamCongRepository chamCongRepository;
    private final UserInfoRepository userInfoRepository;
    private final PhongBanRepository phongBanRepository;
    private final ManagerRepository managerRepository;
    private final JwtUtil jwtUtil;

    public Map<Object, Object> getAll(String transactionId, String type, Integer pageSize, Integer pageNumber) {
        Map<Object, Object> resultExecute = new HashMap<>();
        Result result = Result.OK();
        try {
            // Kiểm tra token
            if (!jwtUtil.isAdminToken(jwtUtil.getToken(CustomAuthenticationFilter.REQUEST))) {
                result = new Result(ResponseCode.TOKEN_INVALID.getCode(), false, ResponseCode.TOKEN_INVALID.getMessage());
                resultExecute.put(Constant.RESPONSE_KEY.RESULT, result);
                return resultExecute;
            }
            Page<DiSomVeMuon> diSomVeMuons;
            Pageable pageable = PageRequest.of(pageNumber, pageSize);
            diSomVeMuons = diSomVeMuonRepository.findAllByStatusResultLateEarlyAndIsDeleteOrderByCreatedAtDesc(type, false, pageable);
            if (!diSomVeMuons.isEmpty()) {
                List<DiSomVeMuonResponse> diSomVeMuonResponses = diSomVeMuons.getContent().stream()
                        .map(np -> {
                            DiSomVeMuonResponse diSomVeMuonResponse = new DiSomVeMuonResponse();
                            accountRepository.findById(String.valueOf(np.getAccountId())).ifPresent(account -> {
                                UserInfo userInfo = userInfoRepository.findByUsernameAndIsDelete(account.getUsername(), false);
                                if (userInfo != null) {
                                    diSomVeMuonResponse.setId(String.valueOf(np.getId()));
                                    diSomVeMuonResponse.setAccountId(String.valueOf(np.getAccountId()));
                                    if (np.getAccountId() != null) {
                                        accountRepository.findById(String.valueOf(np.getAccountId())).ifPresent(ac -> {
                                            UserInfo userInfo1 = userInfoRepository.findByUsernameAndIsDelete(ac.getUsername(), false);
                                            if (userInfo1 != null) {
                                                phongBanRepository.findById(String.valueOf(userInfo1.getIdPhongBan())).ifPresent(phongBan -> {
                                                    diSomVeMuonResponse.setDepartment(phongBan.getTenPhongBan());
                                                });
                                            }
                                        });
                                    }
                                    diSomVeMuonResponse.setFullName(userInfo.getFullName());
                                    diSomVeMuonResponse.setDateLateEarly(np.getDateLateEarly());
                                    diSomVeMuonResponse.setTimeLateEarly(np.getTimeLateEarly());
                                    diSomVeMuonResponse.setStatusLateEarly(np.getStatusLateEarly());
                                    diSomVeMuonResponse.setStatusResultLateEarly(np.getStatusResultLateEarly());
                                    List<Map<String, String>> managers = new ArrayList<>();
                                    if (np.getManagers() != null) {
                                        np.getManagers().forEach(managerId -> {
                                            managerRepository.findById(String.valueOf(managerId)).ifPresent(mn -> {
                                                Map<String, String> data = new HashMap<>();
                                                data.put("name", mn.getName());
                                                data.put("department", mn.getDepartment());
                                                data.put("position", mn.getPosition());
                                                managers.add(data);
                                            });
                                        });
                                        diSomVeMuonResponse.setManagers(managers);
                                    }
                                    diSomVeMuonResponse.setNotes(np.getNotes());
                                    diSomVeMuonResponse.setCreatedAt(np.getCreatedAt());
                                }
                            });
                            return diSomVeMuonResponse;
                        }).collect(Collectors.toList());
                DiSomVeMuonsResponse pheDuyetsResponse = new DiSomVeMuonsResponse();
                pheDuyetsResponse.setResultData(diSomVeMuonResponses);
                pheDuyetsResponse.setTotalRecords((int) diSomVeMuons.getTotalElements());
                resultExecute.put(Constant.RESPONSE_KEY.DATA, pheDuyetsResponse);
            }
        } catch (Exception ex) {
            logger.error("transactionId: {} - xảy ra ngoại lệ khi thực hiện lấy danh sách phê duyệt đi sớm/về muộn! Rootcause: {}", transactionId, ex);
            result = new Result(ResponseCode.SYSTEM.getCode(), false, ResponseCode.SYSTEM.getMessage());
        }
        resultExecute.put(Constant.RESPONSE_KEY.RESULT, result);
        return resultExecute;
    }

    public Map<Object, Object> update(String transactionId, ObjectId id, String type) {
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
                    ChamCong chamCong = chamCongRepository.findByAccountIdAndIsDeleteAndCreatedAt(dsvm.getAccountId(), false, dsvm.getDateLateEarly());
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
            logger.error("transactionId: {} - Lỗi khi accept/cancel phê duyệt đi sớm/về muộn! Root cause: {}", transactionId, ex);
            result = new Result(ResponseCode.SYSTEM.getCode(), false, ResponseCode.SYSTEM.getMessage());
        }

        resultExecute.put(Constant.RESPONSE_KEY.RESULT, result);
        return resultExecute;
    }
}
