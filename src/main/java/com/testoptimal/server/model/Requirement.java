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

public class Requirement implements Comparable <Requirement> {
//	public static enum Priority {high, medium, low};
	public String name;
	public String desc;
	public String priority;

	@Override
	public int compareTo(Requirement req_p) {
		return this.name.compareTo(req_p.name);
	}
	
	public Requirement(String name_p, String desc_p, String priority_p) {
		this.name = name_p;
		this.desc = desc_p;
		this.priority = priority_p;
	}
}
