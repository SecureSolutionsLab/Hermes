package zermia.monitor.runtime.bftsmart;

import zermia.common.schedule.FaultDescription;
import zermia.monitor.state.NodeState;
import zermia.monitor.state.bftsmart.BFTSmartApplicationState;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BFTSmartMonitor extends zermia.monitor.runtime.MonitorRuntime {
    public BFTSmartMonitor(int id) {
        super(id);
        if(zermia.monitor.runtime.MonitorRuntime.monitorRuntime == null) {
            Logger.getLogger(zermia.monitor.runtime.MonitorRuntime.class.getName()).log(Level.INFO, "Instantiating new MonitorRuntime on replica: " + id);
            zermia.monitor.runtime.MonitorRuntime.monitorRuntime = this;
        }
    }

    @Override
    public void initApplicationState(int repID) {
        int n = props.getN();
        crtTargetAppState = new BFTSmartApplicationState(props.getN(), props.getF());
        System.out.println(this.monitorConfiguration.getSchedule());
    }

    @Override
    public FaultDescription getNextFault2Inject() {
        List<FaultDescription> schedule = monitorConfiguration.getSchedule();
        if (schedule.size() > 0) {
            FaultDescription fd = schedule.get(0);
            int nodeID = this.monitorConfiguration.getMonitorID();

            NodeState state = this.crtTargetAppState.getReplicaState(nodeID);
            if (state == null)
                state = this.crtTargetAppState.getClientState(nodeID);

            assert state !=  null;

            if (fd.canTriggerFault(state)) {
                schedule.remove(0);
                return fd;
            }
        }
        return null;
    }
}
