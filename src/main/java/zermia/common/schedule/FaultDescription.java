package zermia.common.schedule;

import zermia.monitor.state.NodeState;
import zermia.monitor.state.bftsmart.BFTSmartClientState;
import zermia.monitor.state.bftsmart.BFTSmartReplicaState;

public class FaultDescription {
    private String fault_type;
    private TriggerConditions trigger_conditions;
    private FaultArguments fault_arguments;

    public String getFault_type() {
        return fault_type;
    }

    public void setFault_type(String fault_type) {
        this.fault_type = fault_type;
    }

    public TriggerConditions getFault_trigger_conditions() {
        return trigger_conditions;
    }

    public void setFault_trigger_conditions(TriggerConditions trigger_conditions) {
        this.trigger_conditions = trigger_conditions;
    }

    public FaultArguments getFault_arguments() {
        return fault_arguments;
    }

    public void setFault_arguments(FaultArguments fault_arguments) {
        this.fault_arguments = fault_arguments;
    }

    @Override
    public String toString() {
        return "FaultDescription{" +
                "fault_type='" + fault_type + '\'' +
                ", trigger_conditions=" + trigger_conditions +
                ", fault_arguments=" + fault_arguments +
                '}';
    }

    public boolean canTriggerFault(NodeState crtState) {
        if(crtState instanceof BFTSmartClientState) {
            BFTSmartClientState cltState = (BFTSmartClientState)crtState;
            if(trigger_conditions.getConsensus_id() == cltState.getOperationID()) {
                return true;
            }
            if( ( trigger_conditions.getConsensus_id() == cltState.getOperationID() ||
                    trigger_conditions.getConsensus_id() == cltState.getSequenceID() )&&
                    trigger_conditions.getView_id() <= cltState.getViewID()) {
//                System.out.printf("[FaultDescription].canTriggerFault(): %s - %s\n", trigger_conditions, crtState);
                if(trigger_conditions.getProtocol().equals("null") && trigger_conditions.getProtocol_phase().equals("null")) {
                    return true;
                }
                else {
                    return trigger_conditions.getProtocol_phase().equals(cltState.getProtocolPhase());
                }
            }
        }
        if(crtState instanceof BFTSmartReplicaState) {
            BFTSmartReplicaState srvState = (BFTSmartReplicaState)crtState;
            if(trigger_conditions.getConsensus_id() == srvState.getCrtConsensusID() &&
                trigger_conditions.getView_id() <= srvState.getCrtViewID()) {
//                System.out.printf("[FaultDescription].canTriggerFault(): %s - %s\n", trigger_conditions, crtState);
                if(trigger_conditions.getProtocol().equals("null") && trigger_conditions.getProtocol_phase().equals("null")) {
                    return true;
                }
                else {
                    return trigger_conditions.getProtocol().equals(srvState.getCrtProtocol()) &&
                        trigger_conditions.getProtocol_phase().equals(srvState.getCrtProtocolPhase());
                }
            }
        }
        return false;
    }
}
