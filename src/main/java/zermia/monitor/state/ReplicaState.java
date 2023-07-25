package zermia.monitor.state;

import java.util.HashMap;

public abstract class ReplicaState extends NodeState {
    protected HashMap<Integer, ReplicaState> replicaStates;
    protected HashMap<Integer, ClientState> clientStates;

    protected ReplicaState(int id) {
        super(id);
        this.replicaStates = new HashMap<>();
        this.clientStates = new HashMap<>();
    }


}
