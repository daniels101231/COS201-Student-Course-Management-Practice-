# Student Course Management Mini-System

**COS 201 – Computer Programming I · Mid-Semester Lab Assessment**

A Java 17 console application for managing a student's registered courses. You can add, view and search courses, calculate the total course units, and save the course list to a text file or load it back.

---

## Project structure

```
COS201-Student-Course-Management/
├── src/
│   ├── Course.java          A class representing one course (code, title, units)
│   ├── CourseManager.java   Holds the course list and all operations on it
│   └── Main.java            The console menu: reads input and calls CourseManager
├── .gitignore               Stops compiled files (bin/, *.class) from being committed
└── README.md                This file
```

Files created when the program runs:

| File / folder | Created by | Purpose |
|---|---|---|
| `bin/` | Compiling | Compiled `.class` files (ignored by Git) |
| `courses.txt` | Menu option 5 (Save) | The saved course list |

---

## Requirements

- Java 17 (JDK). Check with `java -version` and `javac -version`.
- Visual Studio Code with the **Extension Pack for Java** (optional, for the Run button).

---

## How to compile and run

### Option A: Terminal (recommended for the assessment)

Open the project folder in VS Code, open a terminal (**Terminal → New Terminal**), and run:

```
javac --release 17 -d bin src/*.java
java -cp bin Main
```

- `javac` compiles the three source files. `--release 17` makes sure only Java 17 features are used, and `-d bin` puts the `.class` files in the `bin` folder.
- `java -cp bin Main` runs the program. `-cp bin` tells Java where the compiled classes are.

> On Windows, `src\*.java` also works. If your terminal does not expand `*`, list the files instead:
> `javac --release 17 -d bin src/Course.java src/CourseManager.java src/Main.java`

### Option B: VS Code Run button

Open `src/Main.java` and click **Run** above the `main` method. Type your choices in the terminal panel that opens.

Always run the program from the **project folder**. That is where `courses.txt` is saved and loaded.

---

## The seven menu operations

| # | Operation | What it does |
|---|---|---|
| 1 | Add a course | Asks for code, title and units, validating each. Duplicate codes are refused. Press Enter on an empty line to cancel. |
| 2 | View all courses | Shows every course in a numbered table. |
| 3 | Search for a course by course code | Finds a course by its code. The search is **case-insensitive** (`cos 201` finds `COS 201`). |
| 4 | Calculate total course units | Adds up all units using a **recursive** method. |
| 5 | Save courses to a file | Writes all courses to `courses.txt`. |
| 6 | Load courses from a file | Reads courses from `courses.txt` and replaces the current list. |
| 7 | Exit | Ends the program. If there are unsaved changes, it asks whether to save first. |

---

## Initial courses

Every time the program starts, it loads these 10 courses (total **22 units**):

| Code | Title | Units |
|---|---|---|
| CYB 203 | Cybercrime Law and Countermeasures | 2 |
| COS 201 | Computer Programming I | 3 |
| COS 203 | Discrete Structures | 2 |
| ENT 211 | Entrepreneurship and Innovation | 2 |
| MIVA-CYB 207 | Zero Trust Networking | 2 |
| SEN 201 | Introduction to Software Engineering | 2 |
| MTH 201 | Mathematical Methods I | 2 |
| MTH 205 | Linear Algebra I | 2 |
| CYB 201 | Introduction to Cybersecurity and Strategy | 2 |
| CYB 299 | SIWES I | 3 |
| | **Total** | **22** |

---

## Saving and loading: important

The program does **not** save or load automatically.

- **Save before you exit.** Courses you add are kept in memory only. If you exit without saving (option 5), they are lost. The program warns you on exit if you have unsaved changes.
- **Load when you start a new session.** A new session always starts with the 10 initial courses (22 units). To get your saved courses back, choose option 6.
- **Loading replaces the current list.** It does not merge with it. Save first if you want to keep what is on screen.

### File format

`courses.txt` stores one course per line. The parts are separated by `|`, because course titles may contain commas:

```
COS 201|Computer Programming I|3
MIVA-CYB 207|Zero Trust Networking|2
```

### How loading handles bad data

1. Every line is read into a **temporary list** first.
2. Each line is checked. Invalid lines are **skipped** and reported with their line number and the reason, for example:
   - wrong number of parts
   - units that are not a whole number, or not between 1 and 6
   - an invalid course code
   - an empty title
   - a duplicate course code
3. Blank lines are ignored.
4. The current course list is **only replaced** if the file was read successfully and at least one valid course was found. If the file is missing, cannot be read, or has no valid courses, the current list is left unchanged.

---

## Input validation rules

