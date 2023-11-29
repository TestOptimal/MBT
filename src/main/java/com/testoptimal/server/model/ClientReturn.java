package com.testoptimal.server.model;

import java.util.Map;

import com.google.gson.Gson;

public class ClientReturn {
	private static Gson gson = new Gson();
	
//	@ApiModelProperty(notes = "OK for success, others either alert or error")
	public String status;
	
	public String alertMessage;
	public String error;
	
	public static ClientReturn OK () {
		ClientReturn ret = new ClientReturn();
		ret.status = "OK";
		return ret;
	}
	
	public static ClientReturn OK (String msg_p) {
		ClientReturn ret = new ClientReturn();
		ret.status = "OK";
		ret.alertMessage = msg_p;
		return ret;
	}
	
	public static ClientReturn Alert (String msg) {
		ClientReturn ret = new ClientReturn();
		ret.status = "alert";
		ret.alertMessage = msg;
		return ret;
	}
	
	/**
	 * construct a map containing a single map/value entry
	 * @param key_p
	 * @param val_p
	 * @return
	 */
	public static Map<String, Object> map (String key_p, Object val_p) {
		Map<String, Object> ret = new java.util.HashMap<>();
		ret.put(key_p, val_p);
		return ret;
	}
	
	public static Map<String, Object> map () {
		Map<String, Object> ret = new java.util.HashMap<>();
		return ret;
	}
	
	/**
	 * 
	 * @param json_p
	 * @return
	 */
	public static ClientReturn fromJSON (String json_p) {
		try {
			return gson.fromJson(json_p, ClientReturn.class);
		}
		catch (Exception e) {
			return ClientReturn.Alert(json_p);
		}
	}
}
