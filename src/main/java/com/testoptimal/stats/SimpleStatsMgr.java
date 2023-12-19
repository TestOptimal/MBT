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

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.testoptimal.exec.FSM.ModelMgr;
import com.testoptimal.stats.exec.ModelExec;
import com.testoptimal.util.FileUtil;

public class SimpleStatsMgr implements ManageStats {

	@Override
	public List<ModelExec> getStatsList(ModelMgr modelMgr_p) throws Exception {
		Gson gson = new Gson();
		File[] flist = FileUtil.getFileList(modelMgr_p.getStatsFolderPath());
		List<ModelExec> retList = Arrays.asList(flist).stream()
			.filter(f -> f.getName().endsWith(".json"))
			.map(f -> {
				try {
					ModelExec execStats = gson.fromJson(FileUtil.readFile(f.getAbsolutePath()).toString(), ModelExec.class);
					return execStats;
				}
				catch (Exception e) {
					// skip
					return null;
				}
			})
			.filter(s -> s!=null)
			.collect(Collectors.toList());
		retList.sort((o1, o2) -> o1.execSummary.startDT.compareTo(o2.execSummary.startDT));
		Collections.reverse(retList); // latest first
		return retList;
	}

	@Override
	public ModelExec getStats(ModelMgr modelMgr_p, String mbtSessID_p) throws Exception {
		Gson gson = new Gson();
		return gson.fromJson(FileUtil.readFile(modelMgr_p.getStatsFolderPath() + mbtSessID_p + ".json").toString(), ModelExec.class);
	}

	@Override
	public void save(ModelExec execStats_p) throws Exception {
		Gson gson = new Gson();
		String statsJson = gson.toJson(execStats_p);
		FileUtil.writeToFile(execStats_p.filePath, statsJson);
		StatsMgr.purgeStats(execStats_p.modelName);
		DashboardStats.addModelExec(execStats_p);
	}

	@Override
	public List<String> deleteStats(String modelName_p, List<String> mbtSessIDList_p) {
		List<String> dList = new java.util.ArrayList<>();
		String modelFolder = FileUtil.findModelFolder(modelName_p);
		if (FileUtil.exists(modelFolder)) {
			dList =  mbtSessIDList_p.stream()
				.map(m -> {
					try {
						FileUtil.deleteOneFile(modelFolder + "/stats/" + m + ".json");
						return m;
					}
					catch (Exception e) {
						return null;
					}
				})
				.filter(f -> f!=null)
				.collect(Collectors.toList());
		}
		return dList;
	}

}
