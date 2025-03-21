package com.example.cms.service;

import com.example.cms.common.Constant;
import com.example.cms.common.ResponseCode;
import com.example.cms.config.JwtUtil;
import com.example.cms.dao.Account;
import com.example.cms.dto.base.Result;
import com.example.cms.dto.model.CheckTokenDTO;
import com.example.cms.dto.model.SessionData;
import com.example.cms.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final Logger logger = LogManager.getLogger(TokenService.class);

    private final RedissonService redissonService;
    private final AccountRepository accountRepository;
    private final JwtUtil jwtUtil;

    public Map<Object, Object> checkAccessToken(String transactionId, String accessToken, String refreshToken) {
        Map<Object, Object> resultExecute = new HashMap<>();
        Result result = Result.OK();
        try {
            resultExecute.put(Constant.RESPONSE_KEY.RESULT, result);
            if (!jwtUtil.isTokenExpired(accessToken)) {
                SessionData sessionData = redissonService.getSession(accessToken);
                if (sessionData != null) {
                    Account account = accountRepository.findById(sessionData.getAccountId()).orElse(null);
                    resultExecute.put(Constant.RESPONSE_KEY.DATA, account);
                    return resultExecute;
                }
            } else {
                SessionData sessionData = redissonService.getSession(accessToken);
                if (sessionData != null) {
                    if (!jwtUtil.isTokenExpired(sessionData.getRefreshToken())) {
                        redissonService.clearAccessToken(Constant.ACCESS_STRING.concat(accessToken));
                        redissonService.clearRefreshToken(sessionData.getRefreshToken());
                        redissonService.clearSession(accessToken, sessionData.getAccountId());
                        String accessTokenNew = jwtUtil.generateAccessToken(Constant.ACCESS_STRING.concat(sessionData.getAccountId()), sessionData.getTypeAccount());
                        String refreshTokenNew = jwtUtil.generateRefreshToken(Constant.REFRESH_STRING.concat(sessionData.getAccountId()));
                        redissonService.setAccessToken(Constant.ACCESS_STRING.concat(accessTokenNew), accessTokenNew);
                        redissonService.setRefreshToken(Constant.REFRESH_STRING.concat(refreshTokenNew), refreshTokenNew);
                        SessionData sessionData1 = new SessionData();
                        sessionData1.setSessionId(accessTokenNew);
                        sessionData1.setAccountId(sessionData.getAccountId());
                        sessionData1.setRefreshToken(refreshTokenNew);
                        sessionData1.setTypeAccount(sessionData.getTypeAccount());
                        redissonService.setSession(accessTokenNew, sessionData1);
                        Account account = accountRepository.findById(sessionData.getAccountId()).orElse(null);
                        if (account != null) {
                            account.setRefreshToken(refreshTokenNew);
                            accountRepository.save(account);
                        }
                        CheckTokenDTO checkTokenDTO = new CheckTokenDTO();
                        checkTokenDTO.setAccessToken(accessTokenNew);
                        checkTokenDTO.setRefreshToken(refreshTokenNew);
                        resultExecute.put(Constant.RESPONSE_KEY.DATA, checkTokenDTO);
                        return resultExecute;
                    } else {
                        resultExecute.put(Constant.RESPONSE_KEY.RESULT, Result.errorFromCode(ResponseCode.SESSION_EXPIRED));
                        return resultExecute;
                    }
                } else {
                    Account account = accountRepository.findByRefreshToken(refreshToken);
                    if (account != null) {
                        if (!jwtUtil.isTokenExpired(refreshToken)) {
                            redissonService.clearAccessToken(Constant.ACCESS_STRING.concat(accessToken));
                            redissonService.clearRefreshToken(refreshToken);
                            String accessTokenNew = jwtUtil.generateAccessToken(Constant.ACCESS_STRING.concat(String.valueOf(account.getId())), account.getTypeAccount());
                            String refreshTokenNew = jwtUtil.generateRefreshToken(Constant.REFRESH_STRING.concat(String.valueOf(account.getId())));
                            redissonService.setAccessToken(Constant.ACCESS_STRING.concat(accessTokenNew), accessTokenNew);
                            redissonService.setRefreshToken(Constant.REFRESH_STRING.concat(refreshTokenNew), refreshTokenNew);
                            account.setRefreshToken(refreshTokenNew);
                            accountRepository.save(account);
                            CheckTokenDTO checkTokenDTO = new CheckTokenDTO();
                            checkTokenDTO.setAccessToken(accessTokenNew);
                            checkTokenDTO.setRefreshToken(refreshTokenNew);
                            resultExecute.put(Constant.RESPONSE_KEY.DATA, checkTokenDTO);
                            return resultExecute;
                        } else {
                            resultExecute.put(Constant.RESPONSE_KEY.RESULT, Result.errorFromCode(ResponseCode.SESSION_EXPIRED));
                            return resultExecute;
                        }
                    } else {
                        resultExecute.put(Constant.RESPONSE_KEY.RESULT, Result.errorFromCode(ResponseCode.SESSION_EXPIRED));
                        return resultExecute;
                    }
                }
            }
        } catch (Exception ex) {
            logger.error("transactionId: {} - xảy ra ngoại lệ khi thực hiện kiểm tra token! Rootcause: {}", transactionId, ex);
            result = new Result(ResponseCode.SYSTEM.getCode(), false, ResponseCode.SYSTEM.getMessage());
        }
        resultExecute.put(Constant.RESPONSE_KEY.RESULT, result);
        return resultExecute;
    }
}
