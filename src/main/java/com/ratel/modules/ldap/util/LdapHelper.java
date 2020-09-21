package com.ratel.modules.ldap.util;


import com.ratel.config.SSHAPasswordEncoder;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;


public class LdapHelper {

    private static DirContext ctx;

    @SuppressWarnings(value = "unchecked")
    public static DirContext getCtx() {
//        if (ctx != null ) {
//            return ctx;
//        }
        String account = "dc=ketdz,dc=com"; //binddn
        String password = "secret";    //bindpwd
        String root = "cn=manager,dc=ketdz,dc=com"; // root
        Hashtable env = new Hashtable();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, "ldap://192.168.5.90:389/" + root);
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, "cn="+account );
        env.put(Context.SECURITY_CREDENTIALS, password);
        try {
            // 链接ldap
            ctx = new InitialDirContext(env);
            System.out.println("认证成功");
        } catch (javax.naming.AuthenticationException e) {
            System.out.println("认证失败");
        } catch (Exception e) {
            System.out.println("认证出错：");
            e.printStackTrace();
        }
        return ctx;
    }

    public static void closeCtx(){
        try {
            ctx.close();
        } catch (NamingException ex) {
            Logger.getLogger(LdapHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @SuppressWarnings(value = "unchecked")
    public static boolean verifySHA(String ldappw, String inputpw)
            throws NoSuchAlgorithmException {

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
    }
    @SuppressWarnings(value = "unchecked")
    public static byte[] verifySHATest(String ldappw)
            throws NoSuchAlgorithmException {

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

        byte[] shacode = new byte[20];
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
        // 把随机明文添加到摘要计算信息
        md.update(salt);

        // 按SSHA把当前用户密码进行计算
        byte[] inputpwbyte = md.digest();

        // 返回校验结果
        return inputpwbyte;
    }
    public static void main(String[] args) {
        //getCtx();
        try {
            byte[] xx =  verifySHATest("{SSHA}McUX8DLS1u3wt0XibPBzg4g9YtoxMzUyNDY=");
            System.out.println(Arrays.toString(xx));
            byte[] ldappwbyte = Base64.decode("McUX8DLS1u3wt0XibPBzg4g9YtoxMzUyNDY=");
            System.out.println(Arrays.toString(ldappwbyte));
            SSHAPasswordEncoder se = new SSHAPasswordEncoder();//313335323436
            String sepwd =  se.encode("123456");
            System.out.println(sepwd);
            boolean sebo = se.matches("123456","{SSHA}DkKPINc2kfcR40liOJudA8fR5844NjNkNzExMDFm");
            System.out.println(sebo);


        } catch (Exception e) {
            e.printStackTrace();
        }


    }

}