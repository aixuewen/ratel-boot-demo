package com.ratel.modules.qywx.bean;

public class CurrentUserId implements java.io.Serializable{
    /**
	 * 
	 */
	private static final long serialVersionUID = -2091354088507413176L;
	
	private int errcode;
    private String errmsg;
    private String UserId;
    private String DeviceId;
	public int getErrcode() {
		return errcode;
	}
	public void setErrcode(int errcode) {
		this.errcode = errcode;
	}
	public String getErrmsg() {
		return errmsg;
	}
	public void setErrmsg(String errmsg) {
		this.errmsg = errmsg;
	}
	public String getUserId() {
		return UserId;
	}
	public void setUserId(String userId) {
		UserId = userId;
	}
	public String getDeviceId() {
		return DeviceId;
	}
	public void setDeviceId(String deviceId) {
		DeviceId = deviceId;
	}
	   
	   
}
