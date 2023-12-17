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

package com.testoptimal.server.model.sys;

import java.util.Date;

import com.google.gson.Gson;
import com.testoptimal.util.StringUtil;

/**
 * {"timestamp":"2020-03-28T02:40:12.633+0000","status":500,"error":"Internal Server Error","message":"This is not SvrMgr Edition. Contact support@testoptimal.com to request for a license","path":"/api/v1/svrmgr/authLicense"}
 * @author yxl01
 *
 */
public class HttpErrorResp {
	
	public String timestamp;
	public String status;
	public String error;
	public String message;
	public String path;
	
	public static HttpErrorResp fromJSON (String json_p) {
		try {
			Gson gson = new Gson();
			return gson.fromJson(json_p, HttpErrorResp.class);
		}
		catch (Exception e) {
			HttpErrorResp ret = new HttpErrorResp();
			ret.status ="Errored";
			ret.timestamp = (new Date()).toString();
			ret.error = "Invalid json";
			ret.message = json_p;
			return ret;
		}
	}
	
	public String getMsg () {
		return StringUtil.isEmpty(this.error)? this.message: this.error;
	}
}
