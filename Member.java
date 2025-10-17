import java.util.Objects;

public abstract class Member {
    protected String id;
    protected String firstName;
    protected String lastName;
    protected double baseFee; // monthly base fee
    protected double performanceRating; // 0.0 - 100.0 (percentage)
    protected String membershipType; // "Regular" or "Trainer"

    public Member(String id, String firstName, String lastName, double baseFee, double performanceRating, String membershipType) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.baseFee = baseFee;
        this.performanceRating = performanceRating;
        this.membershipType = membershipType;
    }

    // Each subclass defines how final fee is computed (polymorphism)
    public abstract double calculateFee();

    // For saving to CSV (overridden in subclass if needed to include extra fields)
    public String toCSV() {
        // basic fields: id,firstName,lastName,type,baseFee,performanceRating,extra1,extra2
        return String.join(",",
                escape(id),
                escape(firstName),
                escape(lastName),
                escape(membershipType),
                String.valueOf(baseFee),
                String.valueOf(performanceRating),
                "", ""); // placeholders for subclass extras
    }

    private String escape(String s) {
        return s == null ? "" : s.replace(",", " ");
    }

    public String getId() { return id; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public double getBaseFee() { return baseFee; }
    public double getPerformanceRating() { return performanceRating; }
    public String getMembershipType() { return membershipType; }

    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setBaseFee(double baseFee) { this.baseFee = baseFee; }
    public void setPerformanceRating(double performanceRating) { this.performanceRating = performanceRating; }

    @Override
    public String toString() {
        return String.format("ID:%s | %s %s | Type:%s | BaseFee:%.2f | Perf:%.1f",
                id, firstName, lastName, membershipType, baseFee, performanceRating);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Member member = (Member) o;
        return Objects.equals(id, member.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
