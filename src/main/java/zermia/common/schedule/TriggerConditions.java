package zermia.common.schedule;

public class TriggerConditions {

    /** Consensus ID
     * Required CONSENSUS_ID for this triggering condition
     * Default value: -1 (ignored this when triggering fault)
     **/
    private int consensus_id = -1;

    /** View ID
     * Required VIEW_ID for this triggering condition
     * Default value: -1 (ignored this when triggering fault)
     **/
    private int view_id = -1;

    /** Protocol Phase
     * Required Protocol for this triggering condition
     * Default value: null (ignored this when triggering fault)
     * Possible values: CONSENSUS, LEADER-ELECTION, CHECKPOINT, etc.
     **/
    private String protocol = null;
    /** Protocol Phase
     * Required Protocol Phase for this triggering condition
     * Default value: null (ignored this when triggering fault)
     * Possible values: PRE-PREPARE, PREPARE, COMMIT, REQUEST, REPLY, VIEW-CHANGE, NEW-VIEW, etc.
     **/
    private String protocol_phase = null;
    private int delay_duration;
    private int consecutive_rounds;

    public int getView_id() {
        return view_id;
    }

    public void setView_id(int view_id) {
        this.view_id = view_id;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }
    public String getProtocol_phase() {
        return protocol_phase;
    }

    public void setProtocol_phase(String protocol_phase) {
        this.protocol_phase = protocol_phase;
    }

    public int getConsensus_id() {
        return consensus_id;
    }

    public void setConsensus_id(int consensus_id) {
        this.consensus_id = consensus_id;
    }

    public int getDelay_duration() {
        return delay_duration;
    }

    public void setDelay_duration(int delay_duration) {
        this.delay_duration = delay_duration;
    }

    public int getConsecutive_rounds() {
        return consecutive_rounds;
    }

    public void setConsecutive_rounds(int consecutive_rounds) {
        this.consecutive_rounds = consecutive_rounds;
    }

    @Override
    public String toString() {
        return "TriggerConditions{" +
                "consensus_id=" + consensus_id +
                ", view_id=" + view_id +
                ", protocol='" + protocol + '\'' +
                ", protocol_phase='" + protocol_phase + '\'' +
                ", delay_duration=" + delay_duration +
                ", consecutive_rounds=" + consecutive_rounds +
                '}';
    }
}
