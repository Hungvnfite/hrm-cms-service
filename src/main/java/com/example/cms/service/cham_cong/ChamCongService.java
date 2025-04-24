package com.example.cms.service.cham_cong;

import com.example.cms.common.Constant;
import com.example.cms.common.ResponseCode;
import com.example.cms.config.CustomAuthenticationFilter;
import com.example.cms.config.JwtUtil;
import com.example.cms.dao.Account;
import com.example.cms.dao.ChamCong;
import com.example.cms.dao.UserInfo;
import com.example.cms.dto.base.Result;
import com.example.cms.dto.response.ChamCongResponse;
import com.example.cms.repository.AccountRepository;
import com.example.cms.repository.ChamCongRepository;
import com.example.cms.repository.UserInfoRepository;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ChamCongService {

    private final Logger logger = LogManager.getLogger(ChamCongService.class);

    private final ChamCongRepository chamCongRepository;
    private final AccountRepository accountRepository;
    private final UserInfoRepository userInfoRepository;
    private final JwtUtil jwtUtil;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    public Map<Object, Object> getList(String transactionId, ObjectId id, String month, String year) {
        Map<Object, Object> resultExecute = new HashMap<>();
        Result result = Result.OK();
        try {
            // Kiểm tra token
            if (!jwtUtil.isAdminToken(jwtUtil.getToken(CustomAuthenticationFilter.REQUEST))) {
                result = new Result(ResponseCode.TOKEN_INVALID.getCode(), false, ResponseCode.TOKEN_INVALID.getMessage());
                resultExecute.put(Constant.RESPONSE_KEY.RESULT, result);
                return resultExecute;
            }
            YearMonth yearMonth = YearMonth.of(Integer.parseInt(year), Integer.parseInt(month));
            LocalDateTime startOfMonth = yearMonth.atDay(1).atStartOfDay();
            LocalDateTime endOfMonth = yearMonth.atEndOfMonth().atTime(23, 59, 59);
            // Định dạng thành "Fri Mar 21 09:59:29 ICT 2025"
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");

            String startOfMonthStr = startOfMonth.format(formatter);
            String endOfMonthStr = endOfMonth.format(formatter);
            List<ChamCong> chamCongs = chamCongRepository.findAllByAccountIdAndIsDeleteAndCreatedAtBetweenOrderByCreatedAtAsc(id, false, startOfMonthStr, endOfMonthStr);
            List<ChamCongResponse> chamCongResponses = new ArrayList<>();
            for (ChamCong chamCong : chamCongs) {
                ChamCongResponse chamCongResponse = new ChamCongResponse();
                chamCongResponse.setId(String.valueOf(chamCong.getId()));
                Account account = accountRepository.findById(String.valueOf(chamCong.getAccountId())).orElse(null);
                if (account != null) {
                    UserInfo userInfo = userInfoRepository.findByUsernameAndIsDelete(account.getUsername(), false);
                    if (userInfo != null) {
                        chamCongResponse.setNameUser(userInfo.getFullName());
                    }
                }
                chamCongResponse.setCheckinTime(chamCong.getCheckinTime());
                chamCongResponse.setCheckoutTime(chamCong.getCheckoutTime());
                chamCongResponse.setStatus(chamCong.getStatus());
                chamCongResponse.setStatusDetail(chamCong.getStatusDetail());
                chamCongResponse.setStatusDetailOut(chamCong.getStatusDetailOut());
                chamCongResponse.setPeriodCheckin(chamCong.getPeriodIn());
                chamCongResponse.setPeriodCheckout(chamCong.getPeriodOut());
                chamCongResponse.setCreatedAt(chamCong.getCreatedAt());
                chamCongResponses.add(chamCongResponse);
            }
            resultExecute.put(Constant.RESPONSE_KEY.DATA, chamCongResponses);
        } catch (Exception ex) {
            logger.error("transactionId: {} - xảy ra ngoại lệ khi thực hiện thêm mới người dùng! Rootcause: {}", transactionId, ex);
            result = new Result(ResponseCode.SYSTEM.getCode(), false, ResponseCode.SYSTEM.getMessage());
        }
        resultExecute.put(Constant.RESPONSE_KEY.RESULT, result);
        return resultExecute;
    }

}
