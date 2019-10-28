import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

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
	/*
	 * You need to touch only this the following two constants.
	 * 
	 * ILP: true to run ILP on a already-generated problem, false to generate
	 * problems and run GA and SA.
	 * 
	 * ILP_PROBLEM: if ILP=true, it indicates the name of the file containing the
	 * problem. The files are output in the following format: "problem_i_j.txt",
	 * where i is the problem set and j the test case.
	 */
	private static final boolean ILP = false;
	private static final String ILP_PROBLEM = "problem_0_1.txt";
	
	private static final String OUTPUT_DIR = "output/";
	private static final int TEST_CASES = 5;	// number of test cases for each problem set, generated when ILP=false
	
	// each generator represents a problem set
	private static final Generator[] GENERATORS = new Generator[] {
		new Generator(
			new int[] { 10, 15 }, 	// students
	        new int[] { 7, 10 }, 	// courses
	        new int[] { 1, 6 }, 	// days
	        new int[] { 3, 5 }, 	// timeslotsPerDay
	        new int[] { 3, 5 }, 	// classrooms
	        new int[] { 3, 5 }, 	// rangeStudentsCourseCount
	        new int[] { 3, 5 } 		// rangeCoursesLecturesCount
		),
		new Generator(
			new int[] { 20, 30 }, 	// students
	        new int[] { 7, 10 }, 	// courses
	        new int[] { 1, 5 }, 	// days
	        new int[] { 5, 6 }, 	// timeslotsPerDay
	        new int[] { 3, 5 }, 	// classrooms
	        new int[] { 3, 5 }, 	// rangeStudentsCourseCount
	        new int[] { 3, 5 } 		// rangeCoursesLecturesCount
		),
		new Generator(
			new int[] { 10, 15 }, 	// students
	        new int[] { 7, 10 }, 	// courses
	        new int[] { 20, 50 }, 	// days
	        new int[] { 5, 8 }, 	// timeslotsPerDay
	        new int[] { 3, 5 }, 	// classrooms
	        new int[] { 3, 5 }, 	// rangeStudentsCourseCount
	        new int[] { 3, 5 } 		// rangeCoursesLecturesCount
		),
		new Generator(
			new int[] { 20, 30 }, 	// students
	        new int[] { 7, 10 }, 	// courses
	        new int[] { 5, 12 }, 	// days
	        new int[] { 4, 5 }, 	// timeslotsPerDay
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
	
    public static void main(String[] args) throws IOException {
    	/*
    	 * ILP on a generated test case.
    	 */
    	if (ILP) {
    		// solve
    		Problem problem = readProblem(OUTPUT_DIR + ILP_PROBLEM);
    		Evaluator evaluator = new Evaluator(problem);
    		Performance performance = new Performance();
    		performance.solver = new ILP(problem);
    		String endName = "ilp_" + ILP_PROBLEM.split("_")[1] + "_" + ILP_PROBLEM.split("_")[2].replace(".txt", "");
    		long start = System.currentTimeMillis();
    		Solution solution = performance.solver.solve();
    		long end = System.currentTimeMillis();
			saveSolution(solution, problem, OUTPUT_DIR + "solution_" + endName + ".csv");
    		
    		// get performance
    		performance.time = end - start;
            performance.score = evaluator.evaluate(solution);
            performance.percentageInfeasibleLectures = evaluator.percentageInfeasibleLectures(solution);
            performance.percentageScheduledLectures = evaluator.percentageScheduledLectures(solution);
            performance.percentageOverlaps = evaluator.percentageOverlaps(solution);
            performance.percentageCoursesWithRightNumberOfLectures = evaluator.percentageCoursesWithRightNumberOfLectures(solution);
            
            // save performance
            Map<String, Performance> pMap = new HashMap<>();
            pMap.put("ILP", performance);
            savePerformance(pMap, OUTPUT_DIR + "performance_" + endName + ".txt", false);
            
    		return;
    	}
    	
    	
    	/*
    	 * Generate test cases and run all algorithms but ILP.
    	 */
    	prepareOutputFolder(OUTPUT_DIR);
    	for (int i=0; i<GENERATORS.length; i++) {
    		Map<String, Performance> performance = new HashMap<>();
    		performance.put("Simulated Annealing", new Performance());
    		performance.put("Genetic Algorithm", new Performance());
    		
    		for (int j=0; j<TEST_CASES; j++) {
    			// generate problem
            	System.out.println("Generating problem...");
                Problem problem = GENERATORS[i].generate();
                saveProblem(problem, "output/problem_" + i + "_" + j + ".txt");
                System.out.println("Problem generated and saved");
                Evaluator evaluator = new Evaluator(problem);
                
                // create solvers
                performance.get("Simulated Annealing").solver = new Annealing(10000000, .01, problem, evaluator);
                performance.get("Genetic Algorithm").solver = new Genetic(problem, evaluator, 100, 0.05, Integer.MAX_VALUE, 30000);;
                
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
					saveSolution(solution, problem,
							"output/solution_" + p.solver.getClass().getSimpleName().toLowerCase().replaceAll(" ", "_")
									+ "_" + i + "_" + j + ".csv");
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
    			p.time /= TEST_CASES;
    			p.score /= TEST_CASES;
    			p.percentageInfeasibleLectures /= TEST_CASES;
    			p.percentageOverlaps /= TEST_CASES;
    			p.percentageScheduledLectures /= TEST_CASES;
    			p.percentageCoursesWithRightNumberOfLectures /= TEST_CASES;
    		});
    		
    		// save performance
    		System.out.println("Saving performance...");
    		savePerformance(performance, "output/performance_" + i + ".txt", true);
    		System.out.println("Performance saved");
    	}
    }
    
    private static void savePerformance(final Map<String, Performance> performance, final String loc, final boolean append) {
    	try (PrintWriter writer = new PrintWriter(new FileWriter(loc, append))) {
    		for (Map.Entry<String, Performance> e : performance.entrySet()) {
    			String solverName = e.getKey();
    			Performance p = e.getValue();
    			if (p.solver == null)
    				continue;

                writer.println("Solver: " + solverName);
                writer.println("Time: " + p.time + " milliseconds");
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
            writer.print(intArrayToHuman(a, p.getDayCount(), p.getTimeslotsPerDay()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void saveProblem(final Problem p, final String loc) {
    	try (PrintWriter writer = new PrintWriter(loc)) {
            writer.println("students, courses, days, timeslots per day, classrooms");
            writer.println(p.getStudentCount() + "," + p.getCourseCount() + "," + p.getDayCount() + ","
                    + p.getTimeslotsPerDay() + "," + p.getClassroomCount() + "\n");
            writer.println("courses by student");
            writer.println(intArrayToString(p.getStudents()));
            writer.println("lessons by course");
            writer.println(intArrayToString(p.getLecturesPerCourse()));
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static Problem readProblem(final String loc) throws IOException {
    	try (BufferedReader reader = new BufferedReader(new FileReader(loc))) {
    		String line;
    		Scanner scanner;
    		int studentCount, courseCount, dayCount, timeslotsPerDay, classroomCount, students[][], lecturesPerCourse[];
    		
    		// skip first
    		reader.readLine();
    		
    		// read counts
    		line = reader.readLine();
    		scanner = new Scanner(line);
    		scanner.useDelimiter(",");
    		studentCount = scanner.nextInt();
    		courseCount = scanner.nextInt();
    		dayCount = scanner.nextInt();
    		timeslotsPerDay = scanner.nextInt();
    		classroomCount = scanner.nextInt();
    		scanner.close();
    		
    		// skip 2 lines
    		reader.readLine();
    		reader.readLine();
    		
    		// read students
    		students = new int[studentCount][];
    		for (int i=0; i<studentCount; i++) {
    			// read line
    			line = reader.readLine();
    			scanner = new Scanner(line);
    			scanner.useDelimiter(",");
    			
    			// get courses
    			List<Integer> student = new LinkedList<>();
    			while (scanner.hasNextInt())
    				student.add(scanner.nextInt());
    			scanner.close();
    			
    			// copy into array
    			students[i] = new int[student.size()];
    			int j =0;
    			for (int course : student)
    				students[i][j++] = course;
    		}
    		
    		// skip 2 lines
    		reader.readLine();
    		reader.readLine();
    		
    		// read lectures per course
    		lecturesPerCourse = new int[courseCount];
    		line = reader.readLine();
    		scanner = new Scanner(line);
			scanner.useDelimiter(",");
    		for (int i=0; i<courseCount; i++)
    			lecturesPerCourse[i] = scanner.nextInt();
    		scanner.close();
    		
    		// return problem
    		return new Problem(studentCount, courseCount, dayCount, timeslotsPerDay, classroomCount, students, lecturesPerCourse);
    	}
    }

    private static void prepareOutputFolder(final String log) {
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
        String ans = "Day/Classroom,Timeslot";
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

    static int[] stringToArray(String s) {
        final String[] split = s.split(",");
        final int[] a = new int[split.length];
        for (int i = 0; i < a.length; i++) {
            a[i] = Integer.parseInt(split[i]);
        }
        return a;
    }

}
