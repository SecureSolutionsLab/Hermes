package zermia.common.schedule;

public class FaultArguments {
    protected int consecutive_rounds = 0;

    public FaultArguments(int consecutive_rounds) {
        this.consecutive_rounds = consecutive_rounds;
    }

    public int getConsecutive_rounds() {
        return consecutive_rounds;
    }

    public void setConsecutive_rounds(int consecutive_rounds) {
        this.consecutive_rounds = consecutive_rounds;
    }


}
