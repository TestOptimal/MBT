package com.testoptimal.stats;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
