package com.ratel.modules.task;

import com.ratel.framework.http.FormsHttpEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.ratel.modules.ehcache.RatelEchacheCacheProvider;
import com.ratel.modules.ldap.service.LdapService;
import com.ratel.modules.qywx.service.QywxService;
import com.ratel.modules.security.service.AuthService;
import com.ratel.modules.system.service.SysUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 测试用
 */
@Slf4j
@Component
public class UserAndDeptTask {
    @Autowired
    private QywxService qywxService;

    @Autowired
    private AuthService authService;

    @Autowired
    private SysUserService userService;

    @Autowired
    private RatelEchacheCacheProvider ratelEchacheCacheProvider;;

    @Autowired
    private LdapService ldapService;

    @Value("${wx.defaultPsw}")
    private String defaultPsw;

    @Autowired
    private PasswordEncoder passwordEncoder;
    //同步ldap用户
    public void saveLdapUser(){
        try {
            ldapService.saveLdapUser();
            ratelEchacheCacheProvider.getCache("sysUser").removeAll();
            ratelEchacheCacheProvider.getCache("sysRole").removeAll();
        } catch (Exception e) {
            log.error(e.getMessage());
            ldapService.saveError("LdapUser,SysUser","getLdapUser",e.getMessage().length()>4000?e.getMessage().substring(0,4000):e.getMessage(),"失败");
        }
    }
    //同步ldap部门
    public void saveLdapDept(){
        try {
            ldapService.saveLdapDept();
            ratelEchacheCacheProvider.getCache("sysDept").removeAll();
        } catch (Exception e) {
            log.error(e.getMessage());
            ldapService.saveError("LdapDept,SysDept","getLdapDept",e.getMessage().length()>4000?e.getMessage().substring(0,4000):e.getMessage(),"失败");
        }
    }
    //1同步企业微信部门
    public void saveWxDept(){
        try {
            this.qywxService.saveDeptByWx();
            ratelEchacheCacheProvider.getCache("sysDept").removeAll();
        } catch (Exception e) {
            log.error(e.getMessage());
            ldapService.saveError("getdept","1同步企业微信部门",e.getMessage().length()>4000?e.getMessage().substring(0,4000):e.getMessage(),"失败");
        }
    }
    //1同步企业微信用户
    public void saveWxUser(){
        try {

            String password=passwordEncoder.encode(defaultPsw);
            this.qywxService.saveUserByWx(password);
            ratelEchacheCacheProvider.getCache("sysUser").removeAll();
            ratelEchacheCacheProvider.getCache("sysRole").removeAll();
        } catch (Exception e) {
            log.error(e.getMessage());
            ldapService.saveError("getuser","1同步企业微信用户",e.getMessage().length()>4000?e.getMessage().substring(0,4000):e.getMessage(),"失败");
        }
    }

    //2同步用户数据导企业微信
    public void saveAllUserToQywx(){
        try {
            Map m = this.qywxService.saveUserToQywx();
            if(!"0".equals(m.get("errcode").toString())){
                ldapService.saveError("saveUserToQywx","2同步用户数据导企业微信",m.toString().length()>4000?m.toString().substring(0,4000):m.toString(),"失败");
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            ldapService.saveError("saveUserToQywx","2同步用户数据导企业微信",e.getMessage().length()>4000?e.getMessage().substring(0,4000):e.getMessage(),"失败");
        }
    }
    //3同步部门数据导企业微信
    public void saveAllDeptToQywx(){
        try {

            Map m = this.qywxService.saveDeptToQywx();
            if(!"0".equals(m.get("errcode").toString())){
                ldapService.saveError("saveDeptToQywx","3同步部门数据导企业微信",m.toString().length()>4000?m.toString().substring(0,4000):m.toString(),"失败");
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            ldapService.saveError("saveDeptToQywx","3同步部门数据导企业微信",e.getMessage().length()>4000?e.getMessage().substring(0,4000):e.getMessage(),"失败");
        }
    }
}
