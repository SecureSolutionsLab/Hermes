package zermia.monitor.runtime.clients;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

public class ClientMonitorRuntime_todelete { //TODO: to delete
	public static zermia.monitor.runtime.clients.ClientMonitorRuntime_todelete ClientMonitorRuntime = new zermia.monitor.runtime.clients.ClientMonitorRuntime_todelete();
	private final zermia.monitor.runtime.proto.ZermiaCoordinatorStub clientStub = null;
	
	static ReentrantLock z_lock = new ReentrantLock();
	
	static volatile Integer faultyClientGroupSize;
	static volatile Integer baseClientID = 1000;  //change the base id here 
	static volatile Integer limitClientFaultyID = 0;
	static volatile Integer faultScheduleSize = 0;
	static volatile Integer runStart;
	static volatile Integer runTriggers;
	static volatile Integer runTrigSum;
	
	static ArrayList<ArrayList<String>> faultPamList = new ArrayList<ArrayList<String>>();
	static ArrayList<ArrayList<String>> runTriggerList = new ArrayList<ArrayList<String>>();

	AtomicBoolean firstTime = new AtomicBoolean(true);
	Boolean listIncreaseOnce = true;
	volatile Integer runListIterator = 0;
	
	//--FaultyReplicasList--//
	static ArrayList<String> fRepList = new ArrayList<String>();
	static ArrayList<String> repList = new ArrayList<String>();
	
	static Integer replicaGroupSize;
//----------------------------------------------------------------------------------//	
	
	public ClientMonitorRuntime_todelete() {
	}
	
	public static zermia.monitor.runtime.clients.ClientMonitorRuntime_todelete getInstance() {
		return ClientMonitorRuntime;
	}

//----------------------------------------------------------------------------------//	

	//Number of faulty clients
	public void setfaultClientNumber(Integer clientSize) {
		faultyClientGroupSize = clientSize;
	}
	
	public Integer setfaultClientNumber() {
		return faultyClientGroupSize;
	}

//----------------------------------------------------------------------------------//	
	
	public Integer getBaseClientID() {
		return baseClientID;
	}
	
	public Integer getLimitClientFaultyID() {
		return limitClientFaultyID;
	}
	
//----------------------------------------------------------------------------------//		
	public void increaseListIterator() {
		z_lock.lock();
		if(listIncreaseOnce) {
			runListIterator = runListIterator + 1;
			listIncreaseOnce = false;
		}
		z_lock.unlock();
	}
	
	public Integer getListIterator() {
		return runListIterator;
	}
	
	public void setBoolIterator(Boolean bo) {
		listIncreaseOnce = bo;
	}	
	
//----------------------------------------------------------------------------------//	

	public Integer getRunTriggers() {
		return runTriggers;
	}
	
	public void setRunTrigger(Integer runTr) {
		runTriggers = runTr;
	}	
	
//----------------------------------------------------------------------------------//	

	public Integer getRunTrigSum() {
		return	runTrigSum;		
	}
	
	public void setRunTrigSum(Integer runt) {
		runTrigSum = runt;
	}
	
//----------------------------------------------------------------------------------//	
	public Integer getRunStart() {
		return runStart;
	}
	
	public void setRunStart(Integer run) {
		runStart=run;
	}
		
	
//----------------------------------------------------------------------------------//	

	public void setFaultScheduleSize(Integer faultIterator) {
		faultScheduleSize = faultIterator;
	}
	
	public Integer getFaultScheduleSize() {
		return faultScheduleSize;
	}
	
//----------------------------------------------------------------------------------//	

	public ArrayList<ArrayList<String>> getFaultPamList(){
		return faultPamList;
	}
	
	public ArrayList<ArrayList<String>> getRunTriggerList(){
		return runTriggerList;
	}
		
//----------------------------------------------------------------------------------//	
	//Faulty replica list
	public ArrayList<String> getFRepList(){
		return fRepList;
	}
	
	public Integer getReplicaGroupSize() {
		return replicaGroupSize;
	}
	//all replicas list
	public ArrayList<String> getRepList(){
		return repList;
	}

//----------------------------------------------------------------------------------//	
	
	@SuppressWarnings("unchecked")
	public void InstanceStart() {
		/*
		ArrayList<ArrayList<String>> repS = new ArrayList<ArrayList<String>>();
		repS = (ArrayList<ArrayList<String>>) clientStub.runtimeClientFirstRequest().clone();
		
		
		faultyClientGroupSize = Integer.valueOf(repS.get(0).get(0));
		faultScheduleSize = Integer.valueOf(repS.get(1).get(0));
		fRepList.addAll(repS.get(2));
		replicaGroupSize = Integer.parseInt(repS.get(3).get(0));
		repList.addAll(repS.get(4));
		
		limitClientFaultyID = baseClientID + faultyClientGroupSize;
		
		ArrayList<String> replyArrayFP = new ArrayList<String>();
		ArrayList<String> replyArrayRT = new ArrayList<String>();
		
		z_lock.lock();
		if(firstTime.get()) {
		firstTime.set(false);
		ArrayList<ArrayList<String>> replyArray = new ArrayList<ArrayList<String>>();

		for(int i=0;i<faultScheduleSize;i++) {
			replyArray = (ArrayList<ArrayList<String>>) clientStub.runtimeClientFaultRequest(i).clone();
			replyArrayFP.addAll(replyArray.get(0));
			faultPamList.add(i,replyArrayFP);
			replyArrayRT.addAll(replyArray.get(1));
			runTriggerList.add(i,replyArrayRT);

			replyArrayFP = new ArrayList<String>();
			replyArrayRT = new ArrayList<String>();
			}
		
		runStart = Integer.valueOf(runTriggerList.get(0).get(0));
		runTriggers = Integer.valueOf(runTriggerList.get(0).get(1));
		runTrigSum = runStart + runTriggers;
		faultScheduleSize=faultScheduleSize-1;
		z_lock.unlock();
		}

		 */
	}
	
}
