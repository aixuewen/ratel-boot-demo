package com.ratel.modules.ldap.util;

import cn.hutool.core.convert.Convert;
import org.apache.commons.codec.binary.Hex;
import sun.misc.BASE64Encoder;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SSHAUtils {
    public static void main(String[] args) {
      String x =getPwdBySSHA( new String("1qaz2wsx"));
      System.out.println(x);
    }
   // Java SSHA算法参考：
    /**************************对密码进行SSHA摘要计算*************************/
    public static String getPwdBySSHA(String pwd){
        String SecretKey = "0123456789abcdef";
        String salt = "";//salt用于拼接密码进行摘要运算   135246
        BASE64Encoder enc = new BASE64Encoder();
        String finalPwd = "";
        //随机生成一个长度为10的16进制字符串
        for(int i=0;i<10;i++){
            int subBegin = (int)(Math.random()*16);
            salt += SecretKey.substring(subBegin, subBegin+1);
        }

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            md.reset();
            md.update(pwd.getBytes());//对密码做一次摘要算法
            md.update(salt.getBytes());//拼接上随机字符串再做一次摘要算法
            byte[] pwhash = md.digest();
            //将摘要结果（字符数组）和随机字符转换得到的数组进行“拼接”（即合并），然后用base64进行编码；
            finalPwd = "{SSHA}"+enc.encode(concatenate(pwhash, salt.getBytes()));
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return finalPwd;
    }

    /**************************数组合并**********************************/
    private static byte[] concatenate(byte[] l, byte[] r) {
        byte[] b = new byte[l.length + r.length];
        System.arraycopy(l, 0, b, 0, l.length);
        System.arraycopy(r, 0, b, l.length, r.length);
        return b;
    }

}
