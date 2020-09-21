package com.ratel.modules.qywx.service;


import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ratel.framework.http.FormsHttpEntity;
import com.ratel.framework.modules.cache.RatelCacheProvider;
import com.ratel.modules.ldap.service.LdapService;
import com.ratel.modules.qywx.bean.*;
import com.ratel.modules.qywx.mapper.WxMapper;
import com.ratel.modules.qywx.utils.Sha;
import com.ratel.modules.system.domain.SysDept;
import com.ratel.modules.system.domain.SysUser;
import com.ratel.modules.system.repository.SysDeptRepository;
import com.ratel.modules.system.repository.SysUserRepository;
import com.ratel.modules.system.service.SysDeptService;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Consts;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;


@Order(value=999)
@Service
@Slf4j
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class QywxService {

	@Autowired
	private RatelCacheProvider cache;

	@Autowired
	private SysUserRepository sysUserRepository;

	@Autowired
	private SysDeptRepository sysDeptRepository;

	@Autowired
	private WxMapper wxMapper;

	@Autowired
	private LdapService ldapService;


	@Autowired
	private SysDeptService sysDeptService;





	@Value("${wx.secret}")
	private String secret;

	@Value("${wx.corpId}")
	private String corpId;

	@Value("${wx.agentId}")
	private String agentId;

	@Value("token|${wx.secret}|${wx.corpId}")
	private String tokenKey;

	@Value("ticketKey|${wx.secret}|${wx.corpId}")
	private String ticketKey;

	@Value("${wx.viewDetpIds}")
	private String viewDetpIds;

	@Value("${wx.downDeptIds}")
	private String downDeptIds;

	@Value("${wx.uploadDeptIds}")
	private String uploadDeptIds;

	/**
	 * 缓存有效期：秒
	 */
	@Value("${wx.expire}")
	private long expire;

	@Value("${ratel.file.path}")
	private String filePath;


	private AccessToken getToken() throws Exception{
		String key=tokenKey;
		if(cache.get(key)!=null) {
			log.debug("------------TOKEN命中"+key+"----------");
			AccessToken _temp=(AccessToken)cache.get(key);
			AccessToken a=(AccessToken)_temp.clone();
			log.debug("---------AccessToken:"+DateUtil.format(a.getDate(), "yyyy-MM-dd HH:mm:ss"));
			log.debug("---------AccessToken:"+((System.currentTimeMillis()-a.getDate().getTime()))/1000/60.0+"分钟");
			log.debug("---------"+this.expire);
			return a;
		} else {
			this.cache.del(this.ticketKey);
			return this.setToken();
		}
	}

	private AccessToken setToken() {
		String key=tokenKey;
		AccessToken	 token=null;
		String url="https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid="+this.corpId+"&corpsecret="+this.secret;
		log.debug("token-url:"+url);
		try {
			String msg=Request.Get(url).execute().returnContent().toString();
			log.debug("微信token返回："+msg);
			//Gson json=new Gson();
			//token=json.fromJson(msg, AccessToken.class);
			token=JSON.parseObject(msg, AccessToken.class);
			if("0".equals(token.getErrcode())) {
				log.debug("------------TOKEN从微信获取"+key+"----------");
				//table.put(key, token);
				log.debug("----------缓存毫秒："+expire);
				cache.set(key, token, expire);
				return token;
			}else {
				log.error("获取AccessToken错误："+token.getErrmsg());
			}
		}catch(Exception e) {
			log.error(e.getMessage());
		}
		return null;
	}

	private CurrentUserId getCurrentUserId(String code,String token) {
		String url="https://qyapi.weixin.qq.com/cgi-bin/user/getuserinfo?access_token="+token+"&code="+code;
		try {
			String msg=Request.Get(url).execute().returnContent().asString();
			CurrentUserId cu=JSON.parseObject(msg, CurrentUserId.class);
			if(0==cu.getErrcode()) {
				return cu;
			}else {
				log.error("获取CurrentUserId错误："+cu.getErrmsg());
				this.cache.del(tokenKey);
			}
		} catch (Exception e) {
			log.error("获取CurrentUserId错误："+e.getMessage());
		}
		return null;
	}

	private WxUser getUser(String userId,String token) {
		String url="https://qyapi.weixin.qq.com/cgi-bin/user/get?access_token="+token+"&userid="+userId;
		try {
			String msg=Request.Get(url).execute().returnContent().asString();
			log.debug(msg);
			WxUser sysUser=JSON.parseObject(msg, WxUser.class);
			if("0".equals(sysUser.getErrcode())) {
				return sysUser;
			}else {
				log.error("获取USER错误："+sysUser.getErrmsg());
			}
			return sysUser;
		} catch (Exception e) {
			log.error("获取getUser错误："+e.getMessage());
			return null;
		}
	}

	public SysUser login(String code) {
		try {
			AccessToken token=this.getToken();
			if(token!=null) {
				CurrentUserId cui=this.getCurrentUserId(code, token.getAccess_token());
				if(cui!=null) {
					WxUser wu=this.getUser(cui.getUserId(), token.getAccess_token());
					SysUser sysUser=this.sysUserRepository.findByUsername(wu.getUserid());
					return sysUser;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public Map<String,String> sign(String url) throws Exception{
		Map<String,String> map=new HashMap<String,String>();
		try {
			AccessToken token=this.getToken();
			if(token!=null) {
				/**
				 * 获取企业的jsapi_ticket
				 */
				Ticket ticket=this.getTicket(token.getAccess_token());//this.handle.getTicket(at.getAccess_token());

				if(ticket!=null) {
					/**
					 * 签名
					 */
					String noncestr = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
					/**
					 * 时间戳
					 */
					String timestamp = String.valueOf(System.currentTimeMillis() / 1000);//时间戳  


					String temp="jsapi_ticket="+ticket.getTicket()+"&noncestr="+noncestr+"&timestamp="+timestamp+"&url="+url;
					/**
					 * 生成签名
					 */
					String signature =Sha.SHA1(temp);
					map.put("signature", signature);
					map.put("noncestr", noncestr);
					map.put("timestamp", timestamp);
					map.put("corpId", corpId);
					map.put("secret", secret);

				}
			}
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}
		return map;
	}

	public Ticket getTicket(String token) throws Exception{
		//String key=domain.getSecret()+"|"+domain.getCorpId();
		if(this.cache.get(this.ticketKey)!=null) {
			Ticket _temp=(Ticket)cache.get(this.ticketKey);
			Ticket a=(Ticket)_temp.clone();
			log.debug("---------Ticke:"+DateUtil.format(a.getDate(), "yyyy-MM-dd HH:mm:ss"));
			log.debug("---------Ticke:"+((System.currentTimeMillis()-a.getDate().getTime()))/1000/60.0+"分钟");
			log.debug("---------"+this.expire);
			return a;
		} else {
			return this.setTicket(token);
		}
	}

	public Ticket setTicket(String token) throws Exception{
		String key=ticketKey;
		String url="https://qyapi.weixin.qq.com/cgi-bin/get_jsapi_ticket?access_token="+token;
		log.debug("ticket-url:"+url);
		String msg=Request.Get(url).execute().returnContent().toString();
		log.debug(msg);
		Ticket ticket=JSON.parseObject(msg, Ticket.class);
		log.debug("------------Ticket从微信获取"+key+"----------");
		if("0".equals(ticket.getErrcode())) {
			log.debug("----------缓存毫秒："+expire);
			cache.set(key, ticket, expire);
			return ticket;
		}else {
			log.error("获取USER错误："+ticket.getErrmsg());
		}
		return ticket;
	}

	/**
	 * 从企业微信获取部门数据
	 * @param id
	 * @return
	 */
	public DetpList getDetpFromWx(String id) {
		AccessToken token=null;
		DetpList list=null;
		try {
			token = this.getToken();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(token!=null) {
			String url="https://qyapi.weixin.qq.com/cgi-bin/department/list?access_token="+token.getAccess_token();
			if(id!=null) {
				url=url+"&id="+id;
			}
			log.debug("department/list:"+url);
			String msg=null;
			try {
				msg = Request.Get(url).execute().returnContent().toString();
			} catch (Exception e) {
				e.printStackTrace();
			}
			if(msg!=null) {
				log.debug(msg);
				list=JSON.parseObject(msg, DetpList.class);
			}
		}
		return list;
	}

	@Transactional(rollbackFor = Exception.class)
	public void saveDeptByWx() throws Exception{
		/**
		 * 处理全部数据
		 */
		String downDeptIdsX = downDeptIds;
		if(StringUtils.isBlank(downDeptIdsX)){
			downDeptIdsX = null;
		}
		DetpList list=this.getDetpFromWx(downDeptIdsX);
		List<SysDept> deptList=new ArrayList<SysDept>();
		if("0".equals(list.getErrcode())) {
			wxMapper.deleteAllDept();
			for(int i=0;i<list.getDepartment().size();i++) {
				Department d=list.getDepartment().get(i);
				SysDept sd=new SysDept();
				sd.setId(String.valueOf(d.getId()));
				sd.setName(d.getName());
				sd.setPid(d.getParentid());
				//sd.setDesPid("0");
				sd.setEnable("1");
				sd.setType("qywx");
				sd.setStatus("0");
				sd.setEnabled(true);
				sd.setSort(d.getOrder());
				if("0".equals(d.getParentid())){
					sd.setNumberId(Integer.parseInt(d.getId()));
					sd.setStatus("1");
				}
				//sd.setDescription(String.valueOf(times));
				//deptList.add(sd);
				//sysDeptRepository.save(sd);
				wxMapper.intoDept(sd);

			}

			//wxMapper.replaceIntoDept(deptList);
		}else{
			throw new Exception(list.getErrmsg());
		}
		/**
		 * 处理指派部门
		 */
		if(StringUtils.isNotBlank(viewDetpIds)) {
			String depts[]=this.viewDetpIds.split(",");
			List<String> ids=new ArrayList<String>();
			for(int i=0;i<depts.length;i++) {
				String id=depts[i];
				DetpList _list=this.getDetpFromWx(id);
				if("0".equals(_list.getErrcode())) {
					/*Department d=_list.getDepartment().get(i);
					ids.add(String.valueOf(d.getId()));*/
					for(int ii=0;ii<_list.getDepartment().size();ii++) {
						Department d=_list.getDepartment().get(ii);
						ids.add(String.valueOf(d.getId()));
					}
				}
			}
			if(ids.size()>0) {
				this.wxMapper.setViewDept(ids);
			}
		}

	}

	@Transactional(rollbackFor = Exception.class)
	public void saveUserByWx(String password) throws Exception{


		AccessToken token=null;
		try {
			token = this.getToken();
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		if(token!=null) {
			String downDeptIdsX = downDeptIds;
			if(StringUtils.isBlank(downDeptIdsX)){
				downDeptIdsX = null;
			}

			String url="https://qyapi.weixin.qq.com/cgi-bin/user/list?access_token="+token.getAccess_token()+"&department_id="+downDeptIdsX+"&fetch_child=1";
			log.debug("department/list:"+url);
			String msg=null;
			try {
				msg = Request.Get(url).execute().returnContent().toString();
			} catch (Exception e) {
				log.error(e.getMessage());
			}
			if(msg!=null) {
				log.debug(msg);
				UserList list=JSON.parseObject(msg, UserList.class);
				log.debug("----------"+list.getErrcode());
				List<SysUser> userList=new ArrayList<SysUser>();
				if("0".equals(list.getErrcode())) {

					wxMapper.deleteAllUser();
					for(int i=0;i<list.getUserlist().size();i++) {
						WxUser user=list.getUserlist().get(i);
						SysUser su=new SysUser();
						su.setId(user.getUserid());
						su.setUsername(user.getUserid());
						su.setEmail(user.getEmail());
						su.setPhone(user.getMobile());
						if(user.getStatus()==1) {
							su.setEnable("1");
							su.setEnabled(true);

						}else {
							su.setEnable("1");
							su.setEnabled(false);
							continue;
						}
						su.setType("qywx");
						su.setStatus("0");
						if("1".equals(user.getGender())) {
							su.setSex("男");
						}else if("2".equals(user.getGender())) {
							su.setSex("女");
						}else {
							su.setSex(null);
						}
						su.setSort(Long.valueOf(user.getOrder().get(0)));
						su.setDeptId(String.valueOf(user.getDepartment().get(0)));


						su.setNickName(user.getName());
						//userList.add(su);
						//sysUserRepository.save(su);
						wxMapper.intoUser(su);
					}
					//wxMapper.deleteAllUser();
					//wxMapper.replaceIntoUser(userList);
					/**
					 * 添加默认密码
					 */
					wxMapper.updatePassword(password);
					/**
					 * 添加默认权限
					 */
					List<SysUser> ulist=this.wxMapper.findNotRoleUser();
					if(ulist!=null&&ulist.size()>0) {
						//this.wxMapper.saveDefaultRole(ulist);
					}else {
						log.debug("-------------没有需要设置默认权限的用户");
					}

				}else{
					throw new Exception(list.getErrmsg());
				}
			}

			DetpList deptList =  getDetpFromWx("1984019283");



			url="https://qyapi.weixin.qq.com/cgi-bin/user/list?access_token="+token.getAccess_token()+"&department_id="+1984019283+"&fetch_child=1";
			log.debug("department/list:"+url);
			msg=null;
			try {
				msg = Request.Get(url).execute().returnContent().toString();
			} catch (Exception e) {
				log.error(e.getMessage());
			}
			if(msg!=null) {
				log.debug(msg);
				UserList list=JSON.parseObject(msg, UserList.class);
				log.debug("----------"+list.getErrcode());
				List<SysUser> userList=new ArrayList<SysUser>();
				if("0".equals(list.getErrcode())) {
					for(int i=0;i<list.getUserlist().size();i++) {
						WxUser user=list.getUserlist().get(i);
						SysUser su=new SysUser();
						su.setId(user.getUserid());
						su.setUsername(user.getUserid());
						su.setEmail(user.getEmail());
						su.setPhone(user.getMobile());
						if(user.getStatus()==1) {
							su.setEnable("1");
							su.setEnabled(true);
						}else {
							su.setEnable("1");
							su.setEnabled(false);
						}
						if("1".equals(user.getGender())) {
							su.setSex("男");
						}else if("2".equals(user.getGender())) {
							su.setSex("女");
						}else {
							su.setSex(null);
						}
						su.setSort(Long.valueOf(user.getOrder().get(0)));

						//循环标记
						Boolean flag = true;

						//循环用户所属部门
						for(int j=0;j<user.getDepartment().size();j++) {

							if(flag) {
								//循环创文总指办下的部门
								for(int d=0;d<deptList.getDepartment().size();d++) {
									Department department = deptList.getDepartment().get(d);
									if(Long.valueOf(user.getDepartment().get(j)).equals(department.getId())) {
										su.setDeptId(String.valueOf(user.getDepartment().get(j)));
										flag = false;
										break;
									}
								}
							}

						}


						su.setNickName(user.getName());
						userList.add(su);
					}

					wxMapper.replaceIntoUser(userList);
					/**
					 * 添加默认密码
					 */
					wxMapper.updatePassword(password);
					/**
					 * 添加默认权限
					 */
					List<SysUser> ulist=this.wxMapper.findNotRoleUser();
					if(ulist!=null&&ulist.size()>0) {
						this.wxMapper.saveDefaultRole(ulist);
					}else {
						log.debug("-------------没有需要设置默认权限的用户");
					}

				}
			}
		}
	}


	/**
	 *
	 * 根据文件id下载文件
	 *
	 *
	 *
	 * @param mediaId
	 *
	 *            媒体id
	 *
	 * @throws Exception
	 */

	/**
	 *
	 * 根据文件id下载文件
	 *
	 *
	 *
	 * @param mediaId
	 *
	 *            媒体id
	 *
	 * @throws Exception
	 */

	public String getInputStream(String mediaId) throws Exception {
		InputStream is = null;
		File amrPath = null;
		File mp3Path = null;
		String pathReturn = "";
//		String access_token = PropertiesLoader.readProperties("access_token", "wxHelp.properties");
//		String access_token = ;
		String url = "https://qyapi.weixin.qq.com/cgi-bin/media/get?access_token="
				+ this.getToken().getAccess_token() + "&media_id=" + mediaId;
		try {
			URL urlGet = new URL(url);
			HttpURLConnection http = (HttpURLConnection) urlGet
					.openConnection();
			http.setRequestMethod("GET"); // 必须是get方式请求
			http.setRequestProperty("Content-Type","audio/amr");
			http.setDoOutput(true);
			http.setDoInput(true);
			System.setProperty("sun.net.client.defaultConnectTimeout", "30000");// 连接超时30秒
			System.setProperty("sun.net.client.defaultReadTimeout", "30000"); // 读取超时30秒
			http.connect();
			// 获取文件转化为byte流
			is = http.getInputStream();

			//获取项目路径
			String path = filePath;
			//文件添加下级目录地址
			path += "static"+File.separator +"common" + File.separator +"voice";

			UUID uuid = UUID.randomUUID();
			String fileName = uuid.toString().replace("-", "");
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			File file = new File(path);
			File todayFile = new File(path + File.separator  + sdf.format(new Date()));
			amrPath = new File(todayFile + File.separator  + fileName + ".amr");
			mp3Path = new File(todayFile + File.separator  + fileName + ".mp3");


			//如果文件夹不存在则创建
			if  (!file.exists()  && !file.isDirectory()){
				file.mkdirs();
			}
			if  (!todayFile.exists()  && !todayFile.isDirectory()){
				todayFile.mkdir();
			}

			BufferedInputStream in = new BufferedInputStream(is);
			BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(amrPath));
			byte[] by = new byte[1024];
			int lend = 0;
			while((lend = in.read(by)) != -1){
				out.write(by,0,lend);
			}
			in.close();
			out.close();

			int code = changeAmrToMp3(amrPath.toString(),mp3Path.toString());

			if (code == 0){
				pathReturn  = "file"+File.separator +"static"+File.separator +"common"+File.separator +"voice"+File.separator + sdf.format(new Date())+ File.separator + fileName + ".mp3";
				if(amrPath.isFile() && amrPath.exists()){
					amrPath.delete();
				}

			}else{
				throw new Exception("录音转化失败");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return pathReturn;

	}

	public int changeAmrToMp3(String sourcePath, String targetPath) throws Exception {

		Properties props = System.getProperties();
		//String webroot = props.getProperty("user.home") ; //创建临时目录
		String webroot ="" ; //创建临时目录
		String osName = System.getProperties().getProperty("os.name");
		Process p = null;
		int code = 0;
		if (osName.toLowerCase().indexOf("linux") >= 0) {
			webroot = "";
		}

		Runtime run = null;

		try {

			run = Runtime.getRuntime();

			long start=System.currentTimeMillis();


			//执行ffmpeg.exe,前面是ffmpeg.exe的地址，中间是需要转换的文件地址，后面是转换后的文件地址。-i是转换方式，意思是可编码解码，mp3编码方式采用的是libmp3lame

			//wav转pcm

			//Process p=run.exec(new File(webroot).getAbsolutePath()+"/ffmpeg -y -i "+sourcePath+" -acodec pcm_s16le -f s16le -ac 1 -ar 16000 "+targetPath);

			//mp3转pcm
			log.debug("ffmpeg -y -i "+sourcePath+" -ar 44100 -ac 2 -acodec mp3 "+targetPath);
			p=run.exec("ffmpeg -y -i "+sourcePath+" -ar 44100 -ac 2 -acodec mp3 "+targetPath);

			code = p.waitFor();

			long end=System.currentTimeMillis();

			System.out.println(sourcePath+" convert success, costs:"+(end-start)+"ms");

		} catch (Exception e) {
			log.error(e.getMessage());
			e.printStackTrace();
//			 //释放进程
//			 p.getOutputStream().close();
//			 p.getInputStream().close();
//			 p.getErrorStream().close();

		}finally{
			p.getOutputStream().close();
			p.getInputStream().close();
			p.getErrorStream().close();
			p.destroy();
			//run调用lame解码器最后释放内存

			run.freeMemory();

		}

		return code;
	}
	@Transactional(rollbackFor = Exception.class)
	public Map createQywxUser(WxUser wu) {

		Map<String, Object> map1 = null;
		AccessToken token=null;
		try {
			token = this.getToken();
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		if(token!=null) {
			String url = "https://qyapi.weixin.qq.com/cgi-bin/user/create?access_token=" + token.getAccess_token();
			log.debug("access_token:" + token.getAccess_token());
			String msg = null;
			try {
				String mess=JSON.toJSONString(wu);//关键
				msg  = Request.Post(url).bodyString(mess, ContentType.APPLICATION_JSON).execute().returnContent().asString(Consts.UTF_8);
				log.debug(msg);
				//String转map
				map1= getStringToMap(msg);
			} catch (Exception e) {
				log.error(e.getMessage());
			}
		}
		return map1;
	}

	@Transactional(rollbackFor = Exception.class)
	public Map updateQywxUser(WxUser wu) {

		Map<String, Object> map1 = null;
		AccessToken token=null;
		try {
			token = this.getToken();
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		if(token!=null) {
			String url = "https://qyapi.weixin.qq.com/cgi-bin/user/update?access_token=" + token.getAccess_token();
			log.debug("access_token:" + token.getAccess_token());
			String msg = null;
			try {

				String mess=JSON.toJSONString(wu);//关键
				msg  = Request.Post(url).bodyString(mess, ContentType.APPLICATION_JSON).execute().returnContent().asString(Consts.UTF_8);
				log.debug(msg);
				//String转map
				map1= getStringToMap(msg);
			} catch (Exception e) {
				log.error(e.getMessage());
			}
		}
		return map1;
	}

	public Map deleteQywxUser(String id) {

		Map<String, Object> map1 = null;
		AccessToken token=null;
		try {
			token = this.getToken();
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		if(token!=null) {
			String url = "https://qyapi.weixin.qq.com/cgi-bin/user/delete?access_token=" + token.getAccess_token() + "&userid="+id;
			String msg = null;
			try {
				msg = Request.Get(url).execute().returnContent().toString();
				map1= getStringToMap(msg);
			} catch (Exception e) {
				log.error(e.getMessage());
			}

		}
		return map1;

	}

	public Map queryQywxUser(String id) {
		AccessToken token=null;
		Map list=null;
		try {
			token = this.getToken();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(token!=null) {
			//String url="https://qyapi.weixin.qq.com/cgi-bin/user/list?access_token="+token.getAccess_token()+"&department_id="+1+"&fetch_child=1";
			String url="https://qyapi.weixin.qq.com/cgi-bin/user/get?access_token="+token.getAccess_token()+"&userid="+id;
			String msg=null;
			try {
				msg = Request.Get(url).execute().returnContent().toString();
			} catch (Exception e) {
				log.error(e.getMessage());
			}

			if(msg!=null) {
				log.debug(msg);
				list=JSON.parseObject(msg, Map.class);
			}
		}
		return list;
	}

	public Map saveUserToQywx() throws Exception{
		Map list = new HashMap();
		List<SysUser> sysUsersList =  sysUserRepository.findAll();
		List saveSuccessList = new ArrayList<>();
		List savefailList = new ArrayList<>();

		List saveUpdateSuccessList = new ArrayList<>();
		List saveUpdatefailList = new ArrayList<>();
		for(int i=0;i<sysUsersList.size();i++) {
			SysUser su = sysUsersList.get(i);
			String status = su.getStatus();
			if("1".equals(status)){
				continue;
			}
			String type = su.getType();
//			if(!("ldap".equals(type) || "qywx".equals(type))){
//				continue;
//			}
			if(("-1".equals(type))){
				continue;
			}
			String phone = su.getPhone();//如果phone为空则不同步数据
			if(StringUtils.isBlank(phone)){
				continue;
			}
			/*if("ldap".equals(type)){
				continue;
			}*/
			/*if("qywx".equals(type)){
				continue;
			}*/
			WxUser wu = new WxUser();
			String ena = su.getEnabled().toString();
			int x  = -1;
			if("false".equals(ena)){
				x = 0;
			}else{
				x=1;
			}

			wu.setEnable(x);
			wu.setEmail(su.getEmail());
			wu.setName(su.getNickName());
			wu.setAlias(su.getUsername().length()>32?su.getUsername().substring(0,32):su.getUsername());
			wu.setGender(su.getSex());
			List<Integer> dep = new ArrayList<>();
			dep.add(su.getSysDept().getNumberId());
			wu.setDepartment(dep);
			wu.setMobile(su.getPhone());
			List<Integer> orL = new ArrayList<>();
			orL.add(Integer.parseInt(su.getSort().toString()));
			wu.setOrder(orL);
			wu.setPosition("");
			wu.setUserid(su.getId());

			if(null == status ||status.equals("0")){//未同步
				Map query =  queryQywxUser(su.getId());

				if(type.equals("qywx")){//企业微信 修改
					Map m = updateQywxUser(wu);

					if("0".equals(m.get("errcode").toString())){
						saveUpdateSuccessList.add(su.getId());
						//将同步完成的用户的id 的status 改成已同步
						su.setStatus("1");
					}else{
						saveUpdatefailList.add(su.getId());
						throw new Exception(m.get("errmsg").toString());
					}
				}else if(type.equals("ldap")){//其他 新增
					if("0".equals(query.get("errcode").toString())){//update
						Map m = updateQywxUser(wu);

						if("0".equals(m.get("errcode").toString())){
							saveUpdateSuccessList.add(su.getId());
							//将同步完成的用户的id 的status 改成已同步
							su.setStatus("1");
						}else{
							saveUpdatefailList.add(su.getId());
							throw new Exception(m.get("errmsg").toString());
						}
					}else{//insert
						Map m = createQywxUser(wu);
						//Map m = deleteQywxUser(su.getId());
						if("0".equals(m.get("errcode").toString())){
							saveSuccessList.add(su.getId());
							//su.setNumberId(Integer.parseInt(m.get("id").toString()));
							//将同步完成的用户的id 的status 改成已同步
							su.setStatus("1");
						}else{
							savefailList.add(su.getId()+m.get("errmsg").toString());
							throw new Exception(m.get("errmsg").toString());
						}
					}
				}else{
					//其他类型的什么操作也不做
					if("0".equals(query.get("errcode").toString())){//update
						Map m = updateQywxUser(wu);

						if("0".equals(m.get("errcode").toString())){
							saveUpdateSuccessList.add(su.getId());
							//将同步完成的用户的id 的status 改成已同步
							su.setStatus("1");
						}else{
							saveUpdatefailList.add(su.getId());
							throw new Exception(m.get("errmsg").toString());
						}
					}else{//insert
						Map m = createQywxUser(wu);
						//Map m = deleteQywxUser(su.getId());
						if("0".equals(m.get("errcode").toString())){
							saveSuccessList.add(su.getId());
							//su.setNumberId(Integer.parseInt(m.get("id").toString()));
							//将同步完成的用户的id 的status 改成已同步
							su.setStatus("1");
						}else{
							savefailList.add(su.getId()+m.get("errmsg").toString());
							throw new Exception(m.get("errmsg").toString());
						}
					}
				}
				//将同步完成的用户的id 的status 改成已同步
				sysUserRepository.save(su);
			}

		}

		ldapService.saveError("saveUserToQywx","同步用户数据导企业微信---新增","成功插入"+saveSuccessList.size()+"条记录--具体Id如下: "+saveSuccessList.toString(),"成功");
		ldapService.saveError("saveUserToQywx","同步用户数据导企业微信---新增","失败插入"+savefailList.size()+"条记录--具体Id如下: "+savefailList.toString(),"失败");

		ldapService.saveError("saveUserToQywx","同步用户数据导企业微信---修改","成功插入"+saveUpdateSuccessList.size()+"条记录--具体Id如下: "+saveUpdateSuccessList.toString(),"成功");
		ldapService.saveError("saveUserToQywx","同步用户数据导企业微信---修改","失败插入"+saveUpdatefailList.size()+"条记录--具体Id如下: "+saveUpdatefailList.toString(),"失败");

		if(saveUpdatefailList.size()==0 &&savefailList.size()==0){
			list.put("errcode","0");
		}else{
			list.put("errcode","1");
		}

		return list;
	}

	/**
	 *
	 * String转map
	 * @param str
	 * @return
	 */
	public static Map<String,Object> getStringToMap(String str){

		Map map =  JSONObject.parseObject(str);
		//System.out.println("StringToMap=>"+map);
		return map;

	}
	/*
	以下是部门的crud
	 */
	@Transactional(rollbackFor = Exception.class)
	public Map createQywxDept(Department wu) {

		Map<String, Object> map1 = null;
		AccessToken token=null;
		try {
			token = this.getToken();
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		if(token!=null) {
			String url = "https://qyapi.weixin.qq.com/cgi-bin/department/create?access_token=" + token.getAccess_token();
			log.debug("access_token:" + token.getAccess_token());
			String msg = null;
			try {
				String mess=JSON.toJSONString(wu);//关键
				msg  = Request.Post(url).bodyString(mess, ContentType.APPLICATION_JSON).execute().returnContent().asString(Consts.UTF_8);
				log.debug(msg);
				//String转map
				map1= getStringToMap(msg);
			} catch (Exception e) {
				log.error(e.getMessage());
			}
		}
		return map1;
	}
	@Transactional(rollbackFor = Exception.class)
	public Map updateQywxDept(Department wu) {

		Map<String, Object> map1 = null;
		AccessToken token=null;
		try {
			token = this.getToken();
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		if(token!=null) {
			String url = "https://qyapi.weixin.qq.com/cgi-bin/department/update?access_token=" + token.getAccess_token();
			log.debug("access_token:" + token.getAccess_token());
			String msg = null;
			try {

				String mess=JSON.toJSONString(wu);//关键
				msg  = Request.Post(url).bodyString(mess, ContentType.APPLICATION_JSON).execute().returnContent().asString(Consts.UTF_8);
				log.debug(msg);
				//String转map
				map1= getStringToMap(msg);
			} catch (Exception e) {
				log.error(e.getMessage());
			}
		}
		return map1;
	}

	public Map deleteQywxDept(String id) {

		Map<String, Object> map1 = null;
		AccessToken token=null;
		try {
			token = this.getToken();
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		if(token!=null) {
			String url = "https://qyapi.weixin.qq.com/cgi-bin/department/delete?access_token=" + token.getAccess_token() + "&id="+id;
			String msg = null;
			try {
				msg = Request.Get(url).execute().returnContent().toString();
				map1= getStringToMap(msg);
			} catch (Exception e) {
				log.error(e.getMessage());
			}

		}
		return map1;

	}

	public Map queryQywxDept(String id) {
		AccessToken token=null;
		Map list=null;
		try {
			token = this.getToken();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(token!=null) {
			//String url="https://qyapi.weixin.qq.com/cgi-bin/user/list?access_token="+token.getAccess_token()+"&department_id="+1+"&fetch_child=1";
			String url="https://qyapi.weixin.qq.com/cgi-bin/department/list?access_token="+token.getAccess_token()+"&id="+id;
			String msg=null;
			try {
				msg = Request.Get(url).execute().returnContent().toString();
			} catch (Exception e) {
				log.error(e.getMessage());
			}

			if(msg!=null) {
				log.debug(msg);
				list=JSON.parseObject(msg, Map.class);
			}
		}
		return list;
	}


	//递归获取所有的部门  ldap
	public List<SysDept> getldapDepts(List<SysDept> deptsList, List<SysDept> deptDtos) {
		for (SysDept dept : deptsList) {
			deptDtos.add(dept);

			List<SysDept> depts = sysDeptRepository.findByPid(dept.getId());
			if (depts != null && depts.size() != 0) {
				getldapDepts(depts, deptDtos);
			}
		}
		return deptDtos;
	}
	public void saveldapDepts(SysDept su, List saveSuccessList,List savefailList,List saveUpdateSuccessList,List saveUpdatefailList) throws Exception{
		//insert

		String status = su.getStatus();
		String type = su.getType();
		Department wu = new Department();
		String ena = su.getEnabled().toString();

		int x = -1;
		if (!"false".equals(ena)) {
			if (StringUtils.isNotBlank(su.getNumberId() + "") && !"null".equals(su.getNumberId() + "") && su.getNumberId() != 0) {//修改
				//wu.setId(Long.parseLong(su.getNumberId().toString()));
				wu.setId(su.getNumberId().toString());
			} else {
				//wu.setId(Long.parseLong(null)); // 新增不需要设置id
			}
			wu.setDescription(su.getDescription());
			wu.setName(su.getName().length()>32?su.getName().substring(0,32):su.getName());

			wu.setOrder(su.getSort() == null ?0:Integer.parseInt(su.getSort().toString()));
			Map   depts =  wxMapper.findById(su.getPid());
			String parentid ;
			if(null == depts  ){
                parentid = uploadDeptIds;
            }else{
                if (StringUtils.isNotBlank(depts.get("number_id") + "") && !"null".equals(depts.get("number_id") + "") && !"0".equals(depts.get("number_id") )) {//修改
                    parentid=depts.get("number_id")+"";
                }else{
                    parentid = depts.get("id").toString();
                }
            }

			wu.setParentid(parentid);
		}

		if (null == status || status.equals("0")) {//未同步

			Map query = queryQywxDept(su.getNumberId() + "");
			//其他 新增
			List departmentList = (List)query.get("department");
			String falg = "true";
			for(int i=0;i<departmentList.size();i++) {
				JSONObject    dm=  (JSONObject) departmentList.get(i);
				if(dm.getString("id").equals(su.getNumberId()+"") ){
					falg = "false";
					break;
				}

			}
			if (("0".equals(query.get("errcode").toString()) && ((List)query.get("department")).size() == 1) || "false".equals(falg)  ) {//update
				Map m = updateQywxDept(wu);

				if ("0".equals(m.get("errcode").toString())) {
					saveUpdateSuccessList.add(su.getId());
					//将同步完成的用户的id 的status 改成已同步
					su.setStatus("1");
				} else {
					saveUpdatefailList.add(su.getId());
					throw new Exception(m.get("errmsg").toString());
				}
			} else {//insert

				Map m = createQywxDept(wu);
				//Map m = deleteQywxUser(su.getId());
				if ("0".equals(m.get("errcode").toString())) {
					su.setNumberId(Integer.parseInt(m.get("id").toString()));
					//将同步完成的用户的id 的status 改成已同步
					su.setStatus("1");
					saveSuccessList.add(su.getId());
				} else {
					savefailList.add(su.getId());
					throw new Exception(m.get("errmsg").toString());
				}
			}
		}

		sysDeptRepository.save(su);
	}


	public Map saveDeptToQywx() throws   Exception{
		Map list = new HashMap();

		List saveSuccessList = new ArrayList<>();
		List savefailList = new ArrayList<>();

		List saveUpdateSuccessList = new ArrayList<>();
		List saveUpdatefailList = new ArrayList<>();

		//ldap 部门 新增
		List<SysDept> deptsList = new ArrayList<>();
		List<SysDept> deptDtos = new ArrayList<>();
		List<SysDept> sysDeptsList = new ArrayList<>();
		//SysDept sysDept = sysDeptRepository.getOne("root");
		List<SysDept> sysDeptL = sysDeptRepository.findByPid(uploadDeptIds);
		for(int k=0;k<sysDeptL.size();k++){
			SysDept sysDept = sysDeptL.get(k);
			deptsList.add(sysDept);

			sysDeptsList = getldapDepts(deptsList,deptDtos);

			for(int i=0;i<sysDeptsList.size();i++) {
				SysDept su = sysDeptsList.get(i);
				//ldap的部门的操作
				saveldapDepts(su,saveSuccessList,savefailList,saveUpdateSuccessList,saveUpdatefailList);

			}
		}



		//接下来是全部的sys_dept 的数据操作
		sysDeptsList = sysDeptRepository.findAll();


		for(int i=0;i<sysDeptsList.size();i++) {
			SysDept su = sysDeptsList.get(i);

			String status = su.getStatus();
			if("1".equals(status)){
				continue;
			}
			String type = su.getType();
			if("ldap".equals(type) || "qiye".equals(type) ||"jigou".equals(type)){
				continue;
			}

			Department wu = new Department();
			String ena = su.getEnabled().toString();

			int x  = -1;
			if(!"false".equals(ena)){
				if (StringUtils.isNotBlank(su.getNumberId() + "") && !"null".equals(su.getNumberId() + "") && su.getNumberId() != 0) {//修改
					//wu.setId(Long.parseLong(su.getNumberId().toString()));
					wu.setId((su.getNumberId().toString()));
				}else{
					//wu.setId(Long.parseLong(su.getId().toString())); // 新增不需要设置id
				}
				wu.setDescription(su.getDescription());
				//wu.setName(su.getName());
				//企业微信的部门名称。同一个层级的部门名称不能重复。长度限制为1~32个字符，字符不能包括\:?”<>｜
				wu.setName(su.getName().length()>32?su.getName().substring(0,32):su.getName());

				wu.setOrder(Integer.parseInt(su.getSort().toString()));
				wu.setParentid(su.getPid());
			}

			if(null == status ||status.equals("0")){//未同步

				if(type.equals("qywx")){//企业微信 修改

					//wu.setId(Long.parseLong(su.getId().toString()));
					wu.setId((su.getId().toString()));

					Map m = updateQywxDept(wu);

					if("0".equals(m.get("errcode").toString())  ){
						saveUpdateSuccessList.add(su.getId());
						su.setNumberId(Integer.parseInt(su.getId()));
						//将同步完成的用户的id 的status 改成已同步
						su.setStatus("1");
					}else{
						saveUpdatefailList.add(su.getId());
						throw new Exception(m.get("errmsg").toString());
					}
				}else{

					//除了ldap 和qywx 什么操作也不做
				}
				//将同步完成的用户的id 的status 改成已同步
				sysDeptRepository.save(su);
			}

		}

		ldapService.saveError("saveDeptToQywx","同步部门数据导企业微信---新增","成功插入"+saveSuccessList.size()+"条记录--具体Id如下: "+saveSuccessList.toString(),"成功");
		ldapService.saveError("saveDeptToQywx","同步部门数据导企业微信---新增","失败插入"+savefailList.size()+"条记录--具体Id如下: "+savefailList.toString(),"失败");

		ldapService.saveError("saveDeptToQywx","同步部门数据导企业微信---修改","成功插入"+saveUpdateSuccessList.size()+"条记录--具体Id如下: "+saveUpdateSuccessList.toString(),"成功");
		ldapService.saveError("saveDeptToQywx","同步部门数据导企业微信---修改","失败插入"+saveUpdatefailList.size()+"条记录--具体Id如下: "+saveUpdatefailList.toString(),"失败");

		if(saveUpdatefailList.size()==0 &&savefailList.size()==0){
			list.put("errcode","0");
		}else{
			list.put("errcode","1");
		}

		return list;
	}

}
