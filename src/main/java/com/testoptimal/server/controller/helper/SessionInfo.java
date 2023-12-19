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

package com.testoptimal.server.controller.helper;

public class SessionInfo {
	public String mbtSessionID;
	public String modelName;
	public boolean execCompleted;
	
	public SessionInfo (String modelName_p, String mbtSessionID_p, boolean execCompleted_p) {
		this.modelName = modelName_p;
		this.mbtSessionID = mbtSessionID_p;
		this.execCompleted = execCompleted_p;
	}
}
