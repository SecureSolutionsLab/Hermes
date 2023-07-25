package zermia.monitor.state;

public abstract class NodeState {
    protected int id;

    protected NodeState(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
