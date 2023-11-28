package com.testoptimal.exec.navigator;

import com.testoptimal.exec.FSM.Transition;

public interface Sequencer {

	public Transition getNext();
	public boolean isStartingPath();
	public boolean isEndingPath();
	public int getPathCount();
}
