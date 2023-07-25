package zermia.common.schedule;

import java.util.List;

public class GlobalFaultSchedule {
    private List<MonitorFaultSchedule> schedule;

    public List<MonitorFaultSchedule> getSchedule() {
        return schedule;
    }

    public void setSchedule(List<MonitorFaultSchedule> schedule) {
        this.schedule = schedule;
    }

    @Override
    public String toString() {
        return "GlobalFaultSchedule{" +
                "schedule=" + schedule +
                '}';
    }
}
