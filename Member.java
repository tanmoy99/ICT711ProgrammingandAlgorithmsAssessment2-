import java.util.Objects;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Member {
    protected String id;
    protected String firstName;
    protected String lastName;
    protected double baseFee; 
    protected double performanceRating; 
    protected String membershipType; 
    private final List<Double> monthlyPerformance = new ArrayList<>();

    public Member(String id, String firstName, String lastName, double baseFee, double performanceRating, String membershipType) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.baseFee = baseFee;
        this.performanceRating = performanceRating;
        this.membershipType = membershipType;
    }

    public void addMonthlyPerformance(double rating) {
        monthlyPerformance.add(rating);
        // keep the last 12 months  performance only
        if (monthlyPerformance.size() > 12) {
            monthlyPerformance.remove(0);
        }
    }
       public List<Double> getMonthlyPerformanceHistory() {
        return Collections.unmodifiableList(monthlyPerformance);
    }

  



    //use polymorphism to computed final fee
    public abstract double calculateFee();

    // For saving to CSV also overridden in subclass if needed to include extra fields
    public String toCSV() {
        return String.join(",",
                escape(id),
                escape(firstName),
                escape(lastName),
                escape(membershipType),
                String.valueOf(baseFee),
                String.valueOf(performanceRating),
                "", ""); // placeholders for extra subclass 
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
