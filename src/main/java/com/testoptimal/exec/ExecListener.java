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
