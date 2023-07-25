package zermia.monitor.state;

import java.util.concurrent.ConcurrentHashMap;

public abstract class ApplicationState {
    protected int N;
    protected int f;
    protected ConcurrentHashMap<Integer, ClientState> clientStates;
    protected ConcurrentHashMap<Integer, ReplicaState> replicaStates;

    protected ApplicationState(int n, int f) {
        this.N = n;
        this.f = f;
        this.clientStates = new ConcurrentHashMap<>();
        this.replicaStates = new ConcurrentHashMap<>();
    }

    public ClientState getClientState(int id) {
        return clientStates.get(id);
    }

    public void addClientState(ClientState state) {
        clientStates.put(state.getId(), state);
    }

    public ReplicaState getReplicaState(int id) {
        return replicaStates.get(id);
    }

    public void addReplicaState(ReplicaState state) {
        replicaStates.put(state.getId(), state);
    }

}
