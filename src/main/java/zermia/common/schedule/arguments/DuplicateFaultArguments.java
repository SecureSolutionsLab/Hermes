package zermia.common.schedule.arguments;

import zermia.common.schedule.FaultArguments;

public class DuplicateFaultArguments extends FaultArguments {
    private int to;

    public DuplicateFaultArguments(int to, int consecutiveRounds) {
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
        return "DuplicateFaultArguments{" +
                "to=" + to +
                ", consecutive_rounds=" + consecutive_rounds +
                '}';
    }
}
