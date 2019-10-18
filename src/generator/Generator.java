package generator;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import solvers.annealing.Annealing;
import solvers.genetic.Genetic;
import solvers.hill.Hill;
import solvers.lp.ILP;

public class Generator {
    public static Generator predefined() {
        return new Generator(new int[] { 20, 30 }, // students
                new int[] { 7, 10 }, // courses
                new int[] { 1, 2 }, // days
                new int[] { 20, 30 }, // hoursPerDay
                new int[] { 5, 7 }, // classrooms
                new int[] { 3, 7 }, // rangeStudentsCourseCount
                new int[] { 5, 10 } // rangeCoursesLecturesCount
        );
    }

    public static void main(String[] args) {
        String ploc = "ps0#";
        final Generator generator = Generator.predefined();
        final int testCases = 1;
        for (int i = 0; i < testCases; i++) {
            final Problem p = generator.generate();
            saveProblem(p, ploc + i + ".txt");
        }

        long totalTimeA = 0;
        int scoreA = 0;
        int invalidA = 0;
        double scheduledA = 0;
        double takenA = 0;
        for (int i = 0; i < testCases; i++) {
            System.out.println("ANN"+i);
            final Problem p = readProblem(ploc + i + ".txt");
            final Annealing annealing = new Annealing(1000000, .01, p);
            final long start = System.currentTimeMillis();
            final Solution s = annealing.solve();
            final long time = System.currentTimeMillis() - start;
            totalTimeA += time;
            scoreA += Evaluator.evaluate(p, s);
            invalidA += Evaluator.isValid(p, s)?0:1;
            scheduledA += Evaluator.countScheduledLectures(p, s)/ (double)Evaluator.countDesiredLectures(p);
//            System.out.println(scheduled);
            takenA += Evaluator.countTakenLectures(p, s)/ (double)Evaluator.countEnrolledLectures(p);
        }
        System.out.println("ANN ------------------------------------------------------------------------------------");
        System.out.println("TOTime: " + totalTimeA + " - AVG: " + totalTimeA/testCases);
        System.out.println("SCORE: " + scoreA + " - AVG: " + scoreA/testCases);
        System.out.println("Invalid ans: " + invalidA + " - AVG: " + invalidA/testCases);
        System.out.println("Scheduled: " + scheduledA + " - AVG: " + scheduledA/testCases);
        System.out.println("Taken: " + takenA + " - AVG: " + takenA/testCases);

        long totalTimeG = 0;
        int scoreG = 0;
        int invalidG = 0;
        double scheduledG = 0;
        double takenG = 0;
        for (int i = 0; i < testCases; i++) {
            System.out.println("GEN"+i);
            final Problem p = readProblem(ploc + i + ".txt");
            final Genetic gen = new Genetic(p, 100, 0.2, 500, 30000);
            final long start = System.currentTimeMillis();
            final Solution s = gen.solve();
            final long time = System.currentTimeMillis() - start;
            totalTimeG += time;
            scoreG += Evaluator.evaluate(p, s);
            invalidG += Evaluator.isValid(p, s)?0:1;
            scheduledG += Evaluator.countScheduledLectures(p, s)/ (double)Evaluator.countDesiredLectures(p);
//            System.out.println(scheduled);
            takenG += Evaluator.countTakenLectures(p, s)/ (double)Evaluator.countEnrolledLectures(p);
        }
        System.out.println("GEN ------------------------------------------------------------------------------------");
        System.out.println("TOTime: " + totalTimeG + " - AVG: " + totalTimeG/testCases);
        System.out.println("SCORE: " + scoreG + " - AVG: " + scoreG/testCases);
        System.out.println("Invalid ans: " + invalidG + " - AVG: " + invalidG/testCases);
        System.out.println("Scheduled: " + scheduledG + " - AVG: " + scheduledG/testCases);
        System.out.println("Taken: " + takenG + " - AVG: " + takenG/testCases);

        long totalTimeI = 0;
        int scoreI = 0;
        int invalidI = 0;
        double scheduledI = 0;
        double takenI = 0;
        for (int i = 0; i < testCases; i++) {
            System.out.println("ILP"+i);
            final Problem p = readProblem(ploc + i + ".txt");
            final ILP ilp = new ILP(p);
            final long start = System.currentTimeMillis();
            final Solution s = ilp.solve();
            final long time = System.currentTimeMillis() - start;
            totalTimeI += time;
            scoreI += Evaluator.evaluate(p, s);
            invalidI += Evaluator.isValid(p, s)?0:1;
            scheduledI += Evaluator.countScheduledLectures(p, s)/ (double)Evaluator.countDesiredLectures(p);
//            System.out.println(scheduled);
            takenI += Evaluator.countTakenLectures(p, s)/ (double)Evaluator.countEnrolledLectures(p);
        }

        System.out.println("ILP ------------------------------------------------------------------------------------");
        System.out.println("TOTime: " + totalTimeI + " - AVG: " + totalTimeI/testCases);
        System.out.println("SCORE: " + scoreI + " - AVG: " + scoreI/testCases);
        System.out.println("Invalid ans: " + invalidI + " - AVG: " + invalidI/testCases);
        System.out.println("Scheduled: " + scheduledI + " - AVG: " + scheduledI/testCases);
        System.out.println("Taken: " + takenI + " - AVG: " + takenI/testCases);

    }
    // 0 means no course
    private final int[] rangeStudents;
    private final int[] rangeCourses;
    private final int[] rangeDays;
    private final int[] rangeHoursPerDay;
    private final int[] rangeClassrooms;
    private final int[] rangeStudentsCourseCount;
    private final int[] rangeCoursesLecturesCount;