| Input | Rule | Examples |
|---|---|---|
| Course code | Letters, an optional `-LETTERS` part, one space, then exactly 3 digits. It is cleaned up first: spaces trimmed, converted to upper case, repeated spaces collapsed. | ✅ `COS 201`, `MIVA-CYB 207`, `cos  201` ❌ `COS201`, `123`, `COS 20` |
| Duplicate code | A code that already exists is refused. | Adding `cos 201` again is refused. |
| Title | Must not be empty and must not contain `\|`. | |
| Units | A whole number from 1 to 6. | ❌ `abc`, `2.5`, `0`, `7` |
| Menu choice | A number from 1 to 7. Anything else shows an error and the menu again. | ❌ `9`, `hello`, empty |

Invalid input never crashes the program. `NumberFormatException` is caught when text is converted to a number, and the user is asked again.

---

## Recursive feature: total course units

The total units (option 4) is calculated by the recursive method `sumUnitsFrom(int index)` in `CourseManager.java`:

```java
private int sumUnitsFrom(int index) {
    if (index >= courses.size()) {          // base case
        return 0;
    }
    return courses.get(index).getUnits() + sumUnitsFrom(index + 1);   // recursive case
}
```

- **Base case:** when `index` reaches the end of the list, there are no courses left, so the method returns `0`. This stops the recursion.
- **Recursive case:** otherwise, the total equals *this course's units* plus *the total of all the courses after it*. The method calls itself with `index + 1`, moving one step closer to the base case each time.

Example with units `[2, 3, 2]`:

```
sumUnitsFrom(0) = 2 + sumUnitsFrom(1)
                = 2 + (3 + sumUnitsFrom(2))
                = 2 + (3 + (2 + sumUnitsFrom(3)))
                = 2 + (3 + (2 + 0))      <- base case
                = 7
```

For the 10 initial courses, the result is **22**.

---

## How the assessment requirements are met

| Requirement | Where |
|---|---|
| Classes, objects, methods | `Course`, `CourseManager`, `Main`; each course is a `Course` object |
| Loops | `while` menu loop; `while` re-prompt loops; `for` loops in view, search and load |
| Conditional statements | `switch` on the menu choice; `if/else` for validation, duplicates and search results |
| Input validation & string processing | `trim()`, `toUpperCase()`, `replaceAll()`, `matches()` (regex), `split()`, `String.format()` |
| Exception handling | `NumberFormatException`, `IllegalArgumentException`, `FileNotFoundException`, `IOException`, `NoSuchElementException` |
| File reading and writing | `BufferedReader`/`FileReader` and `PrintWriter`/`FileWriter`, both in **try-with-resources** |
| Recursion | `sumUnitsFrom()` for total units |

---

## Test checklist

Run these after compiling. Tick each one off as you confirm it on your own computer.

| # | Steps | Expected result |
|---|---|---|
| 1 | Start the program | "10 initial courses loaded (22 units)." |
| 2 | Option 2 | Table of 10 courses, including `MIVA-CYB 207` |
| 3 | Option 4 | `Total course units: 22 (across 10 courses)` |
| 4 | Option 3, enter `cos 201` | Found: COS 201, Computer Programming I, 3 units |
| 5 | Option 3, enter `miva-cyb 207` | Found: MIVA-CYB 207, Zero Trust Networking |
| 6 | Option 3, enter `XYZ 999` | "No course found with code XYZ 999." |
| 7 | Menu: enter `9`, then `hello`, then nothing | "Invalid choice…" each time; menu shows again |
| 8 | Option 1, code `COS 201` | Refused as a duplicate |
| 9 | Option 1, code `CYB203` | "Invalid format…" |
| 10 | Option 1, `COS 205`, title `Data Structures`, units `abc`, `0`, `2.5`, `2` | First three units rejected; course added |
| 11 | Option 4 | `Total course units: 24 (across 11 courses)` |
| 12 | Option 5 | "11 course(s) saved to courses.txt." and `courses.txt` appears in the project folder |
| 13 | Option 7 then restart; option 4 | Back to 22 (new session starts with the initial courses) |
| 14 | Option 6, then option 4 | "11 course(s) loaded…", total 24 |
| 15 | Add a course, then option 7 | Asks "Save before exiting? (y/n)" |
| 16 | Delete `courses.txt`, then option 6 | "not found" message; list unchanged |
| 17 | Add a line like `BROKEN LINE` and `MTH 201\|Maths\|two` to `courses.txt`, then option 6 | Each bad line reported with its line number and reason; valid lines loaded |
| 18 | Make `courses.txt` contain only invalid lines, then option 6 | "No valid courses were found… NOT changed"; total still the same as before |
