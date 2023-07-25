package zermia.monitor.state.bftsmart;

import zermia.monitor.state.ReplicaState;

public class BFTSmartReplicaState extends ReplicaState {
    private int crtConsensusID = -1; // consensus ID
    private int lastConsensusID = -1; // last consensus ID
    private int crtViewID = -1; // view/epoch ID
    private int crtPrimary = -1; // ID of current Primary replica
    private int regency = -1; // ID of current view/epoch
    private String crtProtocol = ""; // protocol - consensus, view change, leader election, etc.
    private String crtProtocolPhase = ""; // protocol phase - pre-prepare, prepare, commit, etc.
    private int leader = -1;

    public BFTSmartReplicaState(int id) {
        super(id);
    }

    public int getCrtConsensusID() {
        return crtConsensusID;
    }

    public void setCrtConsensusID(int crtConsensusID) {
        this.crtConsensusID = crtConsensusID;
    }

    public int getLastConsensusID() {
        return lastConsensusID;
    }

    public void setLastConsensusID(int lastConsensusID) {
        this.lastConsensusID = lastConsensusID;
    }

    public int getCrtViewID() {
        return crtViewID;
    }

    public void setCrtViewID(int crtViewID) {
        this.crtViewID = crtViewID;
    }

    public int getCrtPrimary() {
        return crtPrimary;
    }

    public void setCrtPrimary(int crtPrimary) {
        this.crtPrimary = crtPrimary;
    }

    public int getRegency() {
        return regency;
    }

    public void setRegency(int regency) {
        this.regency = regency;
    }

    public String getCrtProtocol() {
        return crtProtocol;
    }

    public void setCrtProtocol(String crtProtocol) {
        this.crtProtocol = crtProtocol;
    }

    public String getCrtProtocolPhase() {
        return crtProtocolPhase;
    }

    public void setCrtProtocolPhase(String crtProtocolPhase) {
        this.crtProtocolPhase = crtProtocolPhase;
    }

    public int getLeader() {
        return leader;
    }

    public void setLeader(int leader) {
        this.leader = leader;
    }

    @Override
    public String toString() {
        return "BFTSmartReplicaState{" +
                "crtConsensusID=" + crtConsensusID +
                ", lastConsensusID=" + lastConsensusID +
                ", crtViewID=" + crtViewID +
                ", crtPrimary=" + crtPrimary +
                ", regency=" + regency +
                ", crtProtocol='" + crtProtocol + '\'' +
                ", crtProtocolPhase='" + crtProtocolPhase + '\'' +
                ", leader=" + leader +
                '}';
    }
}
