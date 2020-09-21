package com.ratel.modules.ldap.rest;

import com.ratel.framework.annotation.aop.log.RatelLog;
import com.ratel.framework.http.FormsHttpEntity;
import com.ratel.modules.ehcache.RatelEchacheCacheProvider;
import com.ratel.modules.ldap.service.LdapService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "LDAP：")
@RestController
@RequestMapping("/api/ladp")
public class LDAPController {

	private static final Logger log = LoggerFactory.getLogger(LDAPController.class);

	@Autowired
	private LdapService ldapService;

	@Value("${spring.ldap.password}")
	private String defaultPsw;


	@Autowired
	private RatelEchacheCacheProvider ratelEchacheCacheProvider;


	@RatelLog("同步ldap用户")
	@ApiOperation(value = "同步ldap用户")
	@GetMapping("/getLdapUser")
	public ResponseEntity<Object> getLdapUser() {
		try {
			//PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
			//String password=passwordEncoder.encode(defaultPsw);
			//ldapService.saveLdapUser(password);
			ldapService.saveLdapUser();
			ratelEchacheCacheProvider.getCache("sysUser").removeAll();
			ratelEchacheCacheProvider.getCache("sysRole").removeAll();
			return FormsHttpEntity.ok();
		} catch (Exception e) {
			log.error(e.getMessage());
			ldapService.saveError("LdapUser,SysUser","getLdapUser",e.getMessage().length()>4000?e.getMessage().substring(0,4000):e.getMessage(),"失败");
			return FormsHttpEntity.error(null, 1001, e.getMessage());
		}
	}

	@RatelLog("同步ldap部门")
    @ApiOperation(value = "同步ldap部门")
	@GetMapping("/getLdapDept")
    public ResponseEntity<Object> getLdapDept() {
		try {
			ldapService.saveLdapDept();
			ratelEchacheCacheProvider.getCache("sysDept").removeAll();
			return FormsHttpEntity.ok();
		} catch (Exception e) {
			log.error(e.getMessage());
			ldapService.saveError("LdapDept,SysDept","getLdapDept",e.getMessage().length()>4000?e.getMessage().substring(0,4000):e.getMessage(),"失败");
			return FormsHttpEntity.error(null, 1001, e.getMessage());
		}
    }

	@RatelLog("test")
	@ApiOperation(value = "test")
	@GetMapping("/test")
	public ResponseEntity<Object> test() {
		return FormsHttpEntity.ok();
	}
	

}
