package zermia.common.schedule;

import java.util.List;

public class MonitorFaultSchedule {
    private int monitor_id;
    private String target;
    private Boolean notify_coordinator = false;
    private Boolean update_coordinator = false;
    private List<FaultDescription> fault_schedule;

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public int getMonitor_id() {
        return monitor_id;
    }

    public void setMonitor_id(int monitor_id) {
        this.monitor_id = monitor_id;
    }

    public List<FaultDescription> getFault_schedule() {
        return fault_schedule;
    }

    public void setFault_schedule(List<FaultDescription> fault_schedule) {
        this.fault_schedule = fault_schedule;
    }

    public Boolean getNotify_coordinator() {
        return notify_coordinator;
    }

    public void setNotify_coordinator(Boolean notify_coordinator) {
        this.notify_coordinator = notify_coordinator;
    }

    public Boolean getUpdate_coordinator() {
        return update_coordinator;
    }

    public void setUpdate_coordinator(Boolean update_coordinator) {
        this.update_coordinator = update_coordinator;
    }

    @Override
    public String toString() {
        return "MonitorFaultSchedule{" +
                "monitor_id=" + monitor_id +
                ", target='" + target + '\'' +
                ", notify_coordinator=" + notify_coordinator +
                ", update_coordinator=" + update_coordinator +
                ", fault_schedule=" + fault_schedule +
                '}';
    }
}
