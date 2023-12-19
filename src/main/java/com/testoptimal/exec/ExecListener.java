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

package com.testoptimal.exec;

import com.testoptimal.exec.FSM.State;
import com.testoptimal.exec.FSM.Transition;
import com.testoptimal.exec.exception.MBTAbort;

public interface ExecListener {

	public void enterMbtStart() throws MBTAbort;
	public void exitMbtStart() throws MBTAbort;
	public void enterMbtEnd() throws MBTAbort;
	public void exitMbtEnd() throws MBTAbort;
	public void mbtFailed() throws MBTAbort;
	public void mbtErrored(MBTAbort e);
	public void enterState(State stateObj_p) throws MBTAbort;
	public void exitState(State stateObj_p) throws MBTAbort;
	public void enterTrans(Transition transObj_p) throws MBTAbort;
	public void exitTrans(Transition transObj_p) throws MBTAbort;
	public void mbtAbort();
}
