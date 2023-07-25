package zermia.monitor.state.bftsmart;

import zermia.monitor.state.ApplicationState;

public class BFTSmartApplicationState extends ApplicationState {
    public BFTSmartApplicationState(int n, int f) {
        super(n, f);
    }

    @Override
    public String toString() {
        return "BFTSmartApplicationState{" +
                "N=" + N +
                ", f=" + f +
                ", clientStates=" + clientStates +
                ", replicaStates=" + replicaStates +
                '}';
    }
}

