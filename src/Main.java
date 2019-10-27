import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
import solvers.lp.ILP;

/**
 * Main class to run test cases and compare algorithms
 */
public class Main {
	private static final int TEST_CASES = 5;	// test cases for each problem set
	private static final String OUTPUT_DIR = "output/";
	
	// each generator represents a problem set
	private static final Generator[] GENERATORS = new Generator[] {
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
		double percentageInfeasibleLectures;
		double percentageScheduledLectures;
		double percentageOverlaps;
		double percentageCoursesWithRightNumberOfLectures;
	}
	
    public static void main(String[] args) {
    	// prepare output folder
    	File directory = new File(OUTPUT_DIR);
    	if (directory.exists()) {
    		// clean
    		String[] entries = directory.list();
    		for (String file : entries)
    			new File(directory.getPath(), file).delete();
    	} else {
    		// create
    		directory.mkdir();
    	}
    	
    	for (int i=0; i<GENERATORS.length; i++) {
    		Map<String, Performance> performance = new HashMap<>();
    		performance.put("Simulated Annealing", new Performance());
    		performance.put("Genetic Algorithm", new Performance());
    		performance.put("ILP", new Performance());
    		
    		for (int j=0; j<TEST_CASES; j++) {
    			// generate problem
            	System.out.println("Generating problem...");
                Problem problem = GENERATORS[i].generate();
                saveProblem(problem, "output/problem_" + i + "_" + j + ".txt");
                System.out.println("Problem generated and saved");
                Evaluator evaluator = new Evaluator(problem);
                
                // create solvers
                performance.get("Simulated Annealing").solver = new Annealing(10000000, .01, problem, evaluator);
                performance.get("Genetic Algorithm").solver = new Genetic(problem, evaluator, 100, 0.05, Integer.MAX_VALUE, 10000);;
//                performance.get("ILP").solver = new ILP(problem);
                
                // solve and fill performance
                System.out.println("Solving problem set " + i + " test case " + j + "...");
                for (Map.Entry<String, Performance> e : performance.entrySet()) {
                	String solverName = e.getKey();
                	Performance p = e.getValue();
                	if (p.solver == null)
                		continue;
                	
                	// solve
                    long start = System.currentTimeMillis();
                    Solution solution = p.solver.solve();
                    System.out.println(solverName + " completed");
                    saveSolution(solution, problem, "output/solution_" + p.solver.getClass().getSimpleName().toLowerCase().replaceAll(" ", "_") + "_" + i + "_" + j + ".csv");
                    long end = System.currentTimeMillis();
                    
                    // update performance
                    p.time += end - start;
                    p.score += evaluator.evaluate(solution);
                    p.percentageInfeasibleLectures += evaluator.percentageInfeasibleLectures(solution);
                    p.percentageScheduledLectures += evaluator.percentageScheduledLectures(solution);
                    p.percentageOverlaps += evaluator.percentageOverlaps(solution);
                    p.percentageCoursesWithRightNumberOfLectures += evaluator.percentageCoursesWithRightNumberOfLectures(solution);
                }
    		}
    		
    		// take average
    		performance.values().stream().forEach(p -> {
    			p.time /= (TEST_CASES*1000);	// also converts to seconds
    			p.score /= TEST_CASES;
    			p.percentageInfeasibleLectures /= TEST_CASES;
    			p.percentageOverlaps /= TEST_CASES;
    			p.percentageScheduledLectures /= TEST_CASES;
    			p.percentageCoursesWithRightNumberOfLectures /= TEST_CASES;
    		});
    		
    		// save performance
    		System.out.println("Saving performance...");
    		savePerformance(performance, "output/performance_" + i + ".txt");
    		System.out.println("Performance saved");
    	}
    }
    
    private static void savePerformance(final Map<String, Performance> performance, final String loc) {
    	try (PrintWriter writer = new PrintWriter(new FileWriter(loc, true))) {
    		for (Map.Entry<String, Performance> e : performance.entrySet()) {
    			String solverName = e.getKey();
    			Performance p = e.getValue();
    			if (p.solver == null)
    				continue;

                writer.println("Solver: " + solverName);
                writer.println("Time: " + p.time + " seconds");
                writer.println("Score: " + p.score);
                writer.println(String.format("Percentage infeasible lectures: %.2f", p.percentageInfeasibleLectures));
                writer.println(String.format("Percentage scheduled lectures: %.2f", p.percentageScheduledLectures));
                writer.println(String.format("Percentage overlaps: %.2f", p.percentageOverlaps));
                writer.println(String.format("Percentage courses with right number of lectures: %.2f", p.percentageCoursesWithRightNumberOfLectures));
                writer.println();
    		}
        } catch (IOException e) {
        	e.printStackTrace();
        }
    }

    private static void saveSolution(final Solution s, final Problem p, final String loc) {
        try (PrintWriter writer = new PrintWriter(loc)) {
            final int[][] a = s.getSchedule();
            writer.print(intArrayToHuman(a, p.getDays(), p.getHoursPerDay()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void saveProblem(final Problem p, final String loc) {
    	try (PrintWriter writer = new PrintWriter(loc)) {
            writer.println("students, courses, timeslots, classrooms");
            writer.println(p.getStudentCount() + " " + p.getCourseCount() + " " + p.getTimeslotsCount() + " "
                    + p.getClassroomCount() + "\n");
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
        String ans = "";
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
