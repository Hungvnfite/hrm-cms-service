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
import java.time.LocalTime;
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

            String startOfMonthStr = startOfMonth.minusDays(1).format(formatter);
            String endOfMonthStr = endOfMonth.plusDays(1).format(formatter);
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

    public Map<Object, Object> sum(String transactionId, ObjectId id, String month, String year) {
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
            DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("HH:mm");

            String startOfMonthStr = startOfMonth.minusDays(1).format(formatter);
            String endOfMonthStr = endOfMonth.plusDays(1).format(formatter);
            List<ChamCong> chamCongs = chamCongRepository.findAllByAccountIdAndIsDeleteAndCreatedAtBetweenOrderByCreatedAtAsc(id, false, startOfMonthStr, endOfMonthStr);
            Double sum = 0.0;
            Double tongQuenChamCong = 0.0;
            Double congDuocTinhKhiQuenCham = 0.0;
            Double tienDiMuonVeSom = 0.0;
            for (ChamCong chamCong : chamCongs) {
                // chuẩn
                if (chamCong.getStatus().equalsIgnoreCase("0") && chamCong.getStatusDetail().equalsIgnoreCase("0") && chamCong.getStatusDetailOut().equalsIgnoreCase("0")) {
                    sum++;
                }
                if (chamCong.getStatusDetail().equalsIgnoreCase("1") || chamCong.getStatusDetailOut().equalsIgnoreCase("1")) {
                    tongQuenChamCong++;
                }
                Double soTienMuonSom = 0.0;
                if (chamCong.getStatusLateEarly().equalsIgnoreCase("1")) {
                    if (chamCong.getCheckoutTime() != null) {
                        LocalTime checkInTime = LocalTime.parse(chamCong.getCheckoutTime().trim(), formatter1);
                        if (chamCong.getPeriodOut() != null) {
                            if (checkInTime.isBefore(LocalTime.parse("17:00", formatter1))) {
                                Double soPhutDiMuon = convertTimeToMinutes(chamCong.getPeriodOut().trim());
                                soTienMuonSom = soTienMuonSom + soPhutDiMuon;
                            }
                        }
                    }
                    tienDiMuonVeSom += soTienMuonSom;
                }
                if (chamCong.getStatusLateEarly().equalsIgnoreCase("2")){
                    if (chamCong.getCheckinTime() != null) {
                        LocalTime checkInTime = LocalTime.parse(chamCong.getCheckinTime().trim(), formatter1);
                        if (chamCong.getPeriodIn() != null) {
                            Double soPhutDiMuon = convertTimeToMinutes(chamCong.getPeriodIn().trim());
                            if (checkInTime.isAfter(LocalTime.parse("08:05", formatter1)) && checkInTime.isBefore(LocalTime.parse("08:16", formatter1))) {
                                soTienMuonSom = soPhutDiMuon;
                            }
                            if (checkInTime.isAfter(LocalTime.parse("08:15", formatter1)) && checkInTime.isBefore(LocalTime.parse("08:31", formatter1))) {
                                soTienMuonSom = ((soPhutDiMuon - 10) * 5) + 10;
                            }
                            if (checkInTime.isAfter(LocalTime.parse("08:30", formatter1)) && checkInTime.isBefore(LocalTime.parse("09:01", formatter1))) {
                                soTienMuonSom = ((soPhutDiMuon - 25) * 10) + 85;
                            }
                        }
                    }
                    tienDiMuonVeSom += soTienMuonSom;
                }
                if (chamCong.getStatusLateEarly().equalsIgnoreCase("0")){
                    if (chamCong.getCheckinTime() != null) {
                        LocalTime checkInTime = LocalTime.parse(chamCong.getCheckinTime().trim(), formatter1);
                        if (chamCong.getPeriodIn() != null) {
                            Double soPhutDiMuon = convertTimeToMinutes(chamCong.getPeriodIn().trim());
                            if (checkInTime.isAfter(LocalTime.parse("08:05", formatter1)) && checkInTime.isBefore(LocalTime.parse("08:16", formatter1))) {
                                soTienMuonSom = soPhutDiMuon;
                            }
                            if (checkInTime.isAfter(LocalTime.parse("08:15", formatter1)) && checkInTime.isBefore(LocalTime.parse("08:31", formatter1))) {
                                soTienMuonSom = ((soPhutDiMuon - 10) * 5) + 10;
                            }
                            if (checkInTime.isAfter(LocalTime.parse("08:30", formatter1)) && checkInTime.isBefore(LocalTime.parse("09:01", formatter1))) {
                                soTienMuonSom = ((soPhutDiMuon - 25) * 10) + 85;
                            }
                        }
                    }
                    if (chamCong.getCheckoutTime() != null) {
                        LocalTime checkInTime = LocalTime.parse(chamCong.getCheckoutTime().trim(), formatter1);
                        if (chamCong.getPeriodOut() != null) {
                            if (checkInTime.isBefore(LocalTime.parse("17:00", formatter1))) {
                                Double soPhutDiMuon = convertTimeToMinutes(chamCong.getPeriodOut().trim());
                                soTienMuonSom = soTienMuonSom + soPhutDiMuon;
                            }
                        }
                    }
                    tienDiMuonVeSom += soTienMuonSom;
                }
//                // quên chấm công về
//                if (chamCong.getStatus().equalsIgnoreCase("2") && chamCong.getStatusDetail().equalsIgnoreCase("0") && chamCong.getStatusDetailOut().equalsIgnoreCase("2")) {
//                    sum += 1;
//                }
//                // quên chấm công về + đi muộn
//                if (chamCong.getStatus().equalsIgnoreCase("2") && chamCong.getStatusDetail().equalsIgnoreCase("1") && chamCong.getStatusDetailOut().equalsIgnoreCase("2")) {
//                    sum += 1;
//                }
//                // quên chấm công đi
//                if (chamCong.getStatus().equalsIgnoreCase("1") && chamCong.getStatusDetail().equalsIgnoreCase("2") && chamCong.getStatusDetailOut().equalsIgnoreCase("0")) {
//                    sum += 1;
//                }
//                // quên chấm công đi + về sớm
//                if (chamCong.getStatus().equalsIgnoreCase("1") && chamCong.getStatusDetail().equalsIgnoreCase("2") && chamCong.getStatusDetailOut().equalsIgnoreCase("1")) {
//                    sum += 1;
//                }
//                // đi muộn
//                if (chamCong.getStatus().equalsIgnoreCase("0") && chamCong.getStatusDetail().equalsIgnoreCase("1") && chamCong.getStatusDetailOut().equalsIgnoreCase("0")) {
//                    sum += 1;
//                }
//                // về sớm
//                if (chamCong.getStatus().equalsIgnoreCase("0") && chamCong.getStatusDetail().equalsIgnoreCase("0") && chamCong.getStatusDetailOut().equalsIgnoreCase("1")) {
//                    sum += 1;
//                }
            }
            if (tongQuenChamCong >= 3) {
                congDuocTinhKhiQuenCham = (tongQuenChamCong - 3) / 2;
                sum = sum + tongQuenChamCong;
            }
            Map<Object, Object> data = new HashMap<>();
            data.put("Tổng công tháng", sum - congDuocTinhKhiQuenCham);
            data.put("Tiền phạt đi muộn về sớm", tienDiMuonVeSom);
            resultExecute.put(Constant.RESPONSE_KEY.DATA, data);
        } catch (Exception ex) {
            logger.error("transactionId: {} - xảy ra ngoại lệ khi thực hiện thêm mới người dùng! Rootcause: {}", transactionId, ex);
            result = new Result(ResponseCode.SYSTEM.getCode(), false, ResponseCode.SYSTEM.getMessage());
        }
        resultExecute.put(Constant.RESPONSE_KEY.RESULT, result);
        return resultExecute;
    }

    public static double convertTimeToMinutes(String timeStr) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        LocalTime time = LocalTime.parse(timeStr, formatter);

        int hours = time.getHour();
        int minutes = time.getMinute();

        return hours * 60 + minutes; // Kết quả là double số phút
    }
}
