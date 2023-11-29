package com.testoptimal.exec;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import com.testoptimal.exception.MBTAbort;
import com.testoptimal.exec.FSM.ModelMgr;
import com.testoptimal.exec.FSM.State;
import com.testoptimal.exec.FSM.Transition;
import com.testoptimal.server.model.TestCmd;
import com.testoptimal.server.model.TestResult;

public class ModelRunnerAgent extends ModelRunner {
	
	private BlockingQueue<TestCmd> cmdQue = new ArrayBlockingQueue<>(200);
	private BlockingQueue<TestResult> resultQue = new ArrayBlockingQueue<>(200);
	private long agentTimeoutMilis;
	
	public ModelRunnerAgent(String sessionId_p, ModelMgr modelMgr_p, long agentTimeoutMillis_p) throws Exception {
		super(sessionId_p, modelMgr_p);
		this.execDir = new ExecutionDirector(this);

		this.agentTimeoutMilis = agentTimeoutMillis_p;
	}
		
	public void startMbt (String mbtMode_p, Map<String,Object> options_p) throws Exception, MBTAbort {
		super.startMbt(mbtMode_p, options_p);
    }
	
	@Override
	public void stopMbt() {
//		try {
//			this.resultQue.put(new TestResult(false, "interrupted"));
//		}
//		catch (Exception e) {
//			//
//		}
//		try {
//			this.cmdQue.put(new TestCmd(Type.ABORT, null, null));
//		}
//		catch (Exception e) {
//			//
//		}
		super.stopMbt();
	}
	

	
	/**
	 * for agent to fetch and wait for next cmd from model execution. 
	 * 
	 * @param timeoutMillis
	 * @return cmd object or null if timed out or errored while waiting/processing
	 */
	public TestCmd fetchCmd (long timeoutMillis_p) {
		try {
			TestCmd cmd = this.cmdQue.poll(timeoutMillis_p, TimeUnit.MILLISECONDS);
			return cmd;
		}
		catch (Exception e) {
			return new TestCmd(TestCmd.Type.TIMEOUT, null, null);
		}
	}

	/**
	 * for agent to send result for last cmd to model execution. 
	 * 
	 * @param result_p
	 * @throws InterruptedException 
	 */
	public void sendResult (TestResult result_p) throws InterruptedException {
		this.resultQue.put(result_p);
	}

	
	/**
	 * for model execution to send cmd to and wait for the result from remote agent. 
	 * 
	 * http://tutorials.jenkov.com/java-util-concurrent/blockingqueue.html
	 * 
	 * @param timeoutMillis
	 * @return result object or null if timed out or errored while waiting/processing
	 */
	private TestResult sendCmd (TestCmd cmdObj_p) {
		try {
			this.cmdQue.put(cmdObj_p);
			TestResult result = this.resultQue.poll(this.agentTimeoutMilis, TimeUnit.MILLISECONDS);
			return result;
		}
		catch (Exception e) {
			return null;
		}
	}


	@Override
	public void mbtFailed() throws MBTAbort {
		this.sendCmd( new TestCmd (TestCmd.Type.FAIL, null, null));
	}

	@Override
	public void mbtErrored(MBTAbort e_p) {
		this.sendCmd( new TestCmd (TestCmd.Type.ERROR, e_p.getMessage(), null));
	}
	
	@Override
	public void mbtAbort() {
		try {
			this.sendCmd( new TestCmd (TestCmd.Type.ABORT, null, null));
		}
		catch (Exception e) {
			logger.warn(e.toString());
		}
	}


	@Override
	public void enterMbtStart() throws MBTAbort {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitMbtStart() throws MBTAbort {
		try {
			this.sendCmd( new TestCmd (TestCmd.Type.MBT_START, null, null));
		}
		catch (Exception e) {
			try {
				this.resultQue.put( new TestResult(false, e.getMessage()));
				logger.warn(e.toString());
			}
			catch (InterruptedException e2) {
				throw new MBTAbort(e2.getMessage());
			}
		}		
	}

	@Override
	public void enterMbtEnd() {
		
	}

	@Override
	public void exitMbtEnd() {
		try {
			this.sendCmd( new TestCmd (TestCmd.Type.MBT_END, null, null));
		}
		catch (Exception e) {
			try {
				this.resultQue.put( new TestResult(false, e.getMessage()));
			}
			catch (Exception e2) {
				// ok
			}
			logger.warn(e.toString());
		}
		
	}

	@Override
	public void enterState(State stateObj_p) throws MBTAbort {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitState(State stateObj_p) throws MBTAbort {
		try {
			this.sendCmd( new TestCmd (TestCmd.Type.STATE, stateObj_p.getStateId(), null));
		}
		catch (Exception e) {
			try {
				this.resultQue.put( new TestResult(false, e.getMessage()));
			}
			catch (Exception e2) {
				// ok
			}
			logger.warn(e.toString());
		}
	}

	@Override
	public void enterTrans(Transition transObj_p) throws MBTAbort {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitTrans(Transition transObj_p) throws MBTAbort {
		try {
			this.sendCmd( new TestCmd (TestCmd.Type.STATE, transObj_p.getFromStateId(), transObj_p.getMarker()));
		}
		catch (Exception e) {
			try {
				this.resultQue.put( new TestResult(false, e.getMessage()));
			}
			catch (Exception e2) {
				// ok
			}
			logger.warn(e.toString());
		}
	}



}