import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import generator.Evaluator;
import generator.Generator;
import generator.Problem;
import generator.Solution;
import solvers.Solver;
import solvers.annealing.Annealing;
import solvers.genetic.Genetic;
import solvers.lp.ILP;

public class Main {

    public static void main(String[] args) {
        // generate problem
        System.err.println("Generating problem...");
        Generator generator = new Generator(new int[] { 100, 500 }, // students
                new int[] { 10, 20 }, // courses
                new int[] { 20, 21 }, // days
                new int[] { 4, 5 }, // hoursPerDay
                new int[] { 5, 6 }, // classrooms
                new int[] { 5, 8 }, // rangeStudentsCourseCount
                new int[] { 7, 15 } // rangeCoursesLecturesCount
        );
        Problem problem = generator.generate();
        saveProblem(problem, "Problem.txt");
        System.err.println("Problem generated and saved");

        // generate solvers
        List<Solver> solvers = new LinkedList<>();
        solvers.add(new Annealing(10000000, .01, problem));
        solvers.add(new Genetic(problem, 100, 0.2, 500, 60000));
        solvers.add(new ILP(problem));

        // solve problem and evaluate performance
        System.err.println("Solving and evaluating solutions...");
        solvers.forEach(s -> {
            // solve
            long start = System.currentTimeMillis();
            Solution solution = s.solve();
            long end = System.currentTimeMillis();
            saveSolutionHuman(solution, problem, s.getClass().getSimpleName() + ".csv");
            saveSolution(solution, s.getClass().getSimpleName() + ".txt");

            // evaluate
            System.err.println();
            System.err.println("Solver: " + s.getClass().getSimpleName());
            System.err.println("Time: " + ((end - start) / 1000) + " seconds");
            System.err.println("Score: " + Evaluator.evaluate(problem, solution));
            System.err.println("Total desired lectures: " + Evaluator.countDesiredLectures(problem));
            System.err.println("Total scheduled lectures: " + Evaluator.countScheduledLectures(problem, solution));
            System.err.println("Total enrolled lectures: " + Evaluator.countEnrolledLectures(problem));
            System.err.println("Total taken lectures: " + Evaluator.countTakenLectures(problem, solution));
        });
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
