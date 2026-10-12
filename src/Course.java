/**
 * Course represents ONE course, for example "COS 201 - Computer Programming I - 3 units".
 *
 * Each Course object stores three pieces of information (fields):
 *   code  - the course code, e.g. "COS 201"
 *   title - the course title, e.g. "Computer Programming I"
 *   units - the number of credit units, e.g. 3
 */
public class Course {

    // Fields are private so other classes must use the methods below (encapsulation).
    private final String code;
    private final String title;
    private final int units;

    /**
     * Constructor: creates a new Course object.
     * The values are expected to be already validated by CourseManager.
     */
    public Course(String code, String title, int units) {
        this.code = code;
        this.title = title;
        this.units = units;
    }

    // ---------- Getters ----------

    public String getCode() {
        return code;
    }

    public String getTitle() {
        return title;
    }

    public int getUnits() {
        return units;
    }

    /**
     * Converts this course into one line of text for saving to a file.
     * The parts are separated by the "|" character, for example:
     *   COS 201|Computer Programming I|3
     */
    public String toFileLine() {
        return code + "|" + title + "|" + units;
    }

    /**
     * Returns a neatly formatted line used when displaying the course on screen.
     * %-14s means "left-align the text in a space 14 characters wide".
     */
    @Override
    public String toString() {
        return String.format("%-14s %-45s %d", code, title, units);
    }
}
