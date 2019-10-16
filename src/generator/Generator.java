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
        return new Generator(new int[] { 10, 15 }, // students
                new int[] { 7, 8 }, // courses
                new int[] { 4, 5 }, // days
                new int[] { 4, 5 }, // hoursPerDay
                new int[] { 5, 6 }, // classrooms
                new int[] { 3, 5 }, // rangeStudentsCourseCount
                new int[] { 7, 15 } // rangeCoursesLecturesCount
        );
    }

    public static void main(String[] args) {
        // final Generator generator = Generator.predefined();

        // final Problem p = generator.generate();
        // final Annealing annealing = new Annealing(100000, .01, p);
        // final Solution solA = annealing.solve();

        // System.out.println("ANN: isvalid: " + Evaluator.isValid(p, solA));
        // System.out.println("ANN " + Evaluator.evaluate(p, solA));

        // final ILP ilp = new ILP(p);
        // final Solution solILP = ilp.solve();

        // System.out.println("ILP: isvalid: " + Evaluator.isValid(p, solILP));
        // System.out.println("ILP " + Evaluator.evaluate(p, solILP));
        // System.out.println(solILP.toString());
        final Problem p = readProblem("problem.txt");
        final Solution s = readSolution("solution.txt");
        System.err.println(p);
        System.err.println("---------------------------------------------------------------------");
        System.err.println(s);
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

    private void generateGroups(Set<List<Integer>> groups, Map<List<Integer>, Integer> groupsCount, int[][] students) {
        for (int[] group : students) {
            List<Integer> key = Arrays.stream(group).boxed().collect(Collectors.toList());
            groups.add(key);
            groupsCount.put(key, groupsCount.getOrDefault(key, 0) + 1);
        }
    }

    //
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

    // courseLectureCount
    private void generateNumOfLecturesPerCourses(final int[] courses) {
        final int len = courses.length;
        for (int i = 0; i < len; i++) {
            courses[i] = getRndNumber(rangeCoursesLecturesCount);
        }
    }

    // read from a file and outputs the problem
    private int getRndNumber(final int[] range) {
        final int diff = range[1] - range[0];
        return range[0] + rnd.nextInt(diff);
    }

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

    static int[] stringToArray(String s) {
        final String[] split = s.split(",");
        final int[] a = new int[split.length];
        for (int i = 0; i < a.length; i++) {
            a[i] = Integer.parseInt(split[i]);
        }
        return a;
    }

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
