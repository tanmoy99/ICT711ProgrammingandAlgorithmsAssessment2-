import java.io.IOException;
import java.util.List;
import java.util.Scanner;

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
        // try to load file; if not exist, ask to create sample
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
        System.out.println("3. Update member information and save to file");
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
        // auto-save after add
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
        // special handling if TrainerMember and want to change trainer details
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
                if ("1".equals(act)) System.out.println(manager.issueReminder(id2));
                else if ("2".equals(act)) System.out.println(manager.issueAppreciation(id2));
                else if ("3".equals(act)) {
                    System.out.print("Enter discount percent (e.g., 0.10 for 10%): ");
                    double p = readDoubleWithDefault(0.10);
                    boolean ok = manager.awardDiscount(id2, p);
                    System.out.println(ok ? "Discount applied." : "Member not found.");
                    try { manager.saveToFile(DATA_FILE); } catch (IOException e) { System.out.println("Failed to save after discount."); }
                } else System.out.println("Unknown action.");
                break;
            default:
                System.out.println("Unknown option.");
        }
    }

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

    public static void main(String[] args) {
        MainApp app = new MainApp();
        app.start();
    }
}
