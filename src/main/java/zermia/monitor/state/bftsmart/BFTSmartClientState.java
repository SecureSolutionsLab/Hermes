package zermia.monitor.state.bftsmart;

import zermia.monitor.state.ClientState;

public class BFTSmartClientState extends ClientState {
    private int operationID = -1; // this client's current operation ID
    private int viewID = -1; // view/epoch ID
    private int sequenceID = -1; //  this client's current sequence ID
    private int lastMessageID = -1; //  this client last Request's message ID
    private String protocolPhase = ""; // protocol phase - pre-prepare, prepare, commit, etc.
    private int crtPrimary = -1;

    public BFTSmartClientState(int id) {
        super(id);
    }

    public int getOperationID() {
        return operationID;
    }

    public void setOperationID(int operationID) {
        this.operationID = operationID;
    }

    public int getViewID() {
        return viewID;
    }

    public void setViewID(int viewID) {
        this.viewID = viewID;
    }

    public int getSequenceID() {
        return sequenceID;
    }

    public void setSequenceID(int sequenceID) {
        this.sequenceID = sequenceID;
    }

    public int getLastMessageID() {
        return lastMessageID;
    }

    public void setLastMessageID(int lastMessageID) {
        this.lastMessageID = lastMessageID;
    }

    public String getProtocolPhase() {
        return protocolPhase;
    }

    public void setProtocolPhase(String protocolPhase) {
        this.protocolPhase = protocolPhase;
    }

    public int getCrtPrimary() {
        return crtPrimary;
    }

    public void setCrtPrimary(int crtPrimary) {
        this.crtPrimary = crtPrimary;
    }

    @Override
    public String toString() {
        return "BFTSmartClientState{" +
                "id=" + id +
                ", operationID=" + operationID +
                ", viewID=" + viewID +
                ", sequenceID=" + sequenceID +
                ", lastMessageID=" + lastMessageID +
                ", protocolPhase='" + protocolPhase + "'" +
                ", crtPrimary=" + crtPrimary +
                '}';
    }
}
