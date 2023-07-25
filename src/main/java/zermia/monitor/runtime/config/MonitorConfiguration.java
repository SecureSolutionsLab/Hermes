package zermia.monitor.runtime.config;

import zermia.common.schedule.FaultDescription;

import java.util.List;

public class MonitorConfiguration {
    public int monitorID; // shared with the corresponding bft replica/client
    public int f; // upperbound on the number of faults
    public int N; // total number of replicas is the system
    public boolean isFaulty; // not needed TODO: delete
    public List<FaultDescription> schedule; // fault schedule managed by this monitor (received when registering with the coordinator)

    public int getMonitorID() {
        return monitorID;
    }
    public void setMonitorID(int monitorID) {
        this.monitorID = monitorID;
    }

    public int getF() {
        return f;
    }

    public void setF(int f) {
        this.f = f;
    }

    public int getN() {
        return N;
    }

    public void setN(int n) {
        N = n;
    }

    public boolean isFaulty() {
        return isFaulty;
    }

    public void setFaulty(boolean faulty) {
        isFaulty = faulty;
    }

    public List<FaultDescription> getSchedule() {
        return schedule;
    }

    public void setSchedule(List<FaultDescription> schedule) {
        this.schedule = schedule;
    }

    @Override
    public String toString() {
        return String.format("[Monitor Configuration] MonitorID=%d, f=%d, N=%d, isFaulty=%b, Schedule=\n%s", monitorID, f, N, isFaulty, schedule.toString());
    }
}