    private final Random rnd = new Random();

    public Generator(final int[] rangeStudents, final int[] rangeNumCourses, final int[] rangeDays,
            final int[] rangeHoursPerDay, final int[] rangeClassrooms, final int[] rangeStudentsCourseCount,
            final int[] rangeCoursesLecturesCount) {
        this.rangeStudents = rangeStudents;
        this.rangeCourses = rangeNumCourses;
        this.rangeDays = rangeDays;
        this.rangeHoursPerDay = rangeHoursPerDay;
        this.rangeClassrooms = rangeClassrooms;
        this.rangeStudentsCourseCount = rangeStudentsCourseCount;
        this.rangeCoursesLecturesCount = rangeCoursesLecturesCount;
    }

    //Generates a new problem based on the given parameters in the constructor
    public Problem generate() {
        Problem p = null;
        do {
            p = new Problem(getRndNumber(rangeStudents), getRndNumber(rangeCourses), getRndNumber(rangeDays),
                    getRndNumber(rangeHoursPerDay), getRndNumber(rangeClassrooms));
            generateCoursePerStudents(p.getStudents(), p.getCourseCount());
            generateNumOfLecturesPerCourses(p.getCourses());
            generateGroups(p.getGroups(), p.getGroupsCount(), p.getStudents());
        } while (!isValid(p));
        return p;
    }

    //Generates groups of students (Students are in the same group if they take the same set of courses)
    private void generateGroups(Set<List<Integer>> groups, Map<List<Integer>, Integer> groupsCount, int[][] students) {
        for (int[] group : students) {
            List<Integer> key = Arrays.stream(group).boxed().collect(Collectors.toList());
            groups.add(key);
            groupsCount.put(key, groupsCount.getOrDefault(key, 0) + 1);
        }
    }

    //Generates the students and which courses they should take based on the constructor parameters.
    private void generateCoursePerStudents(int[][] s, final int courseCount) {
        final int studentCount = s.length;
        ArrayList<Integer> courseList = new ArrayList<Integer>();
        for (int courseIndex = 1; courseIndex <= courseCount; courseIndex++) {
            courseList.add(courseIndex);
        }

        for (int i = 0; i < studentCount; i++) {
            final int registeredCoursesCount = getRndNumber(rangeStudentsCourseCount);
            s[i] = new int[registeredCoursesCount];
            Collections.shuffle(courseList);
            for (int j = 0; j < registeredCoursesCount; j++) {
                s[i][j] = courseList.get(j);
            }
            Arrays.sort(s[i]);
        }
    }

    // Generates the amount of lectures that each course should have
    private void generateNumOfLecturesPerCourses(final int[] courses) {
        final int len = courses.length;
        for (int i = 0; i < len; i++) {
            courses[i] = getRndNumber(rangeCoursesLecturesCount);
        }
    }

    // returns a random number given a range.
    private int getRndNumber(final int[] range) {
        final int diff = range[1] - range[0];
        return range[0] + rnd.nextInt(diff);
    }

