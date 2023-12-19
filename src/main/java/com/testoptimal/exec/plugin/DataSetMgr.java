/**
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
 * 
 */
package com.testoptimal.exec.plugin;

import java.util.Map;

import com.testoptimal.exec.ExecutionDirector;
import com.testoptimal.exec.FSM.DataSet;

/**
 * 
 */
public class DataSetMgr {
	private Map<String, DataSet> dsMap = new java.util.HashMap<>();
	
	public void init(ExecutionDirector execDir_p) {
		execDir_p.getDataSetList().forEach(d -> this.dsMap.put(d.dsName, d));
	}
	
	public DataSet dataset(String id_p) {
		return this.dsMap.get(id_p);
	}
	
	
}
