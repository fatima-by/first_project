import java.util.Scanner;

// Class 8: Main (Integration)
// This is the entry point of the whole program.
// It creates the objects we need and starts the menu loop.
public class BloodPressureMonitor {

    public static void main(String[] args) {
        // Scanner reads whatever the user types in the terminal
        Scanner scanner = new Scanner(System.in);

        // DataStore is where all the saved readings will live
        DataStore dataStore = new DataStore();

        // MenuSystem controls what happens on the screen (the menu loop)
        MenuSystem menuSystem = new MenuSystem();

        // hand control over to the menu system, passing in the scanner and dataStore
        // so it can read input and save records
        menuSystem.run(scanner, dataStore);

        // close the scanner once the program is done, good practice
        scanner.close();
    }
}

// Class 1: Patient
// Just holds the basic details of one patient.
// Used briefly while collecting input, before the values get split
// apart into DataStore's parallel arrays.
class Patient {

    String name; // patient's full name
    String id;   // patient's ID number/code
    int age;     // patient's age in years

    // constructor: runs when we create a new Patient object
    // it fills in the fields above with whatever values we pass in
    public Patient(String name, String id, int age) {
        this.name = name;
        this.id = id;
        this.age = age;
    }
}

// Class 2: BPReading
// Represents ONE blood pressure reading for ONE patient.
// Like Patient, this is just used to carry the values around before
// they get unpacked into DataStore's parallel arrays.
class BPReading {

    Patient patient;   // which patient this reading belongs to
    int systolic;      // top number of the BP reading
    int diastolic;     // bottom number of the BP reading
    String category;   // result of the reading, e.g. "Normal", "Elevated"

    public BPReading(Patient patient, int systolic, int diastolic, String category) {
        this.patient = patient;
        this.systolic = systolic;
        this.diastolic = diastolic;
        this.category = category;
    }
}

// Class 3: InputValidator
// Contains only checking methods, does not print anything or store anything.
// Every method here returns true (input is okay) or false (input is bad).
class InputValidator {

    // checks the systolic/diastolic numbers are within a realistic human range
    // this stops silly numbers like 9999 or negative numbers from being saved
    public static boolean isValidReading(int systolic, int diastolic) {
        return (systolic >= 40 && systolic <= 260) && (diastolic >= 30 && diastolic <= 150);
    }

    // makes sure a text field (name or ID) is not empty or just spaces
    // text.trim() removes spaces from the start/end before checking the length
    public static boolean isValidText(String text) {
        return text != null && text.trim().length() > 0;
    }

    // makes sure age is a realistic number, not negative or 0, not over 120
    public static boolean isValidAge(int age) {
        return age > 0 && age <= 120;
    }
}

// Class 4: BPProcessor
// This class contains the "medical logic" of the program.
// It decides what category a reading belongs to, and what advice to give.
class BPProcessor {

    // works out which BP category the reading falls into
    // checked from most urgent to least urgent so nothing gets missed
    public static String evaluateCategory(int systolic, int diastolic) {

        // low blood pressure check comes first, since it needs attention too
        if (systolic < 90 || diastolic < 60) {
            return "Low Blood Pressure";

            // both numbers are in the healthy range
        } else if (systolic < 120 && diastolic < 80) {
            return "Normal";

            // systolic is a bit high but diastolic is still okay
        } else if (systolic >= 120 && systolic <= 129 && diastolic < 80) {
            return "Elevated";

            // either number is in the stage 1 high blood pressure range
        } else if ((systolic >= 130 && systolic <= 139) || (diastolic >= 80 && diastolic <= 89)) {
            return "High Blood Pressure (Stage 1)";

            // either number is in the stage 2 (more serious) range
        } else if (systolic >= 140 || diastolic >= 90) {
            return "High Blood Pressure (Stage 2)";

            // fallback, should not normally happen if InputValidator already checked the numbers
        } else {
            return "Unknown Reading";
        }
    }

    // returns a short health tip based on the category from evaluateCategory
    public static String getAdvice(String category) {
        if (category.equals("Low Blood Pressure")) {
            return "Drink more water, eat a balanced diet, and consult a doctor if symptoms continue.";
        } else if (category.equals("Normal")) {
            return "Your blood pressure is healthy. Keep up the good habits!";
        } else if (category.equals("Elevated")) {
            return "Slightly high. Watch your diet and exercise more.";
        } else if (category.equals("High Blood Pressure (Stage 1)")) {
            return "High. Consider seeing a doctor soon.";
        } else if (category.equals("High Blood Pressure (Stage 2)")) {
            return "Very high! Please go to the hospital as soon as possible.";
        } else {
            return "Unable to give advice for this reading.";
        }
    }
}

// Class 5: MenuSystem
// This is the "control center" of the program.
// It shows the menu, reads the user's choice, and calls the right method.
class MenuSystem {

