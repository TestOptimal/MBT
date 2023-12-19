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
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.testoptimal.exec.ModelRunner;
import com.testoptimal.exec.FSM.ModelMgr;
import com.testoptimal.server.controller.helper.SessionMgr;
import com.testoptimal.stats.exec.ModelExec;
import com.testoptimal.util.FileUtil;

public class StatsMgr {
	private static Logger logger = LoggerFactory.getLogger(StatsMgr.class);

	private static ManageStats statsMgr;
	

	public static void instantiate (String classPath_p) throws Exception {
		if (statsMgr != null) {
			throw new Exception ("StatsMgr already instantiated");
		}
    	Class aClass = Class.forName(classPath_p);
    	Constructor constructor = aClass.getConstructor();
    	statsMgr = (ManageStats) constructor.newInstance();
	}
	
	public static ManageStats getInstance () {
		return statsMgr;
	}
	
	public static ModelExec findModelExec (ModelMgr modelMgr_p, String mbtSessID_p, String httpSessID_p) throws Exception {
		ModelRunner mbtSess;
		ModelExec modelExec = null;
		if (mbtSessID_p.equals("undefined")) {
			mbtSess = SessionMgr.getInstance().getMbtStarterForModel(modelMgr_p.getModelName(), httpSessID_p);
			if (mbtSess != null && mbtSess.getExecDirector().getExecStats() != null) {
				modelExec = mbtSess.getExecDirector().getExecStats();
			}
		}
		else {
			mbtSess = SessionMgr.getInstance().getMbtStarterForMbtSession(modelMgr_p.getModelName(), mbtSessID_p);
			if (mbtSess != null && mbtSess.getExecDirector().getExecStats() != null) {
				modelExec = mbtSess.getExecDirector().getExecStats();
			}
		}
		if (modelExec == null) {
			if (mbtSessID_p.equals("undefined")) {
				Optional <ModelExec> opt = StatsMgr.getInstance().getStatsList(modelMgr_p).stream().findFirst();
				modelExec = opt.orElse(null);
			}
			else {
				modelExec = StatsMgr.getInstance().getStats(modelMgr_p, mbtSessID_p);
			}
		}

		return modelExec;
	}	

	public static int purgeStats(String modelName_p) {
		try {
			ModelMgr modelMgr = new ModelMgr(modelName_p);
			int keepNum = modelMgr.getScxmlNode().getMiscNode().getMaxHistoryStat();
			List<String> mbtSessList = statsMgr.getStatsList(modelMgr).stream()
					.skip(keepNum)
					.map(s -> s.mbtSessID)
					.collect(Collectors.toList());
			List<String> dList = statsMgr.deleteStats(modelName_p, mbtSessList);
			// purge temp folder
			File f = new File(modelMgr.getTempFolderPath());
			Arrays.asList(f.list()).stream()
				.filter(n -> mbtSessList.contains(n))
				.forEach(n -> {
					try {
						FileUtil.recursiveDelete(new File (f, n));
					}
					catch (Exception e) {
						// ok
					}
				});
			return dList.size();
		}
		catch (Exception e) {
			logger.warn("Error cleaning up stats for model " + modelName_p);
			return 0;
		}
	}
}
