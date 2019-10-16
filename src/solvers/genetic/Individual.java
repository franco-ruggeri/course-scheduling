package solvers.genetic;

import java.util.Arrays;
import java.util.Random;

import generator.Evaluator;
import generator.Problem;
import generator.Solution;

/**
 * Represents an individual of the population for GA.
 */
public class Individual {
	private String genes;
	private int fitnessValue;
	private Problem problem;
	private Solution solution;
	private Random random = new Random();
	
	/**
	 * Constructs an individual with genes equivalent to the solution.
	 * 
	 * @param problem
	 * @param solution
	 */
	public Individual(Problem problem, Solution solution) {
		this.genes = toGenes(problem, solution);
		this.fitnessValue = fitnessFunction(problem, solution);
		this.problem = problem;
		this.solution = solution;
	}
	
	/**
	 * Constructs an individual with random (but valid) genes.
	 * 
	 * @param problem
	 */
	public Individual(Problem problem) {
		int timeslotCount = problem.getTimeslotsCount();
        int classroomCount = problem.getClassroomCount();
        int courseCount = problem.getCourseCount();
        int[][] schedule = new int[timeslotCount][classroomCount];
        int[] courseLectureCount = problem.getCourses();
        int[] courseLectureRemaining = Arrays.copyOf(courseLectureCount, courseCount);
        int completedCourseCount = 0;
        boolean complete = false;
        
        for (int t = 0; t < timeslotCount && !complete; t++) {
            for (int cl = 0; cl < classroomCount && !complete; cl++) {
            	boolean found = false;
                int course = 0;
                while (!found) {
                    course = random.nextInt(courseCount) + 1;
                    if (courseLectureRemaining[course-1] > 0)
                    	found = true;
                }
                schedule[t][cl] = course;
                courseLectureRemaining[course-1]--;
                if (courseLectureRemaining[course-1] == 0)
                	completedCourseCount++;
                if (completedCourseCount == courseCount)
                	complete = true;
            }
        }
        
        this.problem = problem;
        this.solution = new Solution(schedule);
        this.genes = toGenes(problem, solution);
		this.fitnessValue = fitnessFunction(problem, solution);
	}
	
	/**
	 * Constructs an individual as child of two individuals.
	 * 
	 * @param x
	 * @param y
	 */
	public Individual(Individual x, Individual y) {
		int n = x.genes.length();
		int c = random.nextInt(n);	// random crossover point
		this.problem = x.problem;
		this.genes = x.genes.substring(0, c) + y.genes.substring(c, n);
		this.solution = toSolution(this.problem, this.genes);
		this.fitnessValue = fitnessFunction(this.problem, this.solution);
	}
	
	/**
	 * Mutates one gene of the individual.
	 */
	public void mutate() {
		int n = genes.length();
		int m = random.nextInt(n);						// index of mutating gene
		char g = genes.charAt(m) == '0' ? '1' : '0';	// mutated gene
		this.genes = genes.substring(0, m) + g + genes.substring(m+1, n);
		solution = toSolution(problem, genes); 
	}
	
	public String getGenes() {
		return genes;
	}
	
	public int getFitnessValue() {
		return fitnessValue;
	}

	public void setFitnessValue(int fitnessValue) {
		this.fitnessValue = fitnessValue;
	}
	
	public Solution getSolution() {
		return solution;
	}
	
	/**
	 * Converts the individual to a solution.
	 * 
	 * @return
	 */
	private static Solution toSolution(Problem problem, String genes) {
		int timeslotCount = problem.getTimeslotsCount();
        int classroomCount = problem.getClassroomCount();
        int courseCount = problem.getCourseCount();
        int genesPerCourse = genesPerCourse(problem);
        int[][] schedule = new int[timeslotCount][classroomCount];
        
        for (int t=0; t<timeslotCount; t++) {
        	for (int cl=0; cl<classroomCount; cl++) {
        		int course = 0;
        		int baseIndex = (t * classroomCount + cl) * genesPerCourse;
        		
        		// translate binary string to integer
        		for (int g=0; g<genesPerCourse; g++)
        			course = course*2 + (genes.charAt(baseIndex + g) == '0' ? 0 : 1);
        		
        		// pay attention, the mutation could have generate an invalid course ID
        		schedule[t][cl] = (course <= courseCount ? course : 0);
        			
        	}
        }
        
        return new Solution(schedule);
	}
	
