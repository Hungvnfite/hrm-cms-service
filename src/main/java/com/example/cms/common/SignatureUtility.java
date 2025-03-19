package com.example.cms.common;

import com.example.cms.dto.base.Result;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class SignatureUtility {

    public Map<Object, Object> decode(String token) {
        Map<Object, Object> mapData = new HashMap<>();
        Result result = Result.OK();
        try {

            String publicKeyContent  = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAtMoTOKK5XOZk0fBwZQNs" +
                    "Ex1ESTJ8+QwbbUHcNwY/dRX/5VevaQYWw9VrTY2qc65j0+eWy5p4xhduWIx7ciVP" +
                    "H2gW3bnhD+dc4gJXUs27SJdaCf8cZ32HM1cySTd8XMhpSOlaQ8wcTYLFrM868deC" +
                    "PKVyYt+ermgq8gEIlDPapXeExHhlI/Haxq32+N3k4ieX7kuaJIqeH+vyRtQ5v+wt" +
                    "3r9rzszzxxtlg5dHglVOfDy4HwPxvXH4M4qQ1gk168hS4G8QMeHgzvMsCxHbTOM0" +
                    "T/hLTNYL5XXVgrGWHjxOe1lntUKMbOrOycThKWUOlLb2bSO+lv/lbK0jSU/zR0Rg" +
                    "UwIDAQAB";

            byte[] keyBytes = Base64.getDecoder().decode(publicKeyContent);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");

            PublicKey publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(keyBytes));

            Jws<Claims> claims = Jwts.parser()
                    .setSigningKey(publicKey)
                    .build()
                    .parseClaimsJws(token);

            System.out.println(claims);

            mapData.put("data", claims);
        } catch (Exception e) {
            e.printStackTrace();
            result = new Result(ResponseCode.SYSTEM.getCode(), false, ResponseCode.SYSTEM.getMessage());
        }
        mapData.put("result", result);
        return mapData;
    }
}
