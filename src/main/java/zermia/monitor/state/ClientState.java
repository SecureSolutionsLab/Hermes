package zermia.monitor.state;

import java.util.HashMap;

public abstract class ClientState extends NodeState {
    protected HashMap<Integer, ReplicaState> replicaStates;

    protected ClientState(int id) {
        super(id);
        replicaStates = new HashMap<>();
    }
}
