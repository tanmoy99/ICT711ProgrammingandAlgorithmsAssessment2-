public class PerformanceRecord {
    private String memberId;
    private String month; // e.g., "2025-03"
    private boolean achieved;
    private String notes;

    public PerformanceRecord(String memberId, String month, boolean achieved, String notes) {
        this.memberId = memberId;
        this.month = month;
        this.achieved = achieved;
        this.notes = notes;
    }

    public String getMemberId() { return memberId; }
    public String getMonth() { return month; }
    public boolean isAchieved() { return achieved; }
    public String getNotes() { return notes; }

    @Override
    public String toString() {
        return String.format("MemberID:%s | Month:%s | Achieved:%b | Notes:%s", memberId, month, achieved, notes);
    }
}