    //Saves the solution to a file
    static void saveSolution(final Solution s, final String loc) {
        try {
            PrintWriter writer = new PrintWriter(loc, "UTF-8");
            final int[][] a = s.getSolution();
            writer.print(intArrayToString(a));
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Saves the solution to a file that be easier to read by a human (compared to SaveSolution)
    static void saveSolutionHuman(final Solution s, final Problem p, final String loc) {
        try {
            PrintWriter writer = new PrintWriter(loc, "UTF-8");
            final int[][] a = s.getSolution();
            writer.print(intArrayToHuman(a, p.getDays(), p.getHoursPerDay()));
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Saves the problem to a file.
    static void saveProblem(final Problem p, final String loc) {
        try {
            PrintWriter writer = new PrintWriter(loc, "UTF-8");
            writer.println(p.getStudentCount() + "," + p.getCourseCount() + "," + p.getDays() + "," + p.getHoursPerDay()
                    + "," + p.getClassroomCount());
            writer.print(intArrayToString(p.getStudents()));
            writer.print(intArrayToString(p.getCourses()));
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Saves the problem to a file in a format that may be easier to read by a human
    static void saveProblemHuman(final Problem p, final String loc) {
        try {
            PrintWriter writer = new PrintWriter(loc, "UTF-8");
            writer.println("students, courses, timeslots, classrooms");
            writer.println(p.getStudentCount() + " " + p.getCourseCount() + " " + p.getTimeslotsCount() + " "
                    + p.getClassroomCount());
            writer.println("courses by student");
            writer.println(intArrayToString(p.getStudents()));
            writer.println("lessons by course");
            writer.println(intArrayToString(p.getCourses()));
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Reads a problem from a file.
    static Problem readProblem(final String loc) {
        List<String> lines = new ArrayList<>();
        try (Stream<String> stream = Files.lines(Paths.get(loc))) {
            lines = stream.collect(Collectors.toList());
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String[] splitOne = lines.get(0).split(",");
        int studentCount = Integer.parseInt(lines.get(1));
        int[][] students = new int[studentCount][];
        int start = 2;
        for (int i = 0; i < studentCount; i++) {
            students[i] = stringToArray(lines.get(i + start));
        }
        start += studentCount;
        int[] courses = stringToArray(lines.get(start));
        final Problem p = new Problem(Integer.parseInt(splitOne[0]), Integer.parseInt(splitOne[1]),
                Integer.parseInt(splitOne[2]), Integer.parseInt(splitOne[3]), Integer.parseInt(splitOne[4]), students,
                courses);
        return p;
    }

    //Reads a Solution from a file.
    static Solution readSolution(final String loc) {
        List<String> lines = new ArrayList<>();
        try (Stream<String> stream = Files.lines(Paths.get(loc))) {
            lines = stream.collect(Collectors.toList());
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        int timeslots = Integer.parseInt(lines.get(0));
        int[][] schedule = new int[timeslots][];
        for (int i = 0; i < timeslots; i++) {
            schedule[i] = stringToArray(lines.get(i + 1));
        }
        final Solution s = new Solution(schedule);
        return s;
    }

    //Turns an int array into a string. Used in the save methods.
    static String intArrayToString(final int[][] a) {
        final int len = a.length;
        String ans = len + "\n";
        for (int i = 0; i < len; i++) {
            int len2 = a[i].length;
            for (int j = 0; j < len2; j++) {
                ans += a[i][j] + ",";
            }
            ans = ans.substring(0, ans.length() - 1);
            ans += "\n";
        }
        return ans;
    }

    //Turns an Array into a human readable string//organizes it.
    static String intArrayToHuman(final int[][] a, final int days, final int hoursPerDay) {
        final int len2 = a[0].length;
        String ans = "Day/Classroom,Hour";
        final String[] week = { "Monday", "Tuesday", "Wednesday", "Thursday", "Friday" };
        for (int cl = 0; cl < len2; cl++) {
            ans += "," + (cl + 1);
        }
        ans += "\n";
        for (int day = 0; day < days; day++) {
            ans += week[day % 5];
            for (int hpd = 0; hpd < hoursPerDay; hpd++) {
                int i = hpd * (day + 1);
                ans += "," + hpd;
                for (int j = 0; j < len2; j++) {
                    ans += "," + a[i][j];
                }
                ans += "\n";
            }
            ans += "\n";
        }
        return ans;
    }

    //turns an array into a string
    static String intArrayToString(final int[] a) {
        final int len = a.length;
        String ans = "";
        for (int i = 0; i < len; i++) {
            ans += a[i] + ",";
        }
        ans = ans.substring(0, ans.length() - 1);
        ans += "\n";
        return ans;
    }

    //Turns a string into an array
    static int[] stringToArray(String s) {
        final String[] split = s.split(",");
        final int[] a = new int[split.length];
        for (int i = 0; i < a.length; i++) {
            a[i] = Integer.parseInt(split[i]);
        }
        return a;
    }

    //Turns a 2d array into a sting
    static int[][] stringToDoubleArray(String s) {
        final String[] split = s.split(",");
        final int[][] a = new int[Integer.parseInt(split[0])][Integer.parseInt(split[1])];
        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < a[0].length; j++) {
                a[i][j] = Integer.parseInt(split[j + i * a.length + 2]);
            }
        }
        return a;
    }

    //Checks if the given problem is valid.
    static boolean isValid(final Problem p) {
        final int capacity = p.getClassroomCount() * p.getTimeslotsCount();
        int sum = 0;
        final int[] c = p.getCourses();
        for (int i : c) {
            sum += i;
        }

        return capacity >= sum;
    }
}
