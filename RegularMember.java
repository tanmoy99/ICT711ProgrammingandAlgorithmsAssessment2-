public class RegularMember extends Member {

    public RegularMember(String id, String firstName, String lastName, double baseFee, double performanceRating) {
        super(id, firstName, lastName, baseFee, performanceRating, "Regular");
    }

    
     //Regular member: if performanceRating >= 75 -> 20% discount
     //if performanceRating >= 60 -> 10% discount
     //else no discount.

    @Override
    public double calculateFee() {
        double discount = 0.0;
        if (performanceRating >= 75.0) discount = 0.20;
        else if (performanceRating >= 60.0) discount = 0.10;
        return baseFee * (1.0 - discount);
    }

    @Override
    public String toCSV() {
        return super.toCSV();
    }
}
