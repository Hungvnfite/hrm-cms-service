package com.example.cms.service;

import com.example.cms.common.Constant;
import com.example.cms.common.DateUtil;
import com.example.cms.common.ResponseCode;
import com.example.cms.common.Utility;
import com.example.cms.config.CustomAuthenticationFilter;
import com.example.cms.config.JwtUtil;
import com.example.cms.dao.Account;
import com.example.cms.dao.UserInfo;
import com.example.cms.dto.base.Result;
import com.example.cms.dto.model.SessionData;
import com.example.cms.dto.request.MailForgetPasswordRequest;
import com.example.cms.dto.request.UpdateApplyJobRequest;
import com.example.cms.feign.ApplyJobService;
import com.example.cms.repository.AccountRepository;
import com.example.cms.repository.UserInfoRepository;



import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.types.ObjectId;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;


import javax.mail.internet.MimeMessage;
import java.security.MessageDigest;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;



@Service
@RequiredArgsConstructor
public class HRService {

    private final Logger logger = LogManager.getLogger(HRService.class);
    private final JavaMailSender mailSender;
    private final AccountRepository accountRepository;
    private final UserInfoRepository userInfoRepository;
    private final ApplyJobService feignClient;
    private final RedissonService redissonService;
    private final JwtUtil jwtUtil;

    public Map<Object, Object> getAll(String transactionId) {
        Map<Object, Object> resultExecute = new HashMap<>();
        Result result = Result.OK();
        try {
            if (!jwtUtil.isAdminToken(jwtUtil.getToken(CustomAuthenticationFilter.REQUEST))) {
                result = new Result(ResponseCode.TOKEN_INVALID.getCode(), false, ResponseCode.TOKEN_INVALID.getMessage());
                resultExecute.put(Constant.RESPONSE_KEY.RESULT, result);
                return resultExecute;
            }
                List<Account> accounts = accountRepository.findByIsForgottenTrue();
                if (!accounts.isEmpty()) {
                    List<Map<String, String>> mapList = new ArrayList<>();
                    accounts.forEach(account -> {
                        UserInfo userInfo = userInfoRepository.findByUsernameAndIsDelete(account.getUsername(), Constant.STATUS.IS_UN_DELETED);
                        Map<String, String> data = new HashMap<>();
                        data.put("id", String.valueOf(account.getId()));
                        data.put("Mã nhân viên", userInfo.getMaNv());
                        data.put("username", account.getUsername());
                        data.put("email", userInfo.getEmail());
                        data.put("Số điện thoại", userInfo.getPhone());
                        mapList.add(data);
                    });
                    resultExecute.put(Constant.RESPONSE_KEY.DATA, mapList);
                }
        } catch (Exception ex) {
            logger.error("transactionId: {} - xảy ra ngoại lệ khi thực hiện lấy danh sách tài khoản quên mật khẩu! Rootcause: {}", transactionId, ex);
            result = new Result(ResponseCode.SYSTEM.getCode(), false, ResponseCode.SYSTEM.getMessage());
        }
        resultExecute.put(Constant.RESPONSE_KEY.RESULT, result);
        return resultExecute;
    }


    public Map<Object, Object> sendNewPassword(String transactionId, MailForgetPasswordRequest request) {
        Map<Object, Object> resultExecute = new HashMap<>();
        Result result = Result.OK();
        MimeMessage message = mailSender.createMimeMessage();

        try {
            Account account = accountRepository.findTopByUsernameAndIsDelete(request.getUsername(),false);
            if (account != null && account.getIsForgotten()) {

                MessageDigest md = MessageDigest.getInstance("MD5");
                md.update(request.getNewPassword().getBytes());
                byte[] mdBytes = md.digest();
                String md5String = bytesToHex(mdBytes);

                // Hash mật khẩu bằng BCrypt
                BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
                account.setPassword(encoder.encode(md5String));
                account.setUpdatedAt(new Date().toString());
                account.setIsForgotten(false);
                accountRepository.save(account);
            }else if(!account.getIsForgotten()){
                result = new Result(ResponseCode.ERROR_REISSUE_LOST_PASSWORD.getCode(), false, ResponseCode.ERROR_REISSUE_LOST_PASSWORD.getMessage());
                resultExecute.put(Constant.RESPONSE_KEY.RESULT, result);
                return resultExecute;
            }else{
                result = new Result(ResponseCode.DOES_NOT_EXIST_ACCOUNT.getCode(), false, ResponseCode.DOES_NOT_EXIST_ACCOUNT.getMessage());
                resultExecute.put(Constant.RESPONSE_KEY.RESULT, result);
                return resultExecute;
            }

            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom("ecommercedemo47@gmail.com");
            helper.setTo(request.getToEmail());
            helper.setSubject("Reissue Lost Password");

            String htmlBody = "<div style='font-family: Arial, sans-serif; font-size: 15px;'>"
                    + "<h2>Hello " + request.getUsername() + ",</h2>"
                    + "<p>We received a request to reset your password.</p>"
                    + "<p><strong>Your new password is:</strong></p>"
                    + "<p style='font-size: 18px; font-weight: bold; color: #d9534f;'>" + request.getNewPassword() + "</p>"
                    + "<p>Please use this password to log in and consider changing it after your first login.</p>"
                    + "<p>If you didn't request a password reset, please ignore this email.</p>"
                    + "<br><p>Best regards,</p>"
                    + "</div>";
            helper.setText(htmlBody, true);
            mailSender.send(message);
        } catch (Exception ex) {
            logger.error("transactionId: {} - xảy ra ngoại lệ khi thực hiện lấy danh sách tài khoản quên mật khẩu! Rootcause: {}", transactionId, ex);
            result = new Result(ResponseCode.SYSTEM.getCode(), false, ResponseCode.SYSTEM.getMessage());
        }
        resultExecute.put(Constant.RESPONSE_KEY.RESULT, result);
        return resultExecute;
    }

