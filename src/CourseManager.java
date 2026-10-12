import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * CourseManager keeps the list of courses and contains all the operations
 * that can be performed on it: add, view, search, total units, save and load.
 *
 * Main.java handles the menu and keyboard input; this class handles the data.
 */
public class CourseManager {

    // Allowed range for course units.
    public static final int MIN_UNITS = 1;
    public static final int MAX_UNITS = 6;

    /*
     * Pattern for a valid course code:
     *   [A-Z]+        one or more capital letters          e.g. COS, MIVA
     *   (-[A-Z]+)?    optionally a hyphen and more letters  e.g. -CYB
     *   " "           exactly one space
     *   [0-9]{3}      exactly three digits                  e.g. 201
     * Accepts "COS 201", "CYB 299" and "MIVA-CYB 207".
     */
    private static final String CODE_PATTERN = "[A-Z]+(-[A-Z]+)? [0-9]{3}";

    // The list that holds all Course objects currently in the system.
    private final List<Course> courses = new ArrayList<>();

    /**
     * Adds the 10 initial courses. Their units add up to 22.
     */
    public void loadDefaultCourses() {
        courses.clear();
        addCourse("CYB 203", "Cybercrime Law and Countermeasures", 2);
        addCourse("COS 201", "Computer Programming I", 3);
        addCourse("COS 203", "Discrete Structures", 2);
        addCourse("ENT 211", "Entrepreneurship and Innovation", 2);
        addCourse("MIVA-CYB 207", "Zero Trust Networking", 2);
        addCourse("SEN 201", "Introduction to Software Engineering", 2);
        addCourse("MTH 201", "Mathematical Methods I", 2);
        addCourse("MTH 205", "Linear Algebra I", 2);
        addCourse("CYB 201", "Introduction to Cybersecurity and Strategy", 2);
        addCourse("CYB 299", "SIWES I", 3);
    }

    // =====================================================================
    // Validation helpers (string processing)
    // =====================================================================

    /**
     * Cleans up a course code typed by the user:
     *   - trim() removes spaces at the start and end
     *   - toUpperCase() makes "cos 201" become "COS 201"
     *   - replaceAll("\\s+", " ") turns several spaces into a single space
     * So "  cos   201 " becomes "COS 201".
     */
    public static String normalizeCode(String rawCode) {
        if (rawCode == null) {
            return "";
        }
        return rawCode.trim().toUpperCase().replaceAll("\\s+", " ");
    }

    /** Returns true if the (already normalized) code matches the expected format. */
    public static boolean isValidCode(String code) {
        return code != null && code.matches(CODE_PATTERN);
    }

    /** A title must not be empty and must not contain "|" (used as the file separator). */
    public static boolean isValidTitle(String title) {
        return title != null && !title.trim().isEmpty() && !title.contains("|");
    }

    /** Units must be a whole number between MIN_UNITS and MAX_UNITS. */
    public static boolean isValidUnits(int units) {
        return units >= MIN_UNITS && units <= MAX_UNITS;
    }

    // =====================================================================
    // Operation 1: Add a course
    // =====================================================================

    /**
     * Validates the details and adds a new course.
     * Throws IllegalArgumentException with a clear message if anything is wrong,
     * so the caller can catch it and show the message to the user.
     */
    public void addCourse(String rawCode, String rawTitle, int units) {
        String code = normalizeCode(rawCode);
        Course course = buildCourse(code, rawTitle, units);

        if (hasCourse(code)) {
            throw new IllegalArgumentException("A course with code " + code + " already exists.");
        }
        courses.add(course);
    }

    /**
     * Checks all three values and creates a Course object.
     * Used both when adding from the keyboard and when loading from a file.
     */
    private static Course buildCourse(String code, String rawTitle, int units) {
        if (!isValidCode(code)) {
            throw new IllegalArgumentException("Invalid course code \"" + code
                    + "\". Use a format like COS 201 or MIVA-CYB 207.");
        }
        if (!isValidTitle(rawTitle)) {
            throw new IllegalArgumentException("Course title must not be empty or contain \"|\".");
        }
        if (!isValidUnits(units)) {
            throw new IllegalArgumentException("Units must be between " + MIN_UNITS
                    + " and " + MAX_UNITS + " (got " + units + ").");
        }
        return new Course(code, rawTitle.trim(), units);
    }

    // =====================================================================
    // Operation 2: View all courses
    // =====================================================================

    /** Returns a copy of the course list so other classes cannot change it directly. */
    public List<Course> getAllCourses() {
        return new ArrayList<>(courses);
    }

    public int getCourseCount() {
        return courses.size();
    }

    // =====================================================================
    // Operation 3: Search for a course by code
    // =====================================================================

