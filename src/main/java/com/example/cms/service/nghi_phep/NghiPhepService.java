package com.example.cms.service.nghi_phep;

import com.example.cms.common.Constant;
import com.example.cms.common.DateUtil;
import com.example.cms.common.ResponseCode;
import com.example.cms.config.JwtUtil;
import com.example.cms.dao.Account;
import com.example.cms.dao.NghiPhep;
import com.example.cms.dao.UserInfo;
import com.example.cms.dto.base.Result;
import com.example.cms.dto.model.SessionData;
import com.example.cms.dto.response.NghiPhepResponse;
import com.example.cms.dto.response.NghiPhepsResponse;
import com.example.cms.repository.AccountRepository;
import com.example.cms.repository.ManagerRepository;
import com.example.cms.repository.NghiPhepRepository;
import com.example.cms.repository.PhongBanRepository;
import com.example.cms.repository.UserInfoRepository;
import com.example.cms.service.RedissonService;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NghiPhepService {

    private final Logger logger = LogManager.getLogger(NghiPhepService.class);

    private final RedissonService redissonService;
    private final JwtUtil jwtUtil;
    private final NghiPhepRepository nghiPhepRepository;
    private final AccountRepository accountRepository;
    private final UserInfoRepository userInfoRepository;
    private final HttpServletRequest request;
    private final ManagerRepository managerRepository;
    private final PhongBanRepository phongBanRepository;

    public Map<Object, Object> getAll(String transactionId, Integer type, Integer pageSize, Integer pageNumber) {
        Map<Object, Object> resultExecute = new HashMap<>();
        Result result = Result.OK();
        try {
            // Kiểm tra token
            if (!jwtUtil.isAdminToken(jwtUtil.getToken(request))) {
                result = new Result(ResponseCode.TOKEN_INVALID.getCode(), false, ResponseCode.TOKEN_INVALID.getMessage());
                resultExecute.put(Constant.RESPONSE_KEY.RESULT, result);
                return resultExecute;
            }
            Page<NghiPhep> nghiPheps;
            Pageable pageable = PageRequest.of(pageNumber, pageSize);
            nghiPheps = nghiPhepRepository.findAllByStatusAndIsDeleteOrderByCreatedAtDesc(type, false, pageable);
            if (!nghiPheps.isEmpty()) {
                List<NghiPhepResponse> nghiPhepResponses = nghiPheps.getContent().stream()
                        .map(np -> {
                            NghiPhepResponse nghiPhepResponse = new NghiPhepResponse();
                            accountRepository.findById(String.valueOf(np.getAccountId())).ifPresent(account -> {
                                UserInfo userInfo = userInfoRepository.findByUsernameAndIsDelete(account.getUsername(), false);
                                if (userInfo != null) {
                                    nghiPhepResponse.setId(String.valueOf(np.getId()));
                                    nghiPhepResponse.setAccountId(String.valueOf(np.getAccountId()));
                                    if (np.getAccountId() != null) {
                                        accountRepository.findById(String.valueOf(np.getAccountId())).ifPresent(ac -> {
                                            UserInfo userInfo1 = userInfoRepository.findByUsernameAndIsDelete(ac.getUsername(), false);
                                            if (userInfo1 != null) {
                                                phongBanRepository.findById(String.valueOf(userInfo1.getIdPhongBan())).ifPresent(phongBan -> {
                                                    nghiPhepResponse.setDepartment(phongBan.getTenPhongBan());
                                                });
                                            }
                                        });
                                    }
                                    nghiPhepResponse.setFullName(userInfo.getFullName());
                                    nghiPhepResponse.setStartDate(np.getStartDate());
                                    nghiPhepResponse.setEndDate(np.getEndDate());
                                    nghiPhepResponse.setIsHalfStartDate(np.getIsHalfStartDate());
                                    nghiPhepResponse.setIsHalfEndDate(np.getIsHalfEndDate());
                                    nghiPhepResponse.setLeaveType(np.getLeaveType());
                                    nghiPhepResponse.setIsHalfDay(np.getIsHalfDay());
                                    nghiPhepResponse.setStatus(np.getStatus());
                                    if (np.getReviewer() != null) {
                                        accountRepository.findById(String.valueOf(np.getReviewer())).ifPresent(rv -> {
                                            UserInfo userInfo1 = userInfoRepository.findByUsernameAndIsDelete(rv.getUsername(), false);
                                            if (userInfo1 != null) {
                                                nghiPhepResponse.setReviewer(userInfo1.getFullName());
                                            }
                                        });
                                    }
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
                                        nghiPhepResponse.setManagers(managers);
                                    }
                                    nghiPhepResponse.setNotes(np.getNotes());
                                    nghiPhepResponse.setCreatedAt(np.getCreatedAt());
                                }
                            });
                            return nghiPhepResponse;
                        }).collect(Collectors.toList());
                NghiPhepsResponse nghiPhepsResponse = new NghiPhepsResponse();
                nghiPhepsResponse.setResultData(nghiPhepResponses);
                nghiPhepsResponse.setTotalRecords((int) nghiPheps.getTotalElements());
                resultExecute.put(Constant.RESPONSE_KEY.DATA, nghiPhepsResponse);
            }
        } catch (Exception ex) {
            logger.error("transactionId: {} - xảy ra ngoại lệ khi thực hiện lấy danh sách nghỉ phép! Rootcause: {}", transactionId, ex);
            result = new Result(ResponseCode.SYSTEM.getCode(), false, ResponseCode.SYSTEM.getMessage());
        }
        resultExecute.put(Constant.RESPONSE_KEY.RESULT, result);
        return resultExecute;
    }

    public Map<Object, Object> acceptOrCancelRequest(String transactionId, ObjectId id, String status) {
        Map<Object, Object> resultExecute = new HashMap<>();
        AtomicReference<Result> result = new AtomicReference<>(Result.OK());
        try {
            // Kiểm tra token
            if (!jwtUtil.isAdminToken(jwtUtil.getToken(request))) {
                result.set(new Result(ResponseCode.TOKEN_INVALID.getCode(), false, ResponseCode.TOKEN_INVALID.getMessage()));
                resultExecute.put(Constant.RESPONSE_KEY.RESULT, result);
                return resultExecute;
            }
            if (!status.equalsIgnoreCase("0")) {
                NghiPhep nghiPhep = nghiPhepRepository.findById(String.valueOf(id)).orElse(null);
                if (nghiPhep != null) {
                    Account account = accountRepository.findById(String.valueOf(nghiPhep.getAccountId())).orElse(null);
                    if (account != null) {
                        if (nghiPhep.getStatus() == 0) {
                            UserInfo userInfo = userInfoRepository.findByUsernameAndIsDelete(account.getUsername(), false);
                            if (userInfo != null) {
                                if (Integer.parseInt(status) == 2) {
                                    double countStartDate = 1;
                                    double countEndDate = 1;
                                    if (nghiPhep.getIsHalfStartDate() == 0) {
                                        countStartDate = 0.5;
                                    }
                                    if (nghiPhep.getIsHalfEndDate() == 0) {
                                        countEndDate = 0.5;
                                    }
                                    double countDate = DateUtil.calculateDaysBetweenInclusive(nghiPhep.getStartDate(), nghiPhep.getEndDate()) - 1 + countStartDate + countEndDate;
                                    if (nghiPhep.getLeaveType() == 0) {
                                        if (nghiPhep.getIsHalfDay() == 0) {
                                            userInfo.setNumberOfDaysOffRemaining(userInfo.getNumberOfDaysOffRemaining() + 0.5);
                                        }
                                        if (nghiPhep.getIsHalfDay() == 1) {
                                            userInfo.setNumberOfDaysOffRemaining(userInfo.getNumberOfDaysOffRemaining() + 1);
                                        }
                                        if (nghiPhep.getIsHalfDay() == 2) {
                                            userInfo.setNumberOfDaysOffRemaining(userInfo.getNumberOfDaysOffRemaining() + countDate);
                                        }
                                        userInfoRepository.save(userInfo);
                                    }
                                }
                                String accountIdReviewer = jwtUtil.getSubject(jwtUtil.getToken(request));
                                SessionData sessionData = redissonService.getSession(jwtUtil.getToken(request));
                                if (sessionData != null) {
                                    nghiPhep.setReviewer(new ObjectId(sessionData.getAccountId()));
                                } else {
                                    if (accountIdReviewer != null) {
                                        String cleanId;
                                        if (accountIdReviewer.startsWith("access")) {
                                            cleanId = accountIdReviewer.substring(6);
                                        } else {
                                            cleanId = accountIdReviewer; // Giữ nguyên nếu không bắt đầu bằng "access"
                                        }
                                        nghiPhep.setReviewer(new ObjectId(cleanId));
                                    }
                                }
                                nghiPhep.setStatus(Integer.parseInt(status));
                                nghiPhep.setUpdatedAt(DateUtil.genCreatedAt(null));
                                nghiPhepRepository.save(nghiPhep);
                            }
                        } else {
                            result.set(new Result(ResponseCode.ERROR_CANCEL_LEAVE.getCode(), false, ResponseCode.ERROR_CANCEL_LEAVE.getMessage()));
                            resultExecute.put(Constant.RESPONSE_KEY.RESULT, result);
                            return resultExecute;
                        }
                    }
                }
//                nghiPhepRepository.findById(String.valueOf(id)).ifPresent(nghiPhep -> {
//                    accountRepository.findById(String.valueOf(nghiPhep.getAccountId())).ifPresent(account -> {
//                        UserInfo userInfo = userInfoRepository.findByUsernameAndIsDelete(account.getUsername(), false);
//                        if (userInfo != null) {
//                            if (Integer.parseInt(status) == 2) {
//                                double countStartDate = 1;
//                                double countEndDate = 1;
//                                if (nghiPhep.getIsHalfStartDate() == 0) {
//                                    countStartDate = 0.5;
//                                }
//                                if (nghiPhep.getIsHalfEndDate() == 0) {
//                                    countEndDate = 0.5;
//                                }
//                                double countDate = DateUtil.calculateDaysBetweenInclusive(nghiPhep.getStartDate(), nghiPhep.getEndDate()) - 1 + countStartDate + countEndDate;
//                                if (nghiPhep.getLeaveType() == 0) {
//                                    if (nghiPhep.getIsHalfDay() == 0) {
//                                        userInfo.setNumberOfDaysOffRemaining(userInfo.getNumberOfDaysOffRemaining() + 0.5);
//                                    }
//                                    if (nghiPhep.getIsHalfDay() == 1) {
//                                        userInfo.setNumberOfDaysOffRemaining(userInfo.getNumberOfDaysOffRemaining() + 1);
//                                    }
//                                    if (nghiPhep.getIsHalfDay() == 2) {
//                                        userInfo.setNumberOfDaysOffRemaining(userInfo.getNumberOfDaysOffRemaining() + countDate);
//                                    }
//                                    userInfoRepository.save(userInfo);
//                                }
//                            }
//                            String accountIdReviewer = jwtUtil.getSubject(jwtUtil.getToken(request));
//                            SessionData sessionData = redissonService.getSession(jwtUtil.getToken(request));
//                            if (sessionData != null) {
//                                nghiPhep.setReviewer(new ObjectId(sessionData.getAccountId()));
//                            } else {
//                                if (accountIdReviewer != null) {
//                                    String cleanId;
//                                    if (accountIdReviewer.startsWith("access")) {
//                                        cleanId = accountIdReviewer.substring(6);
//                                    } else {
//                                        cleanId = accountIdReviewer; // Giữ nguyên nếu không bắt đầu bằng "access"
//                                    }
//                                    nghiPhep.setReviewer(new ObjectId(cleanId));
//                                }
//                            }
//                            nghiPhep.setStatus(Integer.parseInt(status));
//                            nghiPhep.setUpdatedAt(DateUtil.genCreatedAt(null));
//                            nghiPhepRepository.save(nghiPhep);
//                        }
//                    });
//                });
            }
        } catch (Exception ex) {
            logger.error("transactionId: {} - xảy ra ngoại lệ khi thực hiện lấy danh sách nghỉ phép! Rootcause: {}", transactionId, ex);
            result.set(new Result(ResponseCode.SYSTEM.getCode(), false, ResponseCode.SYSTEM.getMessage()));
        }
        resultExecute.put(Constant.RESPONSE_KEY.RESULT, result);
        return resultExecute;
    }

}
