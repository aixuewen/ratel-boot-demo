package com.ratel.modules.ldap.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.Control;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

public class TestUtils {
    public static void main(String[] args) {

        readLdap();
    }



    /**
     * 认证并获取用户信息
     *
     */
    public static void readLdap(){
        String url = "ldap://192.168.5.90:389";
        String basedn = "dc=ketdz,dc=com";  // basedn
        String factory = "com.sun.jndi.ldap.LdapCtxFactory";
        String root = "cn=manager,dc=ketdz,dc=com";  // 用户
        String pwd ="secret";  // pwd
        String simple="simple";
        Hashtable<String, String> env = new Hashtable<String, String>();
        env.put(Context.INITIAL_CONTEXT_FACTORY,factory);
        env.put(Context.PROVIDER_URL, url);
        env.put(Context.SECURITY_AUTHENTICATION, simple);
        env.put(Context.SECURITY_PRINCIPAL, root);
        env.put(Context.SECURITY_CREDENTIALS, pwd);
        LdapContext ctx = null;
        Control[] connCtls = null;
        try {
            ctx = new InitialLdapContext(env, connCtls);
            System.out.println( "认证成功" );
        }catch (javax.naming.AuthenticationException e) {
            System.out.println("认证失败：");
            e.printStackTrace();
            return;
        } catch (Exception e) {
            System.out.println("认证出错：");
            e.printStackTrace();
            return;
        }
        List<Map<String, Object>> lm=new ArrayList<Map<String, Object>>();

        try {
            //过滤条件
            String filter = "(&(objectClass=*)(uid=*))";
            String[] attrPersonArray = { "uid", "userPassword", "displayName", "cn", "sn", "mail", "description" };
            SearchControls searchControls = new SearchControls();//搜索控件
            searchControls.setSearchScope(2);//搜索范围
            searchControls.setReturningAttributes(attrPersonArray);
            //1.要搜索的上下文或对象的名称；2.过滤条件，可为null，默认搜索所有信息；3.搜索控件，可为null，使用默认的搜索控件
            NamingEnumeration<SearchResult> answer = ctx.search(basedn, filter.toString(),searchControls);
            while (answer.hasMore()) {
                SearchResult result = (SearchResult) answer.next();
                NamingEnumeration<? extends Attribute> attrs = result.getAttributes().getAll();
                Map<String, Object> map = new HashMap<String, Object>();

                while (attrs.hasMore()) {
                    Attribute attr = (Attribute) attrs.next();
                    if("userPassword".equals(attr.getID())){
                        Object value = attr.get();
                        map.put(attr.getID(), new String((byte [])value));
                    }else{
                        map.put(attr.getID(), attr.get());
                    }
                }

                if(map!=null){
                    System.out.println(map);
                    lm.add(map);
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if(ctx != null)
                ctx.close();
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }


}