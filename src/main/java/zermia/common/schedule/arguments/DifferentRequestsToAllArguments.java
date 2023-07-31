package zermia.common.schedule.arguments;

import zermia.common.schedule.FaultArguments;

import java.util.Arrays;

public class DifferentRequestsToAllArguments extends FaultArguments {
    private int timestamp = -1;
    private byte[] operation = null;

    public DifferentRequestsToAllArguments(int consecutiveRounds, int timestamp, byte[] operation) {
        super(consecutiveRounds);
        this.timestamp = timestamp;
        this.operation = operation;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(int timestamp) {
        this.timestamp = timestamp;
    }

    public byte[] getOperation() {
        return operation;
    }

    public void setOperation(byte[] operation) {
        this.operation = operation;
    }

    @Override
    public String toString() {
        return "DifferentRequestsToAllArguments{" +
                "timestamp=" + timestamp +
                ", operation=" + Arrays.toString(operation) +
                ", consecutive_rounds=" + consecutive_rounds +
                '}';
    }
}
