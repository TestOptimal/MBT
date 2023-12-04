package com.testoptimal.stats;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.testoptimal.exec.ModelRunner;
import com.testoptimal.exec.FSM.ModelMgr;
import com.testoptimal.server.config.Config;
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
			mbtSess = SessionMgr.getInstance().getMbtStarterForMbtSession(mbtSessID_p);
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

	public static DashboardStats getDashboardStats () {
		DashboardStats dstats = new DashboardStats();
		List<String> modelList = FileUtil.getModelList(new File(Config.getModelRoot()), false);
		modelList.stream().forEach(m -> {
			try {
				ModelMgr modelMgr = new ModelMgr(m.substring(0, m.lastIndexOf(".")));
				dstats.addModelExec(statsMgr.getStatsList(modelMgr));
			}
			catch (Exception e) {
				//
				logger.error(e.getMessage());
			}
		});
		return dstats;
	}

	public static int purgeStats(String modelName_p) {
		try {
			ModelMgr modelMgr = new ModelMgr(modelName_p);
			int keepNum = modelMgr.getScxmlNode().getMiscNode().getMaxHistoryStat();
			List<String> mbtSessList = statsMgr.getStatsList(modelMgr).stream()
					.skip(keepNum)
					.map(s -> s.mbtSessID)
					.collect(Collectors.toList());
			int deletedCount = statsMgr.deleteStats(modelName_p, mbtSessList);
			return deletedCount;
		}
		catch (Exception e) {
			logger.warn("Error cleaning up stats for model " + modelName_p);
			return 0;
		}
	}
}
