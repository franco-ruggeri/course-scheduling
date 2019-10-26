import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import generator.Evaluator;
import generator.Generator;
import generator.Problem;
import generator.Solution;
import solvers.Solver;
import solvers.annealing.Annealing;
import solvers.genetic.Genetic;
//import solvers.lp.ILP;

/**
 * Main class to run test cases and compare algorithms
 */
public class Main {
	private static final int TEST_CASES = 5;	// test cases for each problem set
	
	// each generator represents a problem set
	private static final Generator[] generators = new Generator[] {
		new Generator(
			new int[] { 10, 15 }, 	// students
	        new int[] { 7, 10 }, 	// courses
	        new int[] { 1, 2 }, 	// days
	        new int[] { 3, 15 }, 	// hoursPerDay
	        new int[] { 3, 5 }, 	// classrooms
	        new int[] { 3, 5 }, 	// rangeStudentsCourseCount
	        new int[] { 3, 5 } 		// rangeCoursesLecturesCount
		),
		new Generator(
			new int[] { 20, 30 }, 	// students
	        new int[] { 7, 10 }, 	// courses
	        new int[] { 1, 2 }, 	// days
	        new int[] { 5, 15 }, 	// hoursPerDay
	        new int[] { 3, 5 }, 	// classrooms
	        new int[] { 3, 5 }, 	// rangeStudentsCourseCount
	        new int[] { 3, 5 } 		// rangeCoursesLecturesCount
		),
		new Generator(
			new int[] { 10, 15 }, 	// students
	        new int[] { 7, 10 }, 	// courses
	        new int[] { 1, 2 }, 	// days
	        new int[] { 100, 200 }, // hoursPerDay
	        new int[] { 3, 5 }, 	// classrooms
	        new int[] { 3, 5 }, 	// rangeStudentsCourseCount
	        new int[] { 3, 5 } 		// rangeCoursesLecturesCount
		),
		new Generator(
			new int[] { 20, 30 }, 	// students
	        new int[] { 7, 10 }, 	// courses
	        new int[] { 1, 2 }, 	// days
	        new int[] { 20, 30 }, 	// hoursPerDay
	        new int[] { 5, 7 }, 	// classrooms
	        new int[] { 3, 7 }, 	// rangeStudentsCourseCount
	        new int[] { 5, 10 } 	// rangeCoursesLecturesCount
		)
	};
	
	private static class Performance {
		Solver solver;
		long time;
		long score;
		int percentageInfeasibleLectures;
		int percentageScheduledLectures;
		int percentageOverlaps;
		int adequateNumberOfLectures;
	}
	
    public static void main(String[] args) {
    	
    	for (int i=0; i<generators.length; i++) {
    		Map<String, Performance> performance = new HashMap<>();
    		performance.put("Simulated Annealing", new Performance());
    		performance.put("Genetic Algorithm", new Performance());
    		performance.put("ILP", new Performance());
    		
    		for (int j=0; j<TEST_CASES; j++) {
    			// generate problem
            	System.out.println("Generating problem...");
                Problem problem = generators[i].generate();
                saveProblem(problem, "problem_" + i + "_" + j + ".txt");
                System.out.println("Problem generated and saved");
                Evaluator evaluator = new Evaluator(problem);
                
                // create solvers
                performance.get("Simulated Annealing").solver = new Annealing(10000000, .01, problem, evaluator);
                performance.get("Genetic Algorithm").solver = new Genetic(problem, evaluator, 100, 0.05, Integer.MAX_VALUE, 60000);;
//                performance.get("Genetic Algorithm").solver = new ILP(problem);
                
                // solve and fill performance
                System.out.println("Solving...");
                for (Performance p : performance.values()) {
                	// solve
                    long start = System.currentTimeMillis();
                    Solution solution = p.solver.solve();
                    saveSolution(solution, problem, "solution_" + p.solver.getClass().getSimpleName().toLowerCase().replaceAll(" ", "_") + "_" + i + "_" + j + ".csv");
                    long end = System.currentTimeMillis();
                    
                    // update performance
                    p.time += end - start;
                    p.score += evaluator.evaluate(solution);
                    p.percentageInfeasibleLectures += evaluator.percentageInfeasibleLectures(solution);
                    p.percentageScheduledLectures += evaluator.percentageScheduledLectures(solution);
                    p.percentageOverlaps += evaluator.percentageOverlaps(solution);
                    p.adequateNumberOfLectures += evaluator.checkNumberOfLectures(solution) ? 1 : 0;
                }
    		}
    		
    		// print average performance
    		performance.entrySet().stream().forEach(e -> {
    			String solverName = e.getKey();
    			Performance p = e.getValue();
    			
    			System.out.println();
                System.out.println("Solver: " + solverName);
                System.out.println("Time: " + p.time / (TEST_CASES*1000) + " seconds");
                System.out.println("Score: " + p.score / TEST_CASES);
                System.out.println("Percentage infeasible lectures: " + p.percentageInfeasibleLectures / TEST_CASES);
                System.out.println("Percentage scheduled lectures: " + p.percentageScheduledLectures / TEST_CASES);
                System.out.println("Percentage overlaps: " + p.percentageOverlaps / TEST_CASES);
                System.out.println("Adequate number of lectures: " + p.adequateNumberOfLectures + " out of " + TEST_CASES);
    		});
    	}
    }

    private static void saveSolution(final Solution s, final Problem p, final String loc) {
        try {
            PrintWriter writer = new PrintWriter(loc, "UTF-8");
            final int[][] a = s.getSchedule();
            writer.print(intArrayToHuman(a, p.getDays(), p.getHoursPerDay()));
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void saveProblem(final Problem p, final String loc) {
        try {
            PrintWriter writer = new PrintWriter(loc, "UTF-8");
            writer.println("students, courses, timeslots, classrooms");
            writer.println(p.getStudentCount() + " " + p.getCourseCount() + " " + p.getTimeslotsCount() + " "
                    + p.getClassroomCount());
            writer.println("courses by student");
            writer.println(intArrayToString(p.getStudents()));
            writer.println("lessons by course");
            writer.println(intArrayToString(p.getLecturesPerCourse()));
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String intArrayToString(final int[][] a) {
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

    private static String intArrayToHuman(final int[][] a, final int days, final int hoursPerDay) {
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

    private static String intArrayToString(final int[] a) {
        final int len = a.length;
        String ans = "";
        for (int i = 0; i < len; i++) {
            ans += a[i] + ",";
        }
        ans = ans.substring(0, ans.length() - 1);
        ans += "\n";
        return ans;
    }

}
