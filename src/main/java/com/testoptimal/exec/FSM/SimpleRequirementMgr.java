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

package com.testoptimal.exec.FSM;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import com.testoptimal.server.model.Requirement;
import com.testoptimal.util.FileUtil;

public class SimpleRequirementMgr implements ManageRequirement {
	
	private static String FILENAME = "_Requirements.tsv";
	
	@Override
	public void init() throws Exception {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public List<Requirement> getRequirement(ModelMgr modelMgr_p) throws Exception {
		String filePath = modelMgr_p.getDatasetFolderPath() + FILENAME;
		if (!FileUtil.exists(filePath)) {
			return Arrays.asList(new Requirement[] {});
		}
		DataSet ds = new DataSet(filePath);
		List<Requirement> reqList = ds.rows.stream()
			.filter(r -> r.length >= 3)
			.map(r -> new Requirement(r[0], r[1], r[2]))
			.collect(Collectors.toList());
		return reqList;
	}

	@Override
	public void saveRequirement(ModelMgr modelMgr_p, List<Requirement> reqList_p) throws Exception {
		String filePath = modelMgr_p.getDatasetFolderPath() + FILENAME;
		String delimiter = "\t";
		StringBuffer outBuf = new StringBuffer();
		outBuf.append("name\tdesc\tpriority\n");
		reqList_p.stream().forEach(r -> {
			outBuf.append(r.name).append(delimiter).append(r.desc).append(delimiter).append(r.priority).append("\n");
		});
		FileUtil.writeToFile(filePath, outBuf);		
	}


}
