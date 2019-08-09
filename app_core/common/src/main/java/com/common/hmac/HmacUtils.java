package com.common.hmac;

import com.common.LogUtil;

/**
 * Hmac加密工具
 *
 * @author yingp
 */
public class HmacUtils {

    // 默认约定密钥
    private final static byte[] key = {104, 116, 116, 112, 58, 47, 47, 119, 119, 119, 46, 117, 98, 116, 111, 98, 46, 99, 111, 109, 47,
            101, 114, 112, 47, 115, 97, 108, 101, 47, 111, 114, 100, 101, 114, 115, 63, 115, 111, 109, 101, 116, 104, 105, 110, 103};
    private static HmacEncoder hmacEncoder;

    static {
        // default algorithm: HmacSHA256
        hmacEncoder = new HmacSHA256Encoder();
    }

    /**
     * @param message
     * @return 16进制密文
     */
    public static String encode(Object message) {
        byte[] encodeData = hmacEncoder.encode(String.valueOf(message).getBytes(), key);
        String result=  new String(Hex.encode(encodeData));
        LogUtil.d("AppLogs","encode result:"+result);
        return result;
    }

}
