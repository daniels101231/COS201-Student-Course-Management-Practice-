import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Student Course Management Mini-System
 * COS 201 - Computer Programming I, mid-semester lab assessment.
 *
 * Main shows the menu, reads the user's choices, validates input,
 * and calls the matching method in CourseManager.
 */
public class Main {

    // Name of the file used by "Save" and "Load". It is created in the folder
    // the program is run from (the project folder when run from VS Code).
    private static final String DATA_FILE = "courses.txt";

    private static final Scanner input = new Scanner(System.in);
    private static final CourseManager manager = new CourseManager();

    // Remembers whether courses were added since the last save or load.
    private static boolean unsavedChanges = false;

    public static void main(String[] args) {
        manager.loadDefaultCourses();

        System.out.println("==============================================");
        System.out.println("   STUDENT COURSE MANAGEMENT MINI-SYSTEM");
        System.out.println("==============================================");
        System.out.println(manager.getCourseCount() + " initial courses loaded ("
                + manager.calculateTotalUnits() + " units).");

        boolean running = true;

        try {
            while (running) {
                printMenu();
                int choice = readMenuChoice();

                switch (choice) {
                    case 1 -> addCourse();
                    case 2 -> viewAllCourses();
                    case 3 -> searchCourse();
                    case 4 -> showTotalUnits();
                    case 5 -> saveCourses();
                    case 6 -> loadCourses();
                    case 7 -> running = !confirmExit();
                    default -> System.out.println("Invalid choice. Please enter a number from 1 to 7.");
                }
            }
        } catch (NoSuchElementException e) {
            // Happens if the input stream is closed (e.g. Ctrl+Z / Ctrl+D). Exit politely.
            System.out.println();
            System.out.println("No more input. Exiting the program.");
        }

        System.out.println("Goodbye!");
    }

    // =====================================================================
    // Menu
    // =====================================================================

    private static void printMenu() {
        System.out.println();
        System.out.println("----------------- MAIN MENU ------------------");
        System.out.println("1. Add a course");
        System.out.println("2. View all courses");
        System.out.println("3. Search for a course by course code");
        System.out.println("4. Calculate total course units");
        System.out.println("5. Save courses to a file");
        System.out.println("6. Load courses from a file");
        System.out.println("7. Exit");
        System.out.println("----------------------------------------------");
    }