	/**
	 * Converts a solution to genes represented as string of 0s and 1s.
	 * The string has #Timeslots x #Classrooms x log2(#Courses) genes.
	 * 
	 * @param solution
	 */
	private static String toGenes(Problem problem, Solution solution) {
        int timeslotCount = problem.getTimeslotsCount();
        int classroomCount = problem.getClassroomCount();
        int genesPerCourse = genesPerCourse(problem);
        int[][] schedule = solution.getSolution();
        StringBuffer sb = new StringBuffer();
        
        // from end to start because of the conversion to binary (add in head)
		for (int t=timeslotCount-1; t>=0; t--) {
			for (int cl=classroomCount-1; cl>=0; cl--) {
				int course = schedule[t][cl];
				int nGenes=0;
				
				// translate to binary and add to string
				while (course != 0) {
					sb.insert(0, course % 2 == 0 ? '0' : '1');
					course /= 2;
					nGenes++;
				}
				
				// insert remaining 0s
				while (nGenes < genesPerCourse) {
					sb.insert(0, '0');
					nGenes++;
				}
			}
		}
		
		return sb.toString();
	}
	
	/**
	 * Computes the minimum number of genes necessary to represent a course.
	 * 
	 * @param problem
	 * @return
	 */
	private static int genesPerCourse(Problem problem) {
		int courseCount = problem.getCourseCount();
		int genesPerCourse;
		
		// log2(courseCount+1), truncated if not an integer (+1 because course IDs start from 1)
        genesPerCourse = (int) (Math.log(courseCount+1) / Math.log(2));
        // if it had been truncated, we have to add 1
        // e.g. for courseCount=6 we need 3 bits, but log2(7) is truncated to 2
        genesPerCourse = Math.pow(2, genesPerCourse) != courseCount+1 ? genesPerCourse+1 : genesPerCourse;
        
        return genesPerCourse;
	}
	
	/**
	 * Computes the fitness value of the individual corresponding to the solution.
	 * 
	 * @param problem
	 * @param solution
	 * @return
	 */
	private static int fitnessFunction(Problem problem, Solution solution) {
    	int fitnessValue;
    	int[][] schedule = solution.getSolution();
    	int courseCount = problem.getCourseCount();
    	int[] desiredLectureCount = problem.getCourses();
    	int[] lectureCount = new int[courseCount];
    	
    	// init such that it cannot be negative (necessary for genetic selection
    	fitnessValue = (int) (Math.pow(Arrays.stream(desiredLectureCount).sum(), 2) +
    			Math.pow(problem.getClassroomCount() * problem.getTimeslotsCount(), 2));
    	
    	// count lectures in timeslot and in total
    	for (int t=0; t<schedule.length; t++) {
    		int[] lectureInTimeslotCount = new int[courseCount];
    		for (int cl=0; cl<schedule[t].length; cl++)
    			if (schedule[t][cl] > 0) {	// 0 means no course
    				lectureInTimeslotCount[schedule[t][cl] - 1]++;
    				lectureCount[schedule[t][cl] - 1]++;
    			}
    		
    		// lectures of the same course in the same timeslot -> penalty
    		fitnessValue -= Arrays.stream(lectureCount).filter(c -> c > 1).map(c -> c*c).sum();
    		Arrays.fill(lectureInTimeslotCount, 0);
    	}
    	
    	// number of lectures different from the desired one -> penalty
    	for (int c=0; c<courseCount; c++)
    		fitnessValue -= Math.pow(desiredLectureCount[c] - lectureCount[c], 2);
    	
    	// overlaps -> penalty
    	// TODO
    	
//    	System.err.println(fitnessValue);
    	
    	return fitnessValue;
    }
	
}
