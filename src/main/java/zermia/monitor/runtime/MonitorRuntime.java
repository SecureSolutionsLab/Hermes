package zermia.monitor.runtime;

import zermia.common.schedule.FaultDescription;
import zermia.coordinator.config.CoordinatorConfiguration;
import zermia.monitor.runtime.config.MonitorConfiguration;
import zermia.monitor.runtime.proto.ZermiaCoordinatorStub;
import zermia.monitor.state.ApplicationState;

import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class MonitorRuntime {
    protected static MonitorRuntime monitorRuntime = null;
    protected ZermiaCoordinatorStub coordinatorStub = null;
    protected CoordinatorConfiguration props = null;
    protected MonitorConfiguration monitorConfiguration = null;
    protected ApplicationState crtTargetAppState = null;

    protected MonitorRuntime(int repID) {
        props = new CoordinatorConfiguration();
        props.loadProperties(CoordinatorConfiguration.defaultPropertiesFilePath);
        coordinatorStub = new ZermiaCoordinatorStub(props);
        monitorConfiguration = coordinatorStub.registerMonitor(repID);
        if (MonitorRuntime.monitorRuntime == null) {
            Logger.getLogger(MonitorRuntime.class.getName()).log(Level.INFO, "Instantiating a new MonitorRuntime on: " + repID);
            MonitorRuntime.monitorRuntime = this;
        }
        initApplicationState(repID);
    }

    public static MonitorRuntime getInstance() {
        return MonitorRuntime.monitorRuntime;
    }

    public abstract void initApplicationState(int repID);

    /**
     * Returns a fault based no current application state or NULL is no faults should be injected.
     *
     * @return
     */
    public abstract FaultDescription getNextFault2Inject();

    public int getID() {
        return monitorConfiguration.getMonitorID();
    }

    public int getGroupSize() {
        return monitorConfiguration.getN();
    }

    public ApplicationState getCrtTargetAppState() {
        return crtTargetAppState;
    }

    public void setCrtTargetAppState(ApplicationState crtTargetAppState) {
        this.crtTargetAppState = crtTargetAppState;
    }

    public zermia.monitor.state.ClientState getClientState(int id) {
        return crtTargetAppState.getClientState(id);
    }

    public void addClientState(zermia.monitor.state.ClientState state) {
        crtTargetAppState.addClientState(state);
    }

    public zermia.monitor.state.ReplicaState getReplicaState(int id) {
        return crtTargetAppState.getReplicaState(id);
    }

    public void addReplicaState(zermia.monitor.state.ReplicaState state) {
        crtTargetAppState.addReplicaState(state);
    }


        /*
        props.loadProperties(ZermiaProperties.defautlPropertiesFilePath);
        numberConsensusRounds=props.getEndTestRounds();

        z_lock.lock();
        replyArray = (ArrayList<ArrayList<String>>) replicaStub.runtimeFirstConnection(replicaID).clone();
        replicaFaultState = Boolean.valueOf(replyArray.get(0).get(0));
        faultScheduleSize = Integer.valueOf(replyArray.get(1).get(0));
        fRepList.addAll(replyArray.get(2));
        faultGroupSize = Integer.parseInt(replyArray.get(3).get(0));
        z_lock.unlock();

        if(replicaFaultState.equals(true)){
            Logger.getLogger(zermia.monitor.runtime.MonitorRuntime.class.getName()).log(Level.INFO, "Replica " + replicaID + " is FAULTY");
        } else {
            Logger.getLogger(zermia.monitor.runtime.MonitorRuntime.class.getName()).log(Level.INFO, "Replica " + replicaID + " is CORRECT");
        }

        if(replicaFaultState) {
            z_lock.lock();
            messageInfo();
            z_lock.unlock();
        }
        */

    //----------------------------------------------------------------------------------//
    //sending message to server to get which messages to inject monitor.faults
    public void messageInfo() {

        /*
        ArrayList<Boolean> messageInfoMsg = new ArrayList<Boolean>();

        messageInfoMsg = (ArrayList<Boolean>) replicaStub.runtimeSecondConnection(replicaID).clone();
        //System.out.println("teste " + messageInfoMsg);

        Vc = messageInfoMsg.get(0);	//Viewchange All
        Ck = messageInfoMsg.get(1);	//Checkpoint
        Fw = messageInfoMsg.get(2); //forwarded

        Fprimary = messageInfoMsg.get(3); //primary focused attack
        Creply = messageInfoMsg.get(4); //Client reply

        CsPrP = messageInfoMsg.get(5);
        CsPr = messageInfoMsg.get(6);
        CsCm = messageInfoMsg.get(7);

        VcStop = messageInfoMsg.get(8);
        VcStopD = messageInfoMsg.get(9);
        VcSync = messageInfoMsg.get(10);
        //System.out.println("teste " + messageInfoMsg.size());

        Logger.getLogger(zermia.monitor.runtime.MonitorRuntime.class.getName()).log(Level.INFO, "Message information passed");

         */
    }

//----------------------------------------------------------------------------------//	

    public void faultScheduler() {
        /*
        if(replicaFaultState.equals(true)) {
            ArrayList<String> replyArrayFP = new ArrayList<String>();
            ArrayList<String> replyArrayRT = new ArrayList<String>();

            z_lock.lock();
            for(int i=0;i<faultScheduleSize;i++) {
                replyArray2 = (ArrayList<ArrayList<String>>) replicaStub.runtimeFaultActivation(replicaID,i).clone();
                replyArrayFP.addAll(replyArray2.get(0));
                faultPamList.add(i,replyArrayFP);
                replyArrayRT.addAll(replyArray2.get(1));
                runTriggerList.add(i,replyArrayRT);

                replyArrayFP = new ArrayList<String>();
                replyArrayRT = new ArrayList<String>();
            }
            z_lock.unlock();

            //for the first and unique time
            runStart = Integer.valueOf(runTriggerList.get(0).get(0));
            runTriggers = Integer.valueOf(runTriggerList.get(0).get(1));
            runTrigSum = runStart + runTriggers;
            faultScheduleSize=faultScheduleSize-1;
        }

         */
    }

//----------------------------------------------------------------------------------//	

    public void statsSend() {
        /*
        z_lock.lock();
        replicaStub.runtimeStatsService(replicaID, timeFinish, numberOfMessagesSent);
        z_lock.unlock();

         */
    }

//----------------------------------------------------------------------------------//	

    public void statSend1sec() {
        /*
        z_lock.lock();
        timeSec = timeSec + 1;
        replicaStub.runtimeStat1SecService(replicaID, timeSec ,numberOfMessages1secSent);
        numberOfMessages1secSent = 0;
        z_lock.unlock();

         */
    }

//----------------------------------------------------------------------------------//	
    //For focused attacks on primary target = primary

//----------------------------------------------------------------------------------//


}
