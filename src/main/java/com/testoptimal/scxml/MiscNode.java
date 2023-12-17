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