    /**
     * Reads the menu choice. If the user types something that is not a number,
     * the NumberFormatException is caught and -1 is returned, which the
     * switch statement treats as an invalid choice.
     */
    private static int readMenuChoice() {
        String text = readLine("Enter your choice (1-7): ");
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    // =====================================================================
    // 1. Add a course
    // =====================================================================

    private static void addCourse() {
        System.out.println();
        System.out.println("--- Add a Course --- (press Enter on an empty line to cancel)");

        // Step 1: ask for the course code until it is valid and not a duplicate.
        String code;
        while (true) {
            String raw = readLine("Course code (e.g. COS 201): ");
            if (raw.isEmpty()) {
                System.out.println("Add course cancelled.");
                return;
            }
            code = CourseManager.normalizeCode(raw);

            if (!CourseManager.isValidCode(code)) {
                System.out.println("Invalid format. Use letters, a space, then 3 digits (e.g. COS 201 or MIVA-CYB 207).");
            } else if (manager.hasCourse(code)) {
                System.out.println("Course " + code + " already exists. Duplicate course codes are not allowed.");
            } else {
                break;
            }
        }

        // Step 2: ask for the title until it is valid.
        String title;
        while (true) {
            title = readLine("Course title: ");
            if (title.isEmpty()) {
                System.out.println("Add course cancelled.");
                return;
            }
            if (CourseManager.isValidTitle(title)) {
                break;
            }
            System.out.println("Title must not contain the \"|\" character.");
        }

        // Step 3: ask for the units until a whole number in range is entered.
        int units;
        while (true) {
            String raw = readLine("Course units (" + CourseManager.MIN_UNITS + "-" + CourseManager.MAX_UNITS + "): ");
            if (raw.isEmpty()) {
                System.out.println("Add course cancelled.");
                return;
            }
            try {
                units = Integer.parseInt(raw);
                if (CourseManager.isValidUnits(units)) {
                    break;
                }
                System.out.println("Units must be between " + CourseManager.MIN_UNITS
                        + " and " + CourseManager.MAX_UNITS + ".");
            } catch (NumberFormatException e) {
                System.out.println("\"" + raw + "\" is not a whole number. Please try again.");
            }
        }

        // Step 4: add the course. CourseManager checks everything again for safety.
        try {
            manager.addCourse(code, title, units);
            unsavedChanges = true;
            System.out.println("Course " + code + " added successfully.");
        } catch (IllegalArgumentException e) {
            System.out.println("Could not add course: " + e.getMessage());
        }
    }

    // =====================================================================
    // 2. View all courses
    // =====================================================================

    private static void viewAllCourses() {
        List<Course> courses = manager.getAllCourses();
        System.out.println();

        if (courses.isEmpty()) {
            System.out.println("There are no courses to display.");
            return;
        }

        System.out.println(String.format("%-4s %-14s %-45s %s", "No.", "Code", "Title", "Units"));
        System.out.println("-".repeat(72));
        for (int i = 0; i < courses.size(); i++) {
            System.out.println(String.format("%-4d %s", i + 1, courses.get(i)));
        }
        System.out.println("-".repeat(72));
        System.out.println("Number of courses: " + courses.size());
    }

    // =====================================================================
    // 3. Search for a course by code
    // =====================================================================

    private static void searchCourse() {
        System.out.println();
        String raw = readLine("Enter the course code to search for: ");

        if (raw.isEmpty()) {
            System.out.println("No course code entered. Search cancelled.");
            return;
        }

        Course found = manager.findByCode(raw);
        if (found != null) {
            System.out.println("Course found:");
            System.out.println("  Code : " + found.getCode());
            System.out.println("  Title: " + found.getTitle());
            System.out.println("  Units: " + found.getUnits());
        } else {
            System.out.println("No course found with code " + CourseManager.normalizeCode(raw) + ".");
        }
    }

    // =====================================================================
    // 4. Calculate total course units
    // =====================================================================

    private static void showTotalUnits() {
        System.out.println();
        System.out.println("Total course units: " + manager.calculateTotalUnits()
                + " (across " + manager.getCourseCount() + " courses)");
    }

    // =====================================================================
    // 5. Save courses to a file
    // =====================================================================

    /** Saves the courses. Returns true if saving worked, false if an error occurred. */
    private static boolean saveCourses() {
        System.out.println();
        try {
            int count = manager.saveToFile(DATA_FILE);
            unsavedChanges = false;
            System.out.println(count + " course(s) saved to " + DATA_FILE + ".");
            return true;
        } catch (IOException e) {
            System.out.println("Error: could not save to " + DATA_FILE + ". " + e.getMessage());
            return false;
        }
    }

    // =====================================================================
    // 6. Load courses from a file
    // =====================================================================

    private static void loadCourses() {
        System.out.println();
        List<String> problems = new ArrayList<>();

        try {
            int loaded = manager.loadFromFile(DATA_FILE, problems);

            // Report any lines that were skipped.
            if (!problems.isEmpty()) {
                System.out.println("The following record(s) were skipped:");
                for (String problem : problems) {
                    System.out.println("  - " + problem);
                }
            }

            if (loaded > 0) {
                unsavedChanges = false;
                System.out.println(loaded + " course(s) loaded from " + DATA_FILE
                        + ". The current list has been replaced.");
            } else {
                System.out.println("No valid courses were found in " + DATA_FILE
                        + ". Your current course list was NOT changed.");
            }

        } catch (FileNotFoundException e) {
            System.out.println("File " + DATA_FILE + " was not found or could not be opened. "
                    + "Save your courses first (option 5).");
            System.out.println("Your current course list was NOT changed.");
        } catch (IOException e) {
            System.out.println("Error reading " + DATA_FILE + ": " + e.getMessage());
            System.out.println("Your current course list was NOT changed.");
        }
    }

    // =====================================================================
    // 7. Exit
    // =====================================================================

    /**
     * Returns true if the program should exit.
     * If there are unsaved changes, the user is offered a chance to save first.
     */
    private static boolean confirmExit() {
        if (!unsavedChanges) {
            return true;
        }

        while (true) {
            String answer = readLine("You have unsaved changes. Save before exiting? (y/n): ").toLowerCase();
            if (answer.equals("y") || answer.equals("yes")) {
                if (saveCourses()) {
                    return true;
                }
                System.out.println("Save failed, so the program will stay open.");
                return false;
            } else if (answer.equals("n") || answer.equals("no")) {
                System.out.println("Exiting without saving.");
                return true;
            }
            System.out.println("Please type y or n.");
        }
    }

    // =====================================================================
    // Helper
    // =====================================================================

    /** Shows a prompt and returns the line the user typed, with spaces trimmed. */
    private static String readLine(String prompt) {
        System.out.print(prompt);
        return input.nextLine().trim();
    }
}
