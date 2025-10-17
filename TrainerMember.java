public class TrainerMember extends Member {
    private String trainerName;
    private double trainerFee; // extra monthly fee for having a personal trainer

    public TrainerMember(String id, String firstName, String lastName, double baseFee, double performanceRating, String trainerName, double trainerFee) {
        super(id, firstName, lastName, baseFee, performanceRating, "Trainer");
        this.trainerName = trainerName;
        this.trainerFee = trainerFee;
    }

    public String getTrainerName() { return trainerName; }
    public double getTrainerFee() { return trainerFee; }
    public void setTrainerName(String trainerName) { this.trainerName = trainerName; }
    public void setTrainerFee(double trainerFee) { this.trainerFee = trainerFee; }

    /**
     * For trainer members: trainerFee added to base. Also apply smaller performance discounts.
     */
    @Override
    public double calculateFee() {
        double discount = 0.0;
        if (performanceRating >= 80.0) discount = 0.15;
        else if (performanceRating >= 65.0) discount = 0.08;
        double total = (baseFee + trainerFee) * (1.0 - discount);
        return total;
    }

    @Override
    public String toCSV() {
        // include trainerName and trainerFee in extra fields
        return String.join(",",
                escape(id),
                escape(firstName),
                escape(lastName),
                escape(membershipType),
                String.valueOf(baseFee),
                String.valueOf(performanceRating),
                escape(trainerName),
                String.valueOf(trainerFee));
    }

    private String escape(String s) {
        return s == null ? "" : s.replace(",", " ");
    }

    @Override
    public String toString() {
        return super.toString() + String.format(" | Trainer: %s | TrainerFee: %.2f", trainerName, trainerFee);
    }
}
