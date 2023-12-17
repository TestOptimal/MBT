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

package com.testoptimal.exec.mcase;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Manages MCase objects. use "$SYS.getMCaseMgr()" to obtain this object.
 * 
 * @author yxl01
 *
 */
public class MCaseMgr {
	private List<MCase> mcaseList = new java.util.ArrayList<>();

	/**
	 * register an MCase for later execution by MCase Sequencer.
	 * @param name_p MCase name
	 * @return MCase object
	 */
	public MCase addMCase (String name_p) {
		MCase mcase = new MCase (name_p);
		this.mcaseList.add(mcase);
		return mcase;
	}

	/**
	 * returns the Map array of all page objects.
	 * @return
	 */
	public List<MCase> getMCaseList () {
		return this.mcaseList;
	}
	
	public List<MCase> getMCaseList (List<String> nameList_p) {
		if (nameList_p == null || nameList_p.isEmpty()) {
			return this.mcaseList;
		}
		
		List<MCase> retList = new java.util.ArrayList<>(nameList_p.size());
		for (MCase m: this.mcaseList) {
			String name = m.getName();
			for (String n: nameList_p) {
				if (name.contentEquals(n)) {
					retList.add(m);
					break;
				}
			}
		}
		return retList;
	}
	
	public MCase getMCase (String mcaseName_p) {
		List<MCase> list = this.mcaseList.stream().filter(m -> m.getName().equals(mcaseName_p)).collect(Collectors.toList());
		if (list.isEmpty()) return null;
		else return list.get(0);
	}
	
}