    // main loop: keeps showing the menu until the user chooses to exit
    public void run(Scanner scanner, DataStore dataStore) {
        System.out.println("=== Blood Pressure Monitoring System ===");

        boolean running = true; // controls the while loop below

        while (running) {
            // print the menu options every time we loop back
            System.out.println("\n--- MENU ---");
            System.out.println("1. Add New Reading");
            System.out.println("2. View History Log");
            System.out.println("3. Exit");
            System.out.print("Choose an option (1-3): ");

            int choice = scanner.nextInt();
            scanner.nextLine(); // clears the leftover newline so nextLine() works properly next time

            // switch picks which block of code to run based on the number typed
            switch (choice) {
                case 1:
                    addReading(scanner, dataStore);
                    break;

                case 2:
                    viewHistory(dataStore);
                    break;

                case 3:
                    running = false; // this makes the while loop stop on the next check
                    System.out.println("Exiting application.");
                    break;

                default:
                    // runs if the user typed something that isn't 1, 2, or 3
                    System.out.println("Invalid choice. Please enter 1, 2, or 3.");
            }
        }
    }

    // handles everything involved in adding one new reading
    private void addReading(Scanner scanner, DataStore dataStore) {

        // ---- get patient name and ID first ----
        System.out.println("Enter patient name");
        String patientName = scanner.nextLine();
        System.out.println("Enter patient ID");
        String patientID = scanner.nextLine();

        // stop here if either field was left blank
        if (!InputValidator.isValidText(patientName) || !InputValidator.isValidText(patientID)) {
            System.out.println("Error: Name and ID cannot be empty.");
            return; // exits this method early, goes back to the menu
        }

        // ---- get age ----
        System.out.println("Enter patient age");
        int patientAge = scanner.nextInt();

        // stop here if age is not realistic
        if (!InputValidator.isValidAge(patientAge)) {
            System.out.println("Error: Invalid age entered.");
            return;
        }

        // ---- get the blood pressure numbers ----
        System.out.print("Enter Systolic (Most accept number, e.g., 120): ");
        int systolic = scanner.nextInt();

        System.out.print("Enter Diastolic (least number to put, e.g., 80): ");
        int diastolic = scanner.nextInt();

        // only save the reading if the numbers are within a realistic range
        if (InputValidator.isValidReading(systolic, diastolic)) {

            // work out the category (Normal, Elevated, etc.) from the numbers
            String category = BPProcessor.evaluateCategory(systolic, diastolic);

            // build a Patient and a BPReading just to carry the values neatly,
            // then DataStore will unpack them into its parallel arrays
            Patient patient = new Patient(patientName, patientID, patientAge);
            BPReading reading = new BPReading(patient, systolic, diastolic, category);

            dataStore.addRecord(reading);

            System.out.println("Reading added! Category: " + category);
            System.out.println("Advice: " + BPProcessor.getAdvice(category));
        } else {
            System.out.println("Error: Invalid blood pressure values entered.");
        }
    }

    // prints out every saved reading, one line per record
    private void viewHistory(DataStore dataStore) {
        System.out.println("\n--- Saved Blood Pressure History ---");

        if (dataStore.recordCount == 0) {
            // nothing has been saved yet
            System.out.println("No records found yet.");
        } else {
            // loop through every used index in the arrays (0 up to recordCount - 1)
            for (int i = 0; i < dataStore.recordCount; i++) {
                System.out.println(OutputFormatter.formatRecord(dataStore, i));
            }
        }
        System.out.println("----------------------------------------");
    }
}

// Class 6: DataStore
// Keeps every saved reading using PARALLEL ARRAYS: six separate arrays,
// all lined up by the same index. Record number 3, for example, is
// nameHistory[3], idHistory[3], ageHistory[3], and so on — all one patient.
// MAX size is fixed at 100, which is plenty for a class demo.
class DataStore {

    // parallel arrays to track history log, up to 100 records max
    String[] nameHistory = new String[100];
    String[] idHistory = new String[100];
    int[] ageHistory = new int[100];
    int[] systolicHistory = new int[100];
    int[] diastolicHistory = new int[100];
    String[] categoryHistory = new String[100];

    int recordCount = 0; // keeps track of array index position

    // takes a finished BPReading object and unpacks its values into
    // the parallel arrays above, one field per array, at the same index
    public void addRecord(BPReading reading) {
        nameHistory[recordCount] = reading.patient.name;
        idHistory[recordCount] = reading.patient.id;
        ageHistory[recordCount] = reading.patient.age;
        systolicHistory[recordCount] = reading.systolic;
        diastolicHistory[recordCount] = reading.diastolic;
        categoryHistory[recordCount] = reading.category;

        recordCount++; // shift index for the next entry
    }
}

// Class 7: OutputFormatter
// Only job: turn the arrays inside DataStore into one nicely formatted line
// of text for a single record at a given index.
class OutputFormatter {

    // reads directly from the DataStore's parallel arrays at the given index
    public static String formatRecord(DataStore dataStore, int index) {
        int recordNumber = index + 1;
        return "Record #" + recordNumber + " | patient:" + dataStore.nameHistory[index] +
                " | ID: " + dataStore.idHistory[index] + " | Age: " + dataStore.ageHistory[index] +
                " -> BP: " + dataStore.systolicHistory[index] + "/" + dataStore.diastolicHistory[index] +
                " mmHg | Category: " + dataStore.categoryHistory[index];
    }
}