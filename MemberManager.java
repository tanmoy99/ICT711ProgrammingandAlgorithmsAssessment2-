import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class MemberManager {
    private List<Member> members;
    private int idCounter = 1000; // simple id generator base

    public MemberManager() {
        this.members = new ArrayList<>();
    }

    // Generate unique ID (simple incremental)
    public String generateId() {
        idCounter += 1;
        return "M" + idCounter;
    }

    public void addMember(Member m) {
        members.add(m);
    }

    public boolean deleteMemberById(String id) {
        return members.removeIf(m -> m.getId().equalsIgnoreCase(id));
    }

    public Member findById(String id) {
        return members.stream().filter(m -> m.getId().equalsIgnoreCase(id)).findFirst().orElse(null);
    }

    public List<Member> findByName(String nameFragment) {
        String f = nameFragment.toLowerCase();
        return members.stream()
                .filter(m -> (m.getFirstName() + " " + m.getLastName()).toLowerCase().contains(f))
                .collect(Collectors.toList());
    }

    public List<Member> findByPerformanceThreshold(double minRating) {
        return members.stream().filter(m -> m.getPerformanceRating() >= minRating).collect(Collectors.toList());
    }

    public List<Member> getAllMembers() {
        return new ArrayList<>(members);
    }

    public void updateMemberDetails(String id, String newFirst, String newLast, Double newBaseFee, Double newPerf) {
        Member m = findById(id);
        if (m != null) {
            if (newFirst != null) m.setFirstName(newFirst);
            if (newLast != null) m.setLastName(newLast);
            if (newBaseFee != null) m.setBaseFee(newBaseFee);
            if (newPerf != null) m.setPerformanceRating(newPerf);
        }
    }

    // Issue reminder letter (simulate text output)
    public String issueReminder(String id) {
        Member m = findById(id);
        if (m == null) return "Member not found.";
        return String.format("Reminder: Dear %s %s (ID:%s), please pay your monthly fee of %.2f.",
                m.getFirstName(), m.getLastName(), m.getId(), m.calculateFee());
    }

    // Issue appreciation letter
    public String issueAppreciation(String id) {
        Member m = findById(id);
        if (m == null) return "Member not found.";
        return String.format("Appreciation: Congrats %s %s (ID:%s)! You achieved a performance rating of %.1f. Keep it up!",
                m.getFirstName(), m.getLastName(), m.getId(), m.getPerformanceRating());
    }

    // Award extra discount by directly reducing baseFee (administrative action)
    public boolean awardDiscount(String id, double percent) {
        Member m = findById(id);
        if (m == null) return false;
        double newBase = m.getBaseFee() * (1.0 - percent);
        m.setBaseFee(newBase);
        return true;
    }

    // Save members to CSV file
    public void saveToFile(String filepath) throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(filepath))) {
            // header
            pw.println("id,firstName,lastName,type,baseFee,performanceRating,extra1,extra2");
            for (Member m : members) {
                pw.println(m.toCSV());
            }
        }
    }

    // Load members from CSV. If file not found, throw IOException to caller.
    public void loadFromFile(String filepath) throws IOException {
        File f = new File(filepath);
        if (!f.exists()) {
            throw new FileNotFoundException("File not found: " + filepath);
        }

        List<Member> loaded = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line = br.readLine(); // header
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",", -1); // allow empty
                // expected: id,first,last,type,baseFee,perf,extra1,extra2
                if (parts.length < 6) continue; // skip malformed
                String id = parts[0].trim();
                String first = parts[1].trim();
                String last = parts[2].trim();
                String type = parts[3].trim();
                double baseFee = 0.0;
                double perf = 0.0;
                try {
                    baseFee = Double.parseDouble(parts[4].trim());
                } catch (NumberFormatException ex) { baseFee = 0.0; }
                try {
                    perf = Double.parseDouble(parts[5].trim());
                } catch (NumberFormatException ex) { perf = 0.0; }

                if ("Trainer".equalsIgnoreCase(type)) {
                    String trainerName = parts.length > 6 ? parts[6].trim() : "";
                    double trainerFee = 0.0;
                    if (parts.length > 7) {
                        try { trainerFee = Double.parseDouble(parts[7].trim()); } catch (Exception e) { trainerFee = 0.0; }
                    }
                    loaded.add(new TrainerMember(id, first, last, baseFee, perf, trainerName, trainerFee));
                } else { // default to Regular
                    loaded.add(new RegularMember(id, first, last, baseFee, perf));
                }

                // keep idCounter in sync
                try {
                    if (id.startsWith("M")) {
                        int num = Integer.parseInt(id.substring(1));
                        if (num > idCounter) idCounter = num;
                    }
                } catch (Exception ignored) {}
            }
        }
        this.members = loaded;
    }

    // Create sample file with 10 records (useful if initial file missing)
    public void createSampleFile(String filepath) throws IOException {
        // create 10 sample members. Replace group placeholders with actual names if you have them.
        members.clear();
        members.add(new RegularMember(generateId(), "Tanmoy", "Bhowmick", 50.0, 78.0)); // example: your name
        members.add(new RegularMember(generateId(), "GroupMember1", "Lastname", 45.0, 62.0));
        members.add(new RegularMember(generateId(), "GroupMember2", "Lastname", 55.0, 45.0));
        members.add(new TrainerMember(generateId(), "GroupMember3", "Lastname", 60.0, 82.0, "AliceTrainer", 30.0));
        members.add(new TrainerMember(generateId(), "GroupMember4", "Lastname", 65.0, 70.0, "BobTrainer", 25.0));
        members.add(new RegularMember(generateId(), "MemberA", "Smith", 40.0, 59.0));
        members.add(new RegularMember(generateId(), "MemberB", "Jones", 35.0, 88.0));
        members.add(new TrainerMember(generateId(), "MemberC", "Brown", 70.0, 90.0, "Cara", 40.0));
        members.add(new RegularMember(generateId(), "MemberD", "Davis", 30.0, 50.0));
        members.add(new RegularMember(generateId(), "MemberE", "Wilson", 48.0, 67.0));

        saveToFile(filepath);
    }
}
