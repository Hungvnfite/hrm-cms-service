package com.example.cms.service;

import com.example.cms.common.Constant;
import com.example.cms.common.ResponseCode;
import com.example.cms.common.Utility;
import com.example.cms.config.CustomAuthenticationFilter;
import com.example.cms.config.JwtUtil;
import com.example.cms.dao.Account;
import com.example.cms.dao.UserInfo;
import com.example.cms.dto.base.Result;
import com.example.cms.dto.model.SessionData;
import com.example.cms.dto.request.MailForgetPasswordRequest;
import com.example.cms.repository.AccountRepository;
import com.example.cms.repository.UserInfoRepository;



import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;


import javax.mail.internet.MimeMessage;
import java.security.MessageDigest;
import java.util.*;



@Service
@RequiredArgsConstructor
public class HRService {

    private final Logger logger = LogManager.getLogger(HRService.class);
    private final JavaMailSender mailSender;
    private final AccountRepository accountRepository;
    private final UserInfoRepository userInfoRepository;
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
                // Get an instance of MessageDigest for MD5
                MessageDigest md = MessageDigest.getInstance("MD5");

                // Update digest with the input string
                md.update(request.getNewPassword().getBytes());

                // Get the MD5 hash
                byte[] mdBytes = md.digest();

                // Convert byte array to a hexadecimal string
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

    // Chuyển pass mới sang md5
    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
