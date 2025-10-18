import java.io.IOException;
import java.util.List;
import java.util.Scanner;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

public class MainApp {
    private static final String DATA_FILE = "member_data.csv";
    private MemberManager manager;
    private Scanner scanner;

    public MainApp() {
        manager = new MemberManager();
        scanner = new Scanner(System.in);
    }

    public void start() {
        System.out.println("===== Member Management System =====");
        // try to load the csv file if the file is not created creat a sample file 
        try {
            manager.loadFromFile(DATA_FILE);
            System.out.println("Loaded members from " + DATA_FILE);
        } catch (IOException e) {
            System.out.println("No data file found (" + DATA_FILE + "). Creating sample data.");
            try {
                manager.createSampleFile(DATA_FILE);
                System.out.println("Sample data created in " + DATA_FILE + ". Re-loading...");
                manager.loadFromFile(DATA_FILE);
            } catch (IOException ex) {
                System.out.println("Failed to create sample data: " + ex.getMessage());
            }
        }

        boolean exit = false;
        while (!exit) {
            showMenu();
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    handleLoadNewFile();
                    break;
                case "2":
                    handleAddMember();
                    break;
                case "3":
                    handleUpdateMember();
                    break;
                case "4":
                    handleDeleteMember();
                    break;
                case "5":
                    handleViewQuery();
                    break;
                case "6":
                    handleSave();
                    break;
                case "7":
                    exit = true;
                    System.out.println("Exiting. Goodbye.");
                    break;
                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }

    private void showMenu() {
        System.out.println("\nMenu:");
        System.out.println("1. Load records from file");
        System.out.println("2. Add new member and save to file");
        System.out.println("3. Update member information and save to file and update member discount and review");
        System.out.println("4. Delete member and save to file");
        System.out.println("5. View / Query member details");
        System.out.println("6. Save current members to file");
        System.out.println("7. Exit");
        System.out.print("Please choose an option: ");
    }

    private void handleLoadNewFile() {
        System.out.print("Enter filename to load (or press enter to use default " + DATA_FILE + "): ");
        String f = scanner.nextLine().trim();
        if (f.isEmpty()) f = DATA_FILE;
        try {
            manager.loadFromFile(f);
            System.out.println("Loaded from " + f + ". Current members count: " + manager.getAllMembers().size());
        } catch (IOException e) {
            System.out.println("Failed to load file: " + e.getMessage());
        }
    }

    private void handleAddMember() {
        System.out.print("Enter member type (1=Regular, 2=Trainer): ");
        String t = scanner.nextLine().trim();
        System.out.print("First name: ");
        String first = scanner.nextLine().trim();
        System.out.print("Last name: ");
        String last = scanner.nextLine().trim();
        System.out.print("Base fee (number): ");
        double base = readDoubleWithDefault(50.0);
        System.out.print("Performance rating (0-100): ");
        double perf = readDoubleWithDefault(50.0);

        String id = manager.generateId();
        if ("2".equals(t)) {
            System.out.print("Trainer name: ");
            String trainerName = scanner.nextLine().trim();
            System.out.print("Trainer fee: ");
            double trainerFee = readDoubleWithDefault(20.0);
            TrainerMember tm = new TrainerMember(id, first, last, base, perf, trainerName, trainerFee);
            manager.addMember(tm);
            System.out.println("Added: " + tm);
        } else {
            RegularMember rm = new RegularMember(id, first, last, base, perf);
            manager.addMember(rm);
            System.out.println("Added: " + rm);
        }
        // save the details in the csv file
        try {
            manager.saveToFile(DATA_FILE);
            System.out.println("Saved changes to " + DATA_FILE);
        } catch (IOException e) {
            System.out.println("Failed to save: " + e.getMessage());
        }
    }

    private void handleUpdateMember() {
        System.out.print("Enter member ID to update: ");
        String id = scanner.nextLine().trim();
        Member m = manager.findById(id);
        if (m == null) {
            System.out.println("Member not found.");
            return;
        }
        System.out.println("Current: " + m);
        System.out.print("New first name (enter to skip): ");
        String nf = scanner.nextLine().trim();
        if (nf.isEmpty()) nf = null;
        System.out.print("New last name (enter to skip): ");
        String nl = scanner.nextLine().trim();
        if (nl.isEmpty()) nl = null;
        System.out.print("New base fee (enter to skip): ");
        String bf = scanner.nextLine().trim();
        Double nb = null;
        if (!bf.isEmpty()) {
            try { nb = Double.parseDouble(bf); } catch (NumberFormatException ex) { System.out.println("Invalid number — skipping base fee."); }
        }
        System.out.print("New performance rating (enter to skip): ");
        String pr = scanner.nextLine().trim();
        Double np = null;
        if (!pr.isEmpty()) {
            try { np = Double.parseDouble(pr); } catch (NumberFormatException ex) { System.out.println("Invalid number — skipping perf rating."); }
        }
        manager.updateMemberDetails(id, nf, nl, nb, np);
        // special  case handling if Trainer Member want to change trainer details
        if (m instanceof TrainerMember) {
            TrainerMember tm = (TrainerMember) manager.findById(id); // re-get
            System.out.print("Change trainer name? (enter to skip): ");
            String tn = scanner.nextLine().trim();
            if (!tn.isEmpty()) tm.setTrainerName(tn);
            System.out.print("Change trainer fee? (enter to skip): ");
            String tfe = scanner.nextLine().trim();
            if (!tfe.isEmpty()) {
                try { tm.setTrainerFee(Double.parseDouble(tfe)); } catch (NumberFormatException ex) { System.out.println("Invalid trainer fee, skip."); }
            }
        }
        System.out.println("Updated: " + manager.findById(id));
        // auto-save
        try {
            manager.saveToFile(DATA_FILE);
            System.out.println("Saved changes to " + DATA_FILE);
        } catch (IOException e) {
            System.out.println("Failed to save: " + e.getMessage());
        }
    }

    private void handleDeleteMember() {
        System.out.print("Enter ID to delete: ");
        String id = scanner.nextLine().trim();
        boolean ok = manager.deleteMemberById(id);
        if (ok) {
            System.out.println("Deleted member " + id);
            try {
                manager.saveToFile(DATA_FILE);
                System.out.println("Saved changes to " + DATA_FILE);
            } catch (IOException e) {
                System.out.println("Failed to save: " + e.getMessage());
            }
        } else {
            System.out.println("Member not found.");
        }
    }

    //view all the mmebers save in the csv file also can set the cutomer discount and sent reminder or appreciation
    private void handleViewQuery() {
        System.out.println("Query options: a) All  b) By ID  c) By name  d) By performance threshold  e) Issue reminder/appreciation/discount");
        System.out.print("Choose: ");
        String c = scanner.nextLine().trim().toLowerCase();
        switch (c) {
            case "a":
                List<Member> all = manager.getAllMembers();
                all.forEach(System.out::println);
                break;
            case "b":
                System.out.print("Enter ID: ");
                String id = scanner.nextLine().trim();
                Member m = manager.findById(id);
                if (m == null) System.out.println("Not found.");
                else {
                    System.out.println(m);
                    System.out.printf("Calculated monthly fee: %.2f%n", m.calculateFee());
                    // show monthly performance history
                    List<Double> history = m.getMonthlyPerformanceHistory();
                    if (history == null || history.isEmpty()) {
                        System.out.println("No monthly performance history available.");
                    } else {
                        System.out.println("Monthly performance history (oldest -> most recent):");
                        for (int i = 0; i < history.size(); i++) {
                            System.out.printf("  Month %d: %.1f%%%n", i + 1, history.get(i));
                        }
                        // also show simple summary
                        double avg = history.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
                        double best = history.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
                        double worst = history.stream().mapToDouble(Double::doubleValue).min().orElse(0.0);
                        System.out.printf("  Avg: %.1f%%  Best: %.1f%%  Worst: %.1f%%%n", avg, best, worst);
                    }
                }
                break;
            case "c":
                System.out.print("Enter name fragment: ");
                String frag = scanner.nextLine().trim();
                List<Member> found = manager.findByName(frag);
                if (found.isEmpty()) System.out.println("No matches.");
                else found.forEach(System.out::println);
                break;
            case "d":
                System.out.print("Enter minimum performance rating (0-100): ");
                double th = readDoubleWithDefault(60.0);
                List<Member> perfList = manager.findByPerformanceThreshold(th);
                if (perfList.isEmpty()) System.out.println("No members meet threshold.");
                else perfList.forEach(System.out::println);
                break;
            case "e":
                System.out.print("Action (1=reminder, 2=appreciation, 3=award discount): ");
                String act = scanner.nextLine().trim();
                System.out.print("Enter member ID: ");
                String id2 = scanner.nextLine().trim();
                if ("1".equals(act)) {
                    String text = manager.issueReminder(id2);
                    System.out.println(text);
                    String fileName = writeLetterFile("reminder", id2, buildReminderLetter(id2, text));
                    if (fileName != null) System.out.println("Reminder letter saved to: " + fileName);
                } else if ("2".equals(act)) {
                    String text = manager.issueAppreciation(id2);
                    System.out.println(text);
                    String fileName = writeLetterFile("appreciation", id2, buildAppreciationLetter(id2, text));
                    if (fileName != null) System.out.println("Appreciation letter saved to: " + fileName);
                } else if ("3".equals(act)) {
                    System.out.print("Enter discount percent (e.g., 0.10 for 10%): ");
                    double p = readDoubleWithDefault(0.10);
                    boolean ok = manager.awardDiscount(id2, p);
                    System.out.println(ok ? "Discount applied." : "Member not found.");
                    if (ok) {
                        // save a receipt file
                        String receipt = buildDiscountReceipt(id2, p);
                        String fileName = writeLetterFile("discount_receipt", id2, receipt);
                        if (fileName != null) System.out.println("Discount receipt saved to: " + fileName);
                        try { manager.saveToFile(DATA_FILE); } catch (IOException e) { System.out.println("Failed to save after discount."); }
                    }
                } else System.out.println("Unknown action.");
                break;
            default:
                System.out.println("Unknown option.");
        }
    }

    //save the details in file
    private void handleSave() {
        try {
            manager.saveToFile(DATA_FILE);
            System.out.println("Saved to " + DATA_FILE);
        } catch (IOException e) {
            System.out.println("Failed to save: " + e.getMessage());
        }
    }

    private double readDoubleWithDefault(double def) {
        String s = scanner.nextLine().trim();
        if (s.isEmpty()) return def;
        try { return Double.parseDouble(s); } catch (NumberFormatException e) {
            System.out.println("Invalid number, using default " + def);
            return def;
        }
    }

    // Helper to create and write cutomer discount and sent reminder or appreciation
    private String writeLetterFile(String prefix, String memberId, String content) {
        try {
            String ts = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss").format(LocalDateTime.now());
            String safeId = memberId.replaceAll("[^a-zA-Z0-9_-]", "_");
            String fname = prefix + "_" + safeId + "_" + ts + ".txt";
            Path p = Paths.get(fname);
            Files.write(p, content.getBytes(StandardCharsets.UTF_8));
            return p.toAbsolutePath().toString();
        } catch (IOException ex) {
            System.out.println("Failed to write file: " + ex.getMessage());
            return null;
        }
    }

    private String buildReminderLetter(String memberId, String summaryText) {
        Member m = manager.findById(memberId);
        String name = (m != null) ? (m.getFirstName() + " " + m.getLastName()) : memberId;
        StringBuilder sb = new StringBuilder();
        sb.append("Date: ").append(LocalDateTime.now().toLocalDate()).append(System.lineSeparator());
        sb.append("To: ").append(name).append(System.lineSeparator()).append(System.lineSeparator());
        sb.append("Subject: Activity Reminder").append(System.lineSeparator()).append(System.lineSeparator());
        sb.append(summaryText).append(System.lineSeparator()).append(System.lineSeparator());
        sb.append("Please contact the front desk if you need assistance with your program.").append(System.lineSeparator());
        return sb.toString();
    }

    private String buildAppreciationLetter(String memberId, String summaryText) {
        Member m = manager.findById(memberId);
        String name = (m != null) ? (m.getFirstName() + " " + m.getLastName()) : memberId;
        StringBuilder sb = new StringBuilder();
        sb.append("Date: ").append(LocalDateTime.now().toLocalDate()).append(System.lineSeparator());
        sb.append("To: ").append(name).append(System.lineSeparator()).append(System.lineSeparator());
        sb.append("Subject: Appreciation").append(System.lineSeparator()).append(System.lineSeparator());
        sb.append(summaryText).append(System.lineSeparator()).append(System.lineSeparator());
        sb.append("Congratulations and keep up the great work!").append(System.lineSeparator());
        return sb.toString();
    }

    private String buildDiscountReceipt(String memberId, double percent) {
        Member m = manager.findById(memberId);
        String name = (m != null) ? (m.getFirstName() + " " + m.getLastName()) : memberId;
        StringBuilder sb = new StringBuilder();
        sb.append("Date: ").append(LocalDateTime.now().toLocalDate()).append(System.lineSeparator());
        sb.append("Member: ").append(name).append(" (ID: ").append(memberId).append(")").append(System.lineSeparator()).append(System.lineSeparator());
        sb.append(String.format("A discount of %.2f%% has been applied to your monthly fee.%n", percent * 100.0));
        if (m != null) {
            sb.append(String.format("Previous base fee: %.2f%n", m.getBaseFee()));
            double newFee = m.calculateFee();
            sb.append(String.format("New monthly fee after discount: %.2f%n", newFee));
        }
        sb.append(System.lineSeparator()).append("Thank you.").append(System.lineSeparator());
        return sb.toString();
    }

    //fucntion to statrt the app
    public static void main(String[] args) {
        MainApp app = new MainApp();
        app.start();
    }
}