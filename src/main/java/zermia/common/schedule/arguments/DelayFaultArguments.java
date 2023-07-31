package zermia.common.schedule.arguments;

import zermia.common.schedule.FaultArguments;

public class DelayFaultArguments extends FaultArguments {
    private int delayDuration;

    public DelayFaultArguments(int consecutiveRounds, int delayDuration) {
        super(consecutiveRounds);
        this.delayDuration = delayDuration;
    }

    public int getDelayDuration() {
        return delayDuration;
    }

    public void setDelayDuration(int delayDuration) {
        this.delayDuration = delayDuration;
    }

    @Override
    public String toString() {
        return "DelayFaultArguments{" +
                "delayDuration=" + delayDuration +
                ", consecutive_rounds=" + consecutive_rounds +
                '}';
    }
}
