package com.ratel.config;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import sun.misc.BASE64Encoder;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class SSHAPasswordEncoder implements PasswordEncoder {
    @Value("${ratel.rsa.salt}")
    private String salt;

    /*private final String salt;

    public SSHAPasswordEncoder(String salt) {
       this.salt = salt;
    }*/
	@Override
	public String encode(CharSequence rawPassword) {
		String pwd  = rawPassword.toString();
		String salt = this.salt;//salt用于拼接密码进行摘要运算   135246   313335323436
		BASE64Encoder enc = new BASE64Encoder();
		String finalPwd = "";
		//随机生成一个长度为10的16进制字符串
		/*for(int i=0;i<10;i++){
			int subBegin = (int)(Math.random()*16);
			salt += SecretKey.substring(subBegin, subBegin+1);
		}*/

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
	@Override
	public boolean matches(CharSequence rawPassword, String encodedPassword) {
		String inputpw = rawPassword.toString();
		String ldappw = encodedPassword;
        try {
		// MessageDigest 提供了消息摘要算法，如 MD5 或 SHA，的功能，这里LDAP使用的是SHA-1
            MessageDigest md = MessageDigest.getInstance("SHA-1");

            // 取出加密字符
            if (ldappw.startsWith("{SSHA}")) {
                ldappw = ldappw.substring(6);
            } else if (ldappw.startsWith("{SHA}")) {
                ldappw = ldappw.substring(5);
            }

            // 解码BASE64
            byte[] ldappwbyte = Base64.decode(ldappw);
            byte[] shacode =  new byte[20];
            byte[] salt;

            // 前20位是SHA-1加密段，20位后是最初加密时的随机明文
            if (ldappwbyte.length <= 20) {
                shacode = ldappwbyte;
                salt = new byte[0];
            } else {
                salt = new byte[ldappwbyte.length - 20];
                System.arraycopy(ldappwbyte, 0, shacode, 0, 20);
                System.arraycopy(ldappwbyte, 20, salt, 0, salt.length);
            }

            // 把用户输入的密码添加到摘要计算信息
            md.update(inputpw.getBytes());
            // 把随机明文添加到摘要计算信息
            md.update(salt);

            // 按SSHA把当前用户密码进行计算
            byte[] inputpwbyte = md.digest();

            // 返回校验结果
            return MessageDigest.isEqual(shacode, inputpwbyte);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
