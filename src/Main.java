import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;

import generator.Evaluator;
import generator.Generator;
import generator.Problem;
import generator.Solution;
import solvers.Solver;
import solvers.annealing.Annealing;
import solvers.genetic.Genetic;
//import solvers.lp.ILP;

public class Main {

    public static void main(String[] args) {
    	// generate problem
    	System.out.println("Generating problem...");
        Generator generator = new Generator(
        		new int[] { 100, 500 }, // students
        		new int[] { 10, 20 }, 	// courses
        		new int[] { 20, 21 }, 	// days
	            new int[] { 4, 5 }, 	// hoursPerDay
	            new int[] { 5, 6 }, 	// classrooms
	            new int[] { 5, 8 }, 	// rangeStudentsCourseCount
	            new int[] { 7, 15 } 	// rangeCoursesLecturesCount
        );
        Problem problem = generator.generate();
        saveProblem(problem, "Problem.txt");
        System.out.println("Problem generated and saved");
        
        // generate solvers
        List<Solver> solvers = new LinkedList<>();
        solvers.add(new Annealing(10000000, .01, problem));
        solvers.add(new Genetic(problem, 100, 0.05, Double.MAX_VALUE, 60000));
//        solvers.add(new ILP(problem));
        
        // solve problem and evaluate performance
        System.out.println("Solving and evaluating solutions...");
        solvers.forEach(s -> {
        	// solve
        	long start = System.currentTimeMillis();
        	Solution solution = s.solve();
        	long end = System.currentTimeMillis();
        	saveSolution(solution, problem, s.getClass().getSimpleName() + ".csv");
        	
        	// evaluate
        	System.out.println();
        	System.out.println("Solver: " + s.getClass().getSimpleName());
        	System.out.println("Time: " + ((end-start)/1000) + " seconds");
        	System.out.println("Total desired lectures: " + Evaluator.countDesiredLectures(problem));
			System.out.println("Total scheduled lectures: " + Evaluator.countScheduledLectures(problem, solution));
			System.out.println("Total unfeasible lectures: " + Evaluator.countUnfeasibleLectures(problem, solution));
	        System.out.println("Total overlaps: " + Evaluator.countOverlaps(problem, solution));
        });
    }
    
    public static Solution readSolution(final String loc) {
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

    public static void saveSolution(final Solution s, final Problem p, final String loc) {
        try {
            PrintWriter writer = new PrintWriter(loc, "UTF-8");
            final int[][] a = s.getSolution();
            writer.print(intArrayToCSV(a, p.getDays(), p.getHoursPerDay()));
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Problem readProblem(final String loc) {
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

    public static void saveProblem(final Problem p, final String loc) {
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

    private static String intArrayToString(final int[][] a) {
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

    private static String intArrayToCSV(final int[][] a, final int days, final int hoursPerDay) {
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

    private static String intArrayToString(final int[] a) {
        final int len = a.length;
        String ans = len + " ";
        for (int i = 0; i < len; i++) {
            ans += a[i] + " ";
        }
        ans += "\n";
        return ans;
    }

    private static int[] stringToArray(String s) {
        final String[] split = s.split(" ");
        final int[] a = new int[Integer.parseInt(split[0])];
        for (int i = 0; i < a.length; i++) {
            a[i] = Integer.parseInt(split[i + 1]);
        }
        return a;
    }

    private static int[][] stringToDoubleArray(String s) {
        final String[] split = s.split(" ");
        final int[][] a = new int[Integer.parseInt(split[0])][Integer.parseInt(split[1])];
        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < a[0].length; j++) {
                a[i][j] = Integer.parseInt(split[j + i * a.length + 2]);
            }
        }
        return a;
    }

}
