package com.ratel.modules.qywx.rest;

import com.alibaba.fastjson.JSON;
import com.ratel.framework.annotation.aop.log.RatelLog;
import com.ratel.framework.http.FormsHttpEntity;
import com.ratel.modules.ehcache.RatelEchacheCacheProvider;
import com.ratel.modules.ldap.service.LdapService;
import com.ratel.modules.qywx.bean.Department;
import com.ratel.modules.qywx.bean.UserList;
import com.ratel.modules.qywx.bean.WxUser;
import com.ratel.modules.qywx.service.QywxService;
import com.ratel.modules.security.domain.vo.AuthCredentials;
import com.ratel.modules.security.service.AuthService;
import com.ratel.modules.system.domain.SysUser;
import com.ratel.modules.system.service.SysUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Api(tags = "企业微信：")
@RestController
@RequestMapping("/api/qywx")
@Slf4j
public class WxController {
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

	@RatelLog("登录")
    @ApiOperation(value = "登录")
    @GetMapping
    public ResponseEntity<Object> wxLogin(HttpServletRequest request,String code) {
		log.debug("==========="+code);
		SysUser user=this.qywxService.login(code);
		if(user!=null) {
			//getAuthUserMap(user.getUsername(), "123456", request);
			AuthCredentials authCredentials =new AuthCredentials();
			authCredentials.setAuthtype(AuthCredentials.QYWX);
			Map<String,Object> map=this.authService.getAuthUserMap(user.getUsername(), authCredentials, request);
			log.debug("登录返回报文："+map.toString());
			return FormsHttpEntity.ok(map);
		}else {
			return FormsHttpEntity.error(null, 1002, "用户不存在！");
		}
    }
	
	@RatelLog("测试登录")
    @ApiOperation(value = "测试登录")
	@GetMapping("/testLogin")
    public ResponseEntity<Object> testLogin(HttpServletRequest request,String id) {
		SysUser user=this.userService.findById(id);
		if(user!=null) {
			//getAuthUserMap(user.getUsername(), "123456", request);
			AuthCredentials authCredentials =new AuthCredentials();
			authCredentials.setAuthtype(AuthCredentials.QYWX);
			Map<String,Object> map=this.authService.getAuthUserMap(user.getUsername(), authCredentials, request);
			log.debug("登录返回报文："+map.toString());
			return FormsHttpEntity.ok(map);
		}else {
			return FormsHttpEntity.error(null, 1002, "用户不存在！");
		}
    }
	
	@RatelLog("签名")
    @ApiOperation(value = "获取签名的信息，为微信的config 提供参数！")
	@GetMapping("/sign")
    public ResponseEntity<Object> wxSign(String url) {
		log.debug("进入签名接口-----------------------------！");
		Map<String, String> map;
		try {
			log.debug("签名url ----------------:"+url);
			map = this.qywxService.sign(url);
			log.debug("签名返回-----------------："+map);
			return FormsHttpEntity.ok(map);
		} catch (Exception e) {
			log.error(e.getMessage());
			return FormsHttpEntity.error(null, 1001, e.getMessage());
		}
    }
	
	@RatelLog("1同步企业微信部门")
    @ApiOperation(value = "1同步企业微信部门")
	@GetMapping("/getdept")
    public ResponseEntity<Object> saveDeptByWx() {
		try {
			this.qywxService.saveDeptByWx();
			ratelEchacheCacheProvider.getCache("sysDept").removeAll();
			return FormsHttpEntity.ok();
		} catch (Exception e) {
			log.error(e.getMessage());
		
			ldapService.saveError("getdept","1同步企业微信部门",e.getMessage().length()>4000?e.getMessage().substring(0,4000):e.getMessage(),"失败");
			return FormsHttpEntity.error(null, 1001, e.getMessage());
		}
    }
	
	@RatelLog("1同步企业微信用户")
    @ApiOperation(value = "1同步企业微信用户")
	@GetMapping("/getuser")
    public ResponseEntity<Object> saveUserByWx() {
		try {
			String password=passwordEncoder.encode(defaultPsw);
			this.qywxService.saveUserByWx(password);
			ratelEchacheCacheProvider.getCache("sysUser").removeAll();
			ratelEchacheCacheProvider.getCache("sysRole").removeAll();
			return FormsHttpEntity.ok();
		} catch (Exception e) {
			log.error(e.getMessage());
			ldapService.saveError("getuser","1同步企业微信用户",e.getMessage().length()>4000?e.getMessage().substring(0,4000):e.getMessage(),"失败");
			return FormsHttpEntity.error(null, 1001, e.getMessage());
		}
    }
	
	
/*	@RatelLog("测试缓存")
    @ApiOperation(value = "测试缓存")
	@GetMapping("/test_cache")
    public ResponseEntity<Object> test() {
		if(this.cache.get("date")==null) {
			log.debug("--------没有命中");
			this.cache.set("date","1000",1);
			return FormsHttpEntity.ok("没有命中");
		}else {
			log.debug("-------命中");
			return FormsHttpEntity.ok("命中:"+this.cache.get("date"));
		}
		
		
    }*/