    /**
     * Finds a course by its code. The search is case-insensitive because both
     * the typed code and the stored codes are normalized to upper case.
     * Returns the Course if found, or null if there is no match.
     */
    public Course findByCode(String rawCode) {
        String code = normalizeCode(rawCode);
        for (Course course : courses) {
            if (course.getCode().equalsIgnoreCase(code)) {
                return course;
            }
        }
        return null;
    }

    /** Returns true if a course with this code already exists. */
    public boolean hasCourse(String rawCode) {
        return findByCode(rawCode) != null;
    }

    // =====================================================================
    // Operation 4: Calculate total course units (RECURSIVE)
    // =====================================================================

    /** Public method used by the menu. It starts the recursion at index 0. */
    public int calculateTotalUnits() {
        return sumUnitsFrom(0);
    }

    /**
     * Recursively adds up the units of the courses from position 'index' to the end.
     *
     * BASE CASE:      if index has reached the end of the list, there are no
     *                 courses left to add, so the answer is 0. This stops the recursion.
     * RECURSIVE CASE: otherwise, the total is the units of the course at 'index'
     *                 PLUS the total of all courses after it (sumUnitsFrom(index + 1)).
     *
     * Example with units [2, 3, 2]:
     *   sumUnitsFrom(0) = 2 + sumUnitsFrom(1)
     *                   = 2 + (3 + sumUnitsFrom(2))
     *                   = 2 + (3 + (2 + sumUnitsFrom(3)))
     *                   = 2 + (3 + (2 + 0))   <- base case reached
     *                   = 7
     */
    private int sumUnitsFrom(int index) {
        if (index >= courses.size()) {          // base case
            return 0;
        }
        return courses.get(index).getUnits() + sumUnitsFrom(index + 1);   // recursive case
    }

    // =====================================================================
    // Operation 5: Save courses to a file
    // =====================================================================

    /**
     * Writes every course to the file, one course per line, e.g.
     *   COS 201|Computer Programming I|3
     * try-with-resources automatically closes the file, even if an error occurs.
     * Returns the number of courses saved. Throws IOException if writing fails.
     */
    public int saveToFile(String fileName) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
            for (Course course : courses) {
                writer.println(course.toFileLine());
            }
            // PrintWriter does not throw exceptions itself, so we check for errors.
            if (writer.checkError()) {
                throw new IOException("An error occurred while writing to " + fileName);
            }
        }
        return courses.size();
    }

    // =====================================================================
    // Operation 6: Load courses from a file
    // =====================================================================

    /**
     * Reads courses from the file into a TEMPORARY list first.
     *   - Each invalid line is skipped and a message describing it is added to 'problems'.
     *   - Duplicate course codes inside the file are also skipped.
     *   - Blank lines are ignored.
     *
     * The current course list is ONLY replaced if the whole file was read
     * successfully AND at least one valid course was found. If the file cannot
     * be read (an IOException is thrown), the current list is not changed.
     *
     * Returns the number of courses loaded (0 means the current list was kept).
     */
    public int loadFromFile(String fileName, List<String> problems) throws IOException {
        List<Course> tempList = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;

                if (line.trim().isEmpty()) {
                    continue;   // ignore blank lines
                }

                // Split the line into its parts: code | title | units
                String[] parts = line.split("\\|");
                if (parts.length != 3) {
                    problems.add("Line " + lineNumber + ": expected 3 parts separated by \"|\" but found "
                            + parts.length + " -> \"" + line + "\"");
                    continue;
                }

                String code = normalizeCode(parts[0]);
                String title = parts[1];
                int units;

                try {
                    units = Integer.parseInt(parts[2].trim());
                } catch (NumberFormatException e) {
                    problems.add("Line " + lineNumber + ": units \"" + parts[2].trim()
                            + "\" is not a whole number.");
                    continue;
                }

                try {
                    Course course = buildCourse(code, title, units);

                    if (containsCode(tempList, code)) {
                        problems.add("Line " + lineNumber + ": duplicate course code " + code + " skipped.");
                        continue;
                    }
                    tempList.add(course);

                } catch (IllegalArgumentException e) {
                    problems.add("Line " + lineNumber + ": " + e.getMessage());
                }
            }
        }

        // The file was read completely. Replace the list only if we found valid courses.
        if (tempList.isEmpty()) {
            return 0;
        }
        courses.clear();
        courses.addAll(tempList);
        return tempList.size();
    }

    /** Helper: checks whether a list already contains a course with this code. */
    private static boolean containsCode(List<Course> list, String code) {
        for (Course course : list) {
            if (course.getCode().equalsIgnoreCase(code)) {
                return true;
            }
        }
        return false;
    }
}
