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

package com.testoptimal.graphing;

import com.testoptimal.util.StringUtil;


public class MsgNode {

	public static final String ROOT = "_ROOT";
	
	private String name;
	private String uid;
	private String stereotype = null;
	private int sortNum = 0;

	public MsgNode(String nodeName_p, String uid_p, String stereotype_p) {
		this.name = nodeName_p;
		this.uid = uid_p;
		this.stereotype = stereotype_p;
	}
	
	public void setSortNum (int sortID_p) {
		this.sortNum = sortID_p;
	}
	int getSortNum () {
		return this.sortNum;
	}
	
	public String getNodeName() { 
		return this.name;
	}
	
	public String getUID() {
		return this.uid;
	}
	
	public boolean isSame(String uid_p) {
		if (this.uid.equalsIgnoreCase(uid_p)) {
			return true;
		}
		else return false;
	}
	
	// stereotype can only contain one char
	public String getStereotype() {
		if (this.stereotype!=null && this.stereotype.length()>1) {
			return this.stereotype.toLowerCase().substring(0,1);
		}
		else return this.stereotype;
	}
	
	public boolean hasStereotype(String stereotype_p) {
		if (this.stereotype==null) return false;
		return (this.stereotype.trim().equalsIgnoreCase(stereotype_p));
	}
	
	public void setStereotype(String stereotype_p) {
		if (StringUtil.isEmpty(this.stereotype)) {
			this.stereotype = stereotype_p;
		}
	}
}
