package org.jeecg.modules.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.jeecg.common.exception.JeecgBootException;

import java.util.Date;

public class JwtUtil {
    /**
     * 过期时间7天
     */
    public static final long EXPIRE_TIME = 7 * 24 * 60 * 60 * 1000L;
    /**
     * jwt 密钥
     */
    public static final String SECRET = "adRtH43cit5vK3TfYRugLZ9H558aaCDtBPvGfEPUvULdObiUPSUMMER";

    /**
     * 生成签名，30天后过期
     * @param id
     * @return
     */
    public static String createToken(String id) {
        try {
            Date date = new Date(System.currentTimeMillis() + EXPIRE_TIME);
            Algorithm algorithm = Algorithm.HMAC256(SECRET);
            return JWT.create()
                    // 将 id 保存到 token 里面
                    .withAudience(id)
                    // token过期时间设置
                    .withExpiresAt(date)
                    // token 的密钥
                    .sign(algorithm);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 根据token获取userId
     * @param token
     * @return
     */
    public static String getId(String token) {
        try {
            String id = JWT.decode(token).getAudience().get(0);
            return id;
        } catch (JWTDecodeException e) {
            throw new JeecgBootException("token解析失败！");
        }
    }

    /**
     * 校验token
     * @param token
     * @return
     */
    public static boolean checkSign(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(SECRET);
            JWTVerifier verifier = JWT.require(algorithm)
                    .build();
            DecodedJWT jwt = verifier.verify(token);
            return true;
        } catch (JWTVerificationException exception) {
            throw new JeecgBootException("token 无效，请重新获取");
        }
    }



}
