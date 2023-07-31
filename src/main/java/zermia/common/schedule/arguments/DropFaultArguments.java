package zermia.common.schedule.arguments;

import zermia.common.schedule.FaultArguments;

public class DropFaultArguments extends FaultArguments {
    private int to;

    public DropFaultArguments(int consecutiveRounds, int to) {
        super(consecutiveRounds);
        this.to = to;
    }

    public int getTo() {
        return to;
    }

    public void setTo(int to) {
        this.to = to;
    }

    @Override
    public String toString() {
        return "DropFaultArguments{" +
                "to=" + to +
                ", consecutive_rounds=" + consecutive_rounds +
                '}';
    }
}
