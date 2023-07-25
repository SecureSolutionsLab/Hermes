package zermia.monitor.state.bftsmart;

import zermia.monitor.state.ClientState;

public class BFTSmartClientState extends ClientState {
    private int operationID; // this client's current operation ID
    private int viewID; // view/epoch ID
    private int sequenceID; //  this client's current sequence ID
    private int lastMessageID; //  this client last Request's message ID
    private String lastProtocolPhase = ""; // protocol phase - pre-prepare, prepare, commit, etc.

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

    public String getLastProtocolPhase() {
        return lastProtocolPhase;
    }

    public void setLastProtocolPhase(String lastProtocolPhase) {
        this.lastProtocolPhase = lastProtocolPhase;
    }

    @Override
    public String toString() {
        return "BFTSmartClientState{" +
                "operationID=" + operationID +
                ", viewID=" + viewID +
                ", sequenceID=" + sequenceID +
                ", lastMessageID=" + lastMessageID +
                ", lastProtocolPhase=" + lastProtocolPhase +
                '}';
    }
}
