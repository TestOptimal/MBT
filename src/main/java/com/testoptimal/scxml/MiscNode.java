package com.testoptimal.scxml;

import java.util.List;


public class MiscNode {
	private String typeCode = "misc";
	public String getTypeCode() { return this.typeCode; }
	
	private int canvasWidth = 1500;
	public int getCanvasWidth () { return this.canvasWidth; }
	
	private int canvasHeight = 1000;
	public int getCanvasHeight() { return this.canvasHeight; }
	
	/**
	 * Max Number of test cases allowed per MBT Execution.
	 */
	private int maxTestCaseNum = 100;
	public int getMaxTestCaseNum () { return this.maxTestCaseNum; }

	/**
	 * Max number of history stats to be kept.
	 */
	private int maxHistoryStat = 5;
	public int getMaxHistoryStat () { return this.maxHistoryStat; }



	private List<SwimlaneNode> swimlanes = new java.util.ArrayList<>();
	public List<SwimlaneNode> getSwimlanes() { 
		if (this.swimlanes==null) {
			this.swimlanes = new java.util.ArrayList<SwimlaneNode>();
		}
		return this.swimlanes; 
	}

	private List<BoxNode> boxes = new java.util.ArrayList<>();
}