    public Map<Object, Object> sendInviteToInterview(String transactionId, UpdateApplyJobRequest request) {
        Map<Object, Object> resultExecute = new HashMap<>();
        Result result = Result.OK();

        try {
            var response = feignClient.applicantReview(transactionId, request);
            Map<String, Object> data = (Map<String, Object>)
                    (response.getData() != null ? response.getData() : new HashMap<>());
            Map<String, Object> resultMap = (Map<String, Object>) response.getResult();
            Boolean isOK = (Boolean) resultMap.get("isOK");

            if (data == null && !isOK) {
                result = new Result(ResponseCode.SYSTEM.getCode(), false, "Không lấy được thông tin ứng viên");
                resultExecute.put(Constant.RESPONSE_KEY.RESULT, result);
                return resultExecute;
            }

            if(Objects.equals(request.getInterviewStatus(), "2") && isOK){
                var exits = accountRepository.findTopByUsernameAndIsDelete(request.getUsername(),false);

                if(exits != null){
                    result = new Result(ResponseCode.SYSTEM.getCode(), false, "Username đã tồn tại ! Vui lòng kiểm rta lại");
                    resultExecute.put(Constant.RESPONSE_KEY.RESULT, result);
                    return resultExecute;
                }
                //tạo sài khoản mới
                Account account = new Account();
                account.setUsername(request.getUsername());

                BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
                account.setPassword(encoder.encode(request.getPassword()));
                account.setIsFirstPassword(true);
                account.setTypeAccount(1);
                account.setCreatedAt(DateUtil.genCreatedAt(null));
                account.setUpdatedAt(DateUtil.genCreatedAt(null));

                UserInfo userInfo = new UserInfo();
                userInfo.setUsername(request.getUsername());
                userInfo.setMaNv(generateMaNv());
                userInfo.setCreatedAt(DateUtil.genCreatedAt(null));
                userInfo.setUpdatedAt(DateUtil.genCreatedAt(null));

                accountRepository.save(account);
                userInfoRepository.save(userInfo);
            }

            String isSent = (String) data.get("isSent");
            if(Objects.equals(isSent, "Y")){
                result = new Result(ResponseCode.SYSTEM.getCode(), false, "Đã gửi mail cho ứng viên");
                resultExecute.put(Constant.RESPONSE_KEY.RESULT, result);
                return resultExecute;
            }else {
                String name = (String) data.get("name");
                String email = (String) data.get("email") != null ? (String) data.get("email") : "ecommercedemo47@gmail.com";
                String interviewDate = (String) data.get("interviewDate");
                String position = (String) data.get("position");
//                String location = (String) data.get("location");
                String location = "4b vương thừa vũ, phường khương mai, quận thanh xuân, thành phố hà nội";

                // Tạo email mời phỏng vấn
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true);
                helper.setFrom("ecommercedemo47@gmail.com");
                helper.setTo(email);
                helper.setSubject("Thư mời phỏng vấn - Công ty VNFite");

                // Convert interviewDate thành định dạng dễ đọc
                String formattedDate = interviewDate;
                try {
                    OffsetDateTime odt = OffsetDateTime.parse(interviewDate);
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm 'ngày' dd/MM/yyyy").withZone(ZoneId.of("Asia/Ho_Chi_Minh"));
                    formattedDate = formatter.format(odt);
                } catch (Exception ignore) {
                }

                //            https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSXkZPceH10ifvYyOrhbjCP1p8hyfd2AtwPPg&s
                String htmlBody = "<div style='font-family: Arial, sans-serif; font-size: 15px; line-height: 1.6; color: #333; max-width: 600px; margin: auto;'>"
                        // Logo
                        + "<div style='text-align: center; margin-bottom: 10px;'>"
                        + "<img src='https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSXkZPceH10ifvYyOrhbjCP1p8hyfd2AtwPPg&s' alt='VNFite Logo' style='width: 200px;'>"
                        + "</div>"

                        // Lời mở đầu
                        + "<h3 style='color: #006A67;'>Kinh gửi Anh/chị : " + name + ",</h3>"
                        + "<p style='text-align: justify;'>Phòng Nhân sự Công ty CP Công nghệ tài chính VNFITE (VNFITE) cảm ơn Anh đã dành thời gian quan tâm và gửi thông tin cá nhân ứng tuyển cho vị trí <strong>" + position + "</strong> tại <strong>VNFite</strong>.</p>"
                        + "<p style='text-align: justify;'>Để có thể hiểu rõ hơn về Công ty và vị trí ứng dụng tuyển dụng, cũng như tạo điều kiện cho chúng tôi đánh giá chính xác hơn về năng lực chuyên ngành và kinh nghiệm của Anh/Chị, " +
                        "kính mời Anh đến tham dự buổi phỏng vẫn trực tiếp tại văn phòng VNFITE có thể với Thông tin buổi phỏng vấn:</p>"

                        // Thông tin phỏng vấn
                        //                    + "<h3 style='color: #004085;'>🔎 Thông tin buổi phỏng vấn</h3>"
                        + "<table style='width: 100%; border-collapse: collapse;'>"
                        + "<tr><td style='padding: 6px 0;'><strong>Vị trí tuyển dụng:</strong></td><td>" + position + "</td></tr>"
                        + "<tr><td style='padding: 6px 0;'><strong>Thời gian:</strong></td><td>" + formattedDate + "</td></tr>"
                        + "<tr><td style='padding: 6px 0;'><strong>Địa điểm:</strong></td><td>" + location + "</td></tr>"
                        + "<tr><td style='padding: 6px 0;'><strong>Người phỏng vấn:</strong></td><td>[Tên & chức vụ]</td></tr>"
                        + "<tr><td style='padding: 6px 0;'><strong>Thời lượng dự kiến:</strong></td><td>Khoảng 30-45 phút</td></tr>"
                        + "<tr><td style='padding: 6px 0;'><strong>Hình thức:</strong></td><td>[Trực tiếp / Online]</td></tr>"
                        + "</table>"

                        // Hướng dẫn chuẩn bị
                        + "<p style='margin-top: 20px;'>👉 Vui lòng chuẩn bị trang phục lịch sự và đến đúng giờ.</p>"
                        + "<p>📞 Nếu cần hỗ trợ, vui lòng liên hệ: <strong>Mr. Hương – 0345.678.910</strong>.</p>"

                        // Xác nhận
                        + "<h3 style='color: #006A67;'>✅ Xác nhận tham dự</h3>"
                        + "<p>Chúng tôi mong nhận được phản hồi xác nhận tham dự phỏng vấn của bạn trước <strong>" + formattedDate + "</strong> bằng cách trả lời email này.</p>"
                        + "<p>Nếu bạn không thể tham dự theo lịch hẹn, vui lòng thông báo để chúng tôi sắp xếp lịch khác phù hợp hơn.</p>"

                        // Kết thư
                        + "<p>Rất mong được gặp bạn tại buổi phỏng vấn.</p>"
                        + "<p>Trân trọng,</p>"
                        + "<p>Phòng Tuyển dụng VNFite</p>"

                        + "</div>";

                helper.setText(htmlBody, true);

                mailSender.send(message);
                resultExecute.put(Constant.RESPONSE_KEY.DATA, response.getData());
            }
        } catch (Exception ex) {
            logger.error("transactionId: {} - xảy ra lỗi khi gửi thư mời phỏng vấn! Rootcause: {}", transactionId, ex);
            result = new Result(ResponseCode.SYSTEM.getCode(), false, "Lỗi hệ thống khi gửi thư mời phỏng vấn.");
        }

        resultExecute.put(Constant.RESPONSE_KEY.RESULT, result);
        return resultExecute;
    }


    // Chuyển pass mới sang md5
    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private String generateMaNv() {
        // Lấy mã nhân viên lớn nhất từ DB
        UserInfo latestAccount = userInfoRepository.findTopByOrderByMaNvDesc();
        int nextNumber;

        if (latestAccount == null || latestAccount.getMaNv() == null) {
            nextNumber = 1; // Bắt đầu từ 0001 nếu chưa có mã nào
        } else {
            nextNumber = Integer.parseInt(latestAccount.getMaNv()) + 1; // Tăng lên 1
        }

        // Định dạng: nếu nhỏ hơn 10000 thì giữ 4 chữ số với số 0 ở đầu, nếu không thì để nguyên
        if (nextNumber < 10000) {
            return String.format("%04d", nextNumber); // "0001", "0012", ..., "9999"
        } else {
            return String.valueOf(nextNumber); // "10000", "10001", ...
        }
    }
}
