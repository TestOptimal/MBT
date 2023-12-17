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

package com.testoptimal.server.model.parser;

import java.util.List;

public class GherkinScenario {
	public String scenarioName;
	public String givenState;
	public List<String> whenScriptList = new java.util.ArrayList<>();
	public String thenState;
	
	public String getGivenState () {
		return GherkinModel.getCode(this.givenState);
	}

	public String getThenState () {
		return GherkinModel.getCode(this.thenState);
	}
}
