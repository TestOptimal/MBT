package com.testoptimal.exec.navigator;

import com.testoptimal.exec.FSM.StateNetwork;
import com.testoptimal.exec.FSM.Transition;
import com.testoptimal.exec.exception.MBTAbort;

public interface Sequencer {

	public Transition getNext() throws MBTAbort;
	public boolean isStartingPath();
	public boolean isEndingPath();
	public int getPathCount();
	
	public StateNetwork getNetworkObj();
}
