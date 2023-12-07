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
