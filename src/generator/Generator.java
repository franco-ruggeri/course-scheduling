package generator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import solvers.annealing.Annealing;
import solvers.genetic.Genetic;

public class Generator {
    static Generator predefined(){
        return new Generator(new int[] { 100, 500 }, // students
                new int[] { 10, 11 }, // courses
                new int[] { 20, 21 }, // days
                new int[] { 4, 5 }, // hoursPerDay
                new int[] { 5, 6 }, // classrooms
                new int[] { 9, 11 }, // rangeStudentsCourseCount
                new int[] { 7, 15 } // rangeCoursesLecturesCount
        );
    }

    public static void main(String[] args) {
        final Generator generator = Generator.predefined();

        final Problem problem = generator.generate();

//        System.out.println(problem.getStudentCount());
//        System.out.println(problem.getGroupsCount());
//        System.out.println(problem.getGroups().size());
//        System.out.println(problem.toString());
        final Annealing solver = new Annealing(10000000, .01, problem);
        final Solution solution = solver.simulate();
//
//        final Problem problem = generator.generate();
//        final Genetic solver = new Genetic(problem, 100, 0.01, 10, 10000000);
//        final Solution solution = solver.simulate();
        
        System.err.println(solution);
        System.err.println("saving");
        saveProblem(problem, "problem.txt");
        System.out.println("HELO WORLD");
        saveSolution(solution, problem, "solution.csv");
        System.err.println("finish");
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

    static Solution readSolution(final String loc) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(loc));
            Solution s = new Solution(stringToDoubleArray(reader.readLine()));
            reader.close();
            return s;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    static void saveSolution(final Solution s, final Problem p, final String loc) {
        try {
            PrintWriter writer = new PrintWriter(loc, "UTF-8");
            final int[][] a = s.getSolution();
            writer.print(intArrayToCSV(a, p.getDays(), p.getHoursPerDay()));
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static Problem readProblem(final String loc) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(loc));
            final String[] splitOne = reader.readLine().split(" ");
            final Problem p = new Problem(Integer.parseInt(splitOne[0]), Integer.parseInt(splitOne[1]),
                    Integer.parseInt(splitOne[2]), Integer.parseInt(splitOne[3]), Integer.parseInt(splitOne[4]),
                    stringToDoubleArray(reader.readLine()), stringToArray(reader.readLine()));
            reader.close();
            return p;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    static void saveProblem(final Problem p, final String loc) {
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

    static String intArrayToString(final int[][] a) {
        final int len = a.length;
        String ans = "";
        for (int i = 0; i < len; i++) {
        	int len2 = a[i].length;
            for (int j = 0; j < len2; j++) {
                ans += a[i][j] + " ";
            }
            ans += "\n";
        }
        return ans;
    }

    static String intArrayToCSV(final int[][] a, final int days, final int hoursPerDay) {
        final int len2 = a[0].length;
        String ans = "Day/Classroom,Hour";
        final String[] week = {"Monday","Tuesday", "Wednesday", "Thursday", "Friday"};
        // String ans = len + "\t" + len2 + "\n";
        // for (int i = 0; i < len; i++) {
        //     for (int j = 0; j < len2; j++) {
        //         ans += a[i][j] + ",";
        //     }
        //     ans += "\n";
        // }
        for (int cl = 0; cl < len2; cl++) {
            ans += "," + (cl+1);
        }
        ans += "\n";
        for (int day = 0; day < days; day++) {
            ans += week[day%5];
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
        String ans = len + " ";
        for (int i = 0; i < len; i++) {
            ans += a[i] + " ";
        }
        ans += "\n";
        return ans;
    }

    static int[] stringToArray(String s) {
        final String[] split = s.split(" ");
        final int[] a = new int[Integer.parseInt(split[0])];
        for (int i = 0; i < a.length; i++) {
            a[i] = Integer.parseInt(split[i + 1]);
        }
        return a;
    }

    static int[][] stringToDoubleArray(String s) {
        final String[] split = s.split(" ");
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