	@RatelLog("下载录音到本地")
	@ApiOperation(value ="录音本地化")
	@GetMapping("downLoadVoice")
	public ResponseEntity<Object>  downLoadVoice(String mediaId){
		String path = "";
		try {
			path = this.qywxService.getInputStream(mediaId);
			return FormsHttpEntity.ok(path);
		} catch (Exception e) {
			log.error(e.getMessage());
			return FormsHttpEntity.error(null, 1001, e.getMessage());
		}
	}

	public static void main(String[] args) {
		/*String _url="";
		try {
			String temp_url="http://jsc.ftzkm.com/reportClient";
			_url=URLEncoder.encode(temp_url,"UTF-8");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		String url="https://open.weixin.qq.com/connect/oauth2/authorize?appid=wwfc1b83e4292d0679"+"&redirect_uri="+_url+"&response_type=code&scope=snsapi_base&#wechat_redirect";
		System.out.println(url);*/

		try {
			String url=URLEncoder.encode("http://jsc.ftzkm.com/reportClient/#/login","utf-8");
			System.out.println(url);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println();
	}
	@RatelLog("2测试新增成员")
	@ApiOperation(value = "2测试新增成员")
	@GetMapping("/createQywxUser")
	public ResponseEntity<Object> createQywxUser() {
		try {
			WxUser wu = new WxUser();
			wu.setEnable(1);
			wu.setEmail("zhangsan@gzdev.com");
			wu.setName("zhangsan");
			wu.setAlias("jackzhang");
			wu.setGender("1");
			List<Integer> dep = new ArrayList<>();
			dep.add(Integer.parseInt("1"));
			dep.add(Integer.parseInt("2"));
			wu.setDepartment(dep);
			wu.setMobile("13800000000");
			List<Integer> orL = new ArrayList<>();
			orL.add(Integer.parseInt("10"));
			orL.add(Integer.parseInt("40"));
			wu.setOrder(orL);
			wu.setPosition("");
			wu.setUserid("zhangsan");

			Map m = this.qywxService.createQywxUser(wu);
			if("0".equals(m.get("errcode").toString())){
				return FormsHttpEntity.ok(m);
			}else{
				return FormsHttpEntity.error(null, 1001,m.toString());
			}

		} catch (Exception e) {
			log.error(e.getMessage());
			return FormsHttpEntity.error(null, 1001, e.getMessage());
		}
	}
	@RatelLog("2测试修改成员")
	@ApiOperation(value = "2测试修改成员")
	@GetMapping("/updateQywxUser")
	public ResponseEntity<Object> updateQywxUser() {
		try {

			WxUser wu = new WxUser();
			wu.setEnable(1);
			wu.setEmail("zhangsan@gzdev.com");
			wu.setName("lisi");
			wu.setAlias("jackzhang");
			wu.setGender("1");
			List<Integer> dep = new ArrayList<>();
			dep.add(Integer.parseInt("1"));
			dep.add(Integer.parseInt("2"));
			wu.setDepartment(dep);
			wu.setMobile("13800000000");
			List<Integer> orL = new ArrayList<>();
			orL.add(Integer.parseInt("10"));
			orL.add(Integer.parseInt("40"));
			wu.setOrder(orL);
			wu.setPosition("");
			wu.setUserid("zhangsan");
			Map m = this.qywxService.updateQywxUser(wu);
			if("0".equals(m.get("errcode").toString())){
				return FormsHttpEntity.ok(m);
			}else{
				return FormsHttpEntity.error(null, 1001,m.toString());
			}

		} catch (Exception e) {
			log.error(e.getMessage());
			return FormsHttpEntity.error(null, 1001, e.getMessage());
		}
	}
	@RatelLog("2测试删除成员")
	@ApiOperation(value = "2测试删除成员")
	@GetMapping("/deleteQywxUser")
	public ResponseEntity<Object> deleteQywxUser() {
		try {

			//Map m = this.qywxService.deleteQywxUser("zhangsan");
			Map m = this.qywxService.deleteQywxUser("297e32584db81e12014db88193cd0eda");
			if("0".equals(m.get("errcode").toString())){
				return FormsHttpEntity.ok(m);
			}else{
				return FormsHttpEntity.error(null, 1001,m.toString());
			}

		} catch (Exception e) {
			log.error(e.getMessage());
			return FormsHttpEntity.error(null, 1001, e.getMessage());
		}
	}
	@RatelLog("2测试查看成员")
	@ApiOperation(value = "2测试查看成员")
	@GetMapping("/queryQywxUser")
	public ResponseEntity<Object> queryQywxUser() {
		try {

			Map m = this.qywxService.queryQywxUser("zhangsan");
			if("0".equals(m.get("errcode").toString())){
				return FormsHttpEntity.ok(m);
			}else{
				return FormsHttpEntity.error(null, 1001,m.toString());
			}

		} catch (Exception e) {
			log.error(e.getMessage());
			return FormsHttpEntity.error(null, 1001, e.getMessage());
		}
	}

	@RatelLog("2同步用户数据导企业微信")
	@ApiOperation(value = "2同步用户数据导企业微信")
	@GetMapping("/saveUserToQywx")
	public ResponseEntity<Object> saveUserToQywx() {
		try {

			Map m = this.qywxService.saveUserToQywx();
			if("0".equals(m.get("errcode").toString())){
				return FormsHttpEntity.ok(m);
			}else{
				return FormsHttpEntity.error(null, 1001,m.toString());
			}

		} catch (Exception e) {
			log.error(e.getMessage());
			ldapService.saveError("saveUserToQywx","2同步用户数据导企业微信",e.getMessage().length()>4000?e.getMessage().substring(0,4000):e.getMessage(),"失败");
			return FormsHttpEntity.error(null, 1001, e.getMessage());
		}
	}



	@RatelLog("3测试新增qywx部门")
	@ApiOperation(value = "3测试新增qywx部门")
	@GetMapping("/createQywxDept")
	public ResponseEntity<Object> createQywxDept() {
		try {
			Department wu = new Department();
			//wu.setId(Long.parseLong("54321"));
			wu.setId(("54321"));
			wu.setDescription("54321");
			wu.setName("54321");
			wu.setOrder(54321);
			wu.setParentid("1");

			Map m = this.qywxService.createQywxDept(wu);

			if("0".equals(m.get("errcode").toString())){
				return FormsHttpEntity.ok(m);
			}else{
				return FormsHttpEntity.error(null, 1001,m.toString());
			}

		} catch (Exception e) {
			log.error(e.getMessage());
			return FormsHttpEntity.error(null, 1001, e.getMessage());
		}
	}
	@RatelLog("3测试修改qywx部门")
	@ApiOperation(value = "3测试修改qywx部门")
	@GetMapping("/updateQywxDept")
	public ResponseEntity<Object> updateQywxDept() {
		try {

			Department wu = new Department();
			//wu.setId(Long.parseLong("54321"));
			wu.setId(("54321"));
			wu.setDescription("2222");
			wu.setName("2222");
			wu.setOrder(54321);
			wu.setParentid("1");

			Map m = this.qywxService.updateQywxDept(wu);
			if("0".equals(m.get("errcode").toString())){
				return FormsHttpEntity.ok(m);
			}else{
				return FormsHttpEntity.error(null, 1001,m.toString());
			}

		} catch (Exception e) {
			log.error(e.getMessage());
			return FormsHttpEntity.error(null, 1001, e.getMessage());
		}
	}
	@RatelLog("3测试删除qywx部门")
	@ApiOperation(value = "3测试删除qywx部门")
	@GetMapping("/deleteQywxDept")
	public ResponseEntity<Object> deleteQywxDept() {
		try {

			Map m = this.qywxService.deleteQywxDept("54321");

			for(int i=33;i<=33;i++) {
				this.qywxService.deleteQywxDept(i+"");
			}
			if("0".equals(m.get("errcode").toString())){
				return FormsHttpEntity.ok(m);
			}else{
				return FormsHttpEntity.error(null, 1001,m.toString());
			}

		} catch (Exception e) {
			log.error(e.getMessage());
			return FormsHttpEntity.error(null, 1001, e.getMessage());
		}
	}
	@RatelLog("3测试查看qywx部门")
	@ApiOperation(value = "3测试查看qywx部门")
	@GetMapping("/queryQywxDept")
	public ResponseEntity<Object> queryQywxDept() {
		try {

			//Map m = this.qywxService.queryQywxDept("54321");
			Map m = this.qywxService.queryQywxDept("10");
			if("0".equals(m.get("errcode").toString())){
				return FormsHttpEntity.ok(m);
			}else{
				return FormsHttpEntity.error(null, 1001,m.toString());
			}

		} catch (Exception e) {
			log.error(e.getMessage());
			return FormsHttpEntity.error(null, 1001, e.getMessage());
		}
	}



	@RatelLog("3同步部门数据导企业微信")
	@ApiOperation(value = "3同步部门数据导企业微信")
	@GetMapping("/saveDeptToQywx")
	public ResponseEntity<Object> saveDeptToQywx() {
		try {

			Map m = this.qywxService.saveDeptToQywx();
			if("0".equals(m.get("errcode").toString())){
				return FormsHttpEntity.ok(m);
			}else{
				return FormsHttpEntity.error(null, 1001,m.toString());
			}

		} catch (Exception e) {
			log.error(e.getMessage());
			ldapService.saveError("saveDeptToQywx","3同步部门数据导企业微信",e.getMessage().length()>4000?e.getMessage().substring(0,4000):e.getMessage(),"失败");
			return FormsHttpEntity.error(null, 1001, e.getMessage());
		}
	}
}
