/***********************************************************************************************
 * Copyright (c) 2009-2024 TestOptimal.com
 *
 * This file is part of TestOptimal MBT.
 *
 * TestOptimal MBT is free software: you can redistribute it and/or modify it under the terms of 
 * the GNU General Public License as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *
 * TestOptimal MBT is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See 
 * the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with TestOptimal MBT. 
 * If not, see <https://www.gnu.org/licenses/>.
 ***********************************************************************************************/

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
