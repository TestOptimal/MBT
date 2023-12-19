/**
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
 * 
 */
package com.testoptimal.exec.FSM;

import com.testoptimal.exec.ExecutionDirector;
import com.testoptimal.exec.exception.MBTAbort;

import groovy.lang.Closure;

/**
 * 
 */
public class Action extends Transition {
	public Closure<Object> func;
	
	public Action (ExecutionDirector execDir_p, State s, Closure <Object> func_p) {
		super(s, s, "action", 1);
		this.func = func_p;
	}

	public boolean run() throws MBTAbort {
		try {
			this.func.call();
			return true;
		}
		catch (Exception e) {
			return false;
		}
	}
	
}
