package solvers.genetic;

import java.util.Arrays;
import java.util.Random;

import generator.Problem;
import generator.Solution;

/**
 * Represents a chromosome of the population for GA.
 */
public class Chromosome {
	private String genes;
	private double fitnessValue;
	private Problem problem;
	private Solution solution;
	private static final Random random = new Random();
	
	/**
	 * Constructs an chromosome with random (but valid) genes.
	 * 
	 * @param problem
	 */
	public Chromosome(Problem problem) {
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
        this.genes = toGenes();
        this.genes = repair(this.problem, this.solution);
        this.refresh();
	}
	
	/**
	 * Constructs an chromosome as offspring of two chromosomes.
	 * 
	 * @param x
	 * @param y
	 */
	public Chromosome(Chromosome x, Chromosome y) {
		this.problem = x.problem;
		this.genes = crossover(x, y);
		this.solution = toSolution();
		this.genes = repair(this.problem, this.solution);
		this.refresh();
	}
	
	/**
	 * Mutates one gene of the chromosome.
	 */
	public void mutate() {
		int n = genes.length();
		int m = random.nextInt(n);						// index of mutating gene
		char g = genes.charAt(m) == '0' ? '1' : '0';	// mutated gene
		this.genes = genes.substring(0, m) + g + genes.substring(m+1, n);
		this.solution = toSolution();
		this.genes = repair(this.problem, this.solution);
		this.refresh();
	}
	
	private double fitness() {
    	double fitnessValue;
    	int[][] schedule = solution.getSolution();
    	int courseCount = problem.getCourseCount();
    	int[] desiredLectureCount = problem.getCourses();
    	int[] lectureCount = new int[courseCount];

    	// count lectures
    	Arrays.stream(schedule)
    		.flatMapToInt(t -> Arrays.stream(t))
    		.filter(c -> c > 0)
    		.forEach(c -> lectureCount[c-1]++);
    	
    	// init fitness value so that it is never negative
    	fitnessValue = 100*Arrays.stream(desiredLectureCount).sum();
    	
    	// number of lectures different from the desired one -> penalty
    	for (int c=0; c<courseCount; c++)
    		fitnessValue -= Math.abs(desiredLectureCount[c] - 100*lectureCount[c]);
    	
    	// overlaps -> penalty
    	// TODO
    	
    	return fitnessValue;
    }
	
	private String crossover(Chromosome x, Chromosome y) {
		int n = x.genes.length();
		int c = random.nextInt(n);	// random crossover point
		return x.genes.substring(0, c) + y.genes.substring(c, n);
	}
	
	private String repair(Problem problem, Solution solution) {
		int timeslotCount = problem.getTimeslotsCount();
        int classroomCount = problem.getClassroomCount();
		int[][] schedule = solution.getSolution();
		boolean[] lectureInTimeslot = new boolean[problem.getCourseCount()];
		
		for (int t=0; t<timeslotCount; t++) {
			for (int cl=0; cl<classroomCount; cl++) {
				// cancel if there is already one in this timeslot for this course
				if (schedule[t][cl] > 0) {
					if (lectureInTimeslot[schedule[t][cl] - 1])
						schedule[t][cl] = 0;
					else
						lectureInTimeslot[schedule[t][cl] - 1] = true;
				}
			}
			Arrays.fill(lectureInTimeslot, false);
		}
		
		this.solution = new Solution(schedule);
		return this.toGenes();
	}
	
	private Solution toSolution() {
		int timeslotCount = problem.getTimeslotsCount();
        int classroomCount = problem.getClassroomCount();
        int courseCount = problem.getCourseCount();
        int genesPerCourse = genesPerCourse();
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
	
	private String toGenes() {
        int timeslotCount = problem.getTimeslotsCount();
        int classroomCount = problem.getClassroomCount();
        int genesPerCourse = genesPerCourse();
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
	
	private int genesPerCourse() {
		int courseCount = problem.getCourseCount();
		int genesPerCourse;
		
		// log2(courseCount+1), truncated if not an integer (+1 because course IDs start from 1)
        genesPerCourse = (int) (Math.log(courseCount+1) / Math.log(2));
        // if it had been truncated, we have to add 1
        // e.g. for courseCount=6 we need 3 bits, but log2(7) is truncated to 2
        genesPerCourse = Math.pow(2, genesPerCourse) != courseCount+1 ? genesPerCourse+1 : genesPerCourse;
        
        return genesPerCourse;
	}
	
	private void refresh() {
		this.solution = toSolution();
		this.fitnessValue = fitness();
	}
	
	public String getGenes() {
		return genes;
	}
	
	public double getFitnessValue() {
		return fitnessValue;
	}

	public void setFitnessValue(double fitnessValue) {
		this.fitnessValue = fitnessValue;
	}
	
	public Solution getSolution() {
		return solution;
	}
	
}
