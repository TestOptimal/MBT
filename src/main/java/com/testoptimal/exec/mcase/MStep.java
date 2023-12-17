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

package com.testoptimal.exec.mcase;

import groovy.lang.Closure;

public class MStep {
	/**
	 * 
	 * default to navigate to, if set to false, it will just jump to the target state/trans.
	 * use this setting to cause the MCase to ends without navigating to final states.
	 */
	public boolean naveTo = true;
	public boolean isState = false;
	public String toID;
	public Closure<Object> funcBefore;
	public Closure<Object> funcAfter;
	
	public Object performBefore (Object [] args) throws Exception {
		if (this.funcBefore == null) {
			return null;
		}
		else {
			return this.funcBefore.call(this, args);
		}
	}

	public Object performAfter (Object [] args) throws Exception {
		if (this.funcAfter == null) {
			return null;
		}
		else {
			return this.funcAfter.call(this, args);
		}
	}
}