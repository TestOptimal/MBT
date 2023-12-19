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

package com.testoptimal.stats;

import java.util.List;

import com.testoptimal.exec.FSM.ModelMgr;
import com.testoptimal.stats.exec.ModelExec;

public interface ManageStats {
	public List<ModelExec> getStatsList(ModelMgr modelMgr_p) throws Exception;
	public ModelExec getStats (ModelMgr modelMgr_p, String mbtSessID_p) throws Exception;
	public void save(ModelExec execStats_p) throws Exception;
	
	// returns mbtSessID deleted
	public List<String> deleteStats (String modelName_p, List<String> mbtSessIDList_p);
}
