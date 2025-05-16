package com.example.cms.service.auth;

import com.example.cms.common.Constant;
import com.example.cms.common.DateUtil;
import com.example.cms.common.ResponseCode;
import com.example.cms.config.JwtUtil;
import com.example.cms.dao.Account;
import com.example.cms.dto.base.Result;
import com.example.cms.dto.model.LoginDTO;
import com.example.cms.dto.model.SessionData;
import com.example.cms.dto.request.LoginRequest;
import com.example.cms.repository.AccountRepository;
import com.example.cms.service.RedissonService;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.types.ObjectId;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final Logger logger = LogManager.getLogger(AuthService.class);
    private final AccountRepository accountRepository;
    private final RedissonService redissonService;
    private final JwtUtil jwtUtil;
    private final HttpServletRequest request;

    public Map<Object, Object> login(String transactionId, LoginRequest request) {
        Map<Object, Object> resultExecute = new HashMap<>();
        Result result = Result.OK();
        try {
            Account account = accountRepository.findTopByUsernameAndIsDelete(request.getUsername(), false);
            if (account != null) {
                PasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
                boolean isMatchesPassword = bCryptPasswordEncoder.matches(request.getPassword(), account.getPassword());
                if (isMatchesPassword) {
                    LoginDTO loginDTO = new LoginDTO();
                    loginDTO.setIsFirstPassword(account.getIsFirstPassword());
                    if (account.getTypeAccount() == 1) {
                        String accessToken = jwtUtil.generateAccessToken(Constant.ACCESS_STRING.concat(String.valueOf(account.getId())), account.getTypeAccount());
                        loginDTO.setAccessToken(accessToken);
                        String refreshToken = jwtUtil.generateRefreshToken(Constant.REFRESH_STRING.concat(String.valueOf(account.getId())));
                        loginDTO.setRefreshToken(refreshToken);
                        SessionData sessionData = new SessionData();
                        sessionData.setSessionId(accessToken);
                        sessionData.setAccountId(String.valueOf(account.getId()));
                        sessionData.setRefreshToken(refreshToken);
                        sessionData.setTypeAccount(account.getTypeAccount());
//                        redissonService.clearSession();
                        redissonService.setSession(accessToken, sessionData);
                        redissonService.setAccessToken(Constant.ACCESS_STRING.concat(accessToken), accessToken);
                        redissonService.setRefreshToken(Constant.REFRESH_STRING.concat(refreshToken), refreshToken);
                        account.setRefreshToken(refreshToken);
                        accountRepository.save(account);
                        resultExecute.put(Constant.RESPONSE_KEY.DATA, loginDTO);
                    }
                } else {
                    result = new Result(ResponseCode.LG_WRONG_USER.getCode(), false, ResponseCode.LG_WRONG_USER.getMessage());
                    resultExecute.put(Constant.RESPONSE_KEY.RESULT, result);
                    return resultExecute;
                }
            } else {
                result = new Result(ResponseCode.LG_WRONG_USER.getCode(), false, ResponseCode.LG_WRONG_USER.getMessage());
                resultExecute.put(Constant.RESPONSE_KEY.RESULT, result);
                return resultExecute;
            }
        } catch (Exception ex) {
            logger.error("transactionId: {} - xảy ra ngoại lệ khi thực hiện đăng nhập! Rootcause: {}", transactionId, ex);
            result = new Result(ResponseCode.SYSTEM.getCode(), false, ResponseCode.SYSTEM.getMessage());
        }
        resultExecute.put(Constant.RESPONSE_KEY.RESULT, result);
        return resultExecute;
    }

    public Map<Object, Object> checkResetPassword(String transactionId, ObjectId id) {
        Map<Object, Object> resultExecute = new HashMap<>();
        Result result = Result.OK();
        try {
            // Kiểm tra token
            if (!jwtUtil.isAdminToken(jwtUtil.getToken(request))) {
                result = new Result(ResponseCode.TOKEN_INVALID.getCode(), false, ResponseCode.TOKEN_INVALID.getMessage());
                resultExecute.put(Constant.RESPONSE_KEY.RESULT, result);
                return resultExecute;
            }
            Account account = accountRepository.findById(String.valueOf(id)).orElse(null);
            if (account != null) {
//                    // Get an instance of MessageDigest for MD5
//                    MessageDigest md = MessageDigest.getInstance("MD5");
//
//                    // Update digest with the input string
//                    md.update(request.getPassword().getBytes());
//
//                    // Get the MD5 hash
//                    byte[] mdBytes = md.digest();
//
//                    // Convert byte array to a hexadecimal string
//                    String md5String = bytesToHex(mdBytes);
//
//                    // Hash mật khẩu bằng BCrypt
//                    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
//                    account.setPassword(encoder.encode(md5String));
                // Chuỗi ký tự để tạo mật khẩu ngẫu nhiên
                String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
                Random random = new Random();
                StringBuilder password = new StringBuilder(8);

                // Tạo chuỗi 8 ký tự ngẫu nhiên
                for (int i = 0; i < 8; i++) {
                    password.append(characters.charAt(random.nextInt(characters.length())));
                }

                // Get an instance of MessageDigest for MD5
                MessageDigest md = MessageDigest.getInstance("MD5");

                // Update digest with the input string
                md.update(password.toString().getBytes());

                // Get the MD5 hash
                byte[] mdBytes = md.digest();

                // Convert byte array to a hexadecimal string
                String md5String = bytesToHex(mdBytes);

                // Hash mật khẩu bằng BCrypt
                BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
                account.setPassword(encoder.encode(md5String));
                account.setUpdatedAt(DateUtil.genCreatedAt(null));
                accountRepository.save(account);
                resultExecute.put(Constant.RESPONSE_KEY.DATA, password.toString());
            }

        } catch (Exception ex) {
            logger.error("transactionId: {} - xảy ra ngoại lệ khi thực hiện thay đổi mật khẩu! Rootcause: {}", transactionId, ex);
            result = new Result(ResponseCode.SYSTEM.getCode(), false, ResponseCode.SYSTEM.getMessage());
        }
        resultExecute.put(Constant.RESPONSE_KEY.RESULT, result);
        return resultExecute;
    }

    // Chuyển pass mới sang md5
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
