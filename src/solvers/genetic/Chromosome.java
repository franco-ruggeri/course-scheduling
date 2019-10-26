package solvers.genetic;

import java.util.Arrays;
import java.util.Random;

import generator.Evaluator;
import generator.Problem;
import generator.Solution;

/**
 * Represents a chromosome of the population for GA. The correspondent solution
 * is a valid schedule, i.e. there is at most 1 lecture per course in the same
 * time slot. However, a right number of lectures for the courses is not
 * guaranteed.
 * 
 * Notice, once again, that here 'valid' means that one course cannot have more
 * than one lecture in the same time slot, i.e. there cannot be infeasible
 * lectures.
 */
public class Chromosome {
	private String genes;
	private double fitnessValue;
	private Problem problem;
	private Solution solution;
	private static final Random random = new Random();
	
	/**
	 * Constructs a chromosome with random but valid genes.
	 * 
	 * @param problem problem to solve
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
        
        // generate random schedule
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
        
        // set attributes
        this.problem = problem;
        this.solution = new Solution(schedule);
        this.genes = toGenes();
        
        // make it valid
        this.repair();
	}
	
	/**
	 * Constructs a chromosome as offspring of two chromosomes. The offspring is
	 * then repaired in order to be valid.
	 * 
	 * @param x parent 1
	 * @param y parent 2
	 */
	public Chromosome(Chromosome x, Chromosome y) {
		this.problem = x.problem;
		this.genes = crossover(x, y);
		this.solution = toSolution();
		this.repair();
	}
	
	/**
	 * Mutates one gene of the chromosome. After the mutation, the chromosome is
	 * repaired in order to be valid.
	 */
	public void mutate() {
		int n = genes.length();
		int m = random.nextInt(n);						// index of mutating gene
		char g = genes.charAt(m) == '0' ? '1' : '0';	// mutated gene
		this.genes = genes.substring(0, m) + g + genes.substring(m+1, n);
		this.solution = toSolution();
		this.repair();
	}
	
	/**
	 * Compute the fitness value of the chromosome.
	 * 
	 * It considers the following factors:
	 * - number of lectures for the courses similar to the desired ones.
	 * - number of overlaps for students.
	 * 
	 * Notice that the fitness value must not be negative for a correct selection in
	 * GA.
	 * 
	 * @return fitness value
	 */
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
    	fitnessValue = 150 * Arrays.stream(desiredLectureCount).sum();
    	
    	// number of lectures different from the desired one -> penalty
    	for (int c=0; c<courseCount; c++)
    		fitnessValue -= 50 * Math.abs(desiredLectureCount[c] - lectureCount[c]);
    	
    	// overlaps -> penalty
    	fitnessValue -= 2 * Evaluator.countOverlaps(problem, solution);
    	
    	return fitnessValue > 0.0 ? fitnessValue : 0.0;
    }
	
	/**
	 * Combine two chromosomes to generate the genes of the offspring.
	 * 
	 * @param x parent 1
	 * @param y parent 2
	 * @return sequence of genes of the offspring
	 */
	private String crossover(Chromosome x, Chromosome y) {
		int n = x.genes.length();
		int genesPerTimeslot = problem.getClassroomCount() * genesPerCourse();
		int crossoverPoint;
		
		/*
		 * Random crossover point with the constraint of separating whole time slots.
		 * This way, the offspring is valid, i.e. it does not have 2 lectures of the
		 * same course in the same time slot.
		 */
		crossoverPoint = random.nextInt(n);
		crossoverPoint = (crossoverPoint / genesPerTimeslot) * genesPerTimeslot;
		
		 // first part from one parent and the second part from the other
		return x.genes.substring(0, crossoverPoint) + y.genes.substring(crossoverPoint, n);
	}
	
	/**
	 * Repairs the chromosome, in order to make it valid. This is achieved by
	 * canceling invalid lectures. A lecture is invalid if there is already another
	 * lecture of the same course in the same time slot.
	 */
	private void repair() {
		int timeslotCount = problem.getTimeslotsCount();
        int classroomCount = problem.getClassroomCount();
        int courseCount = problem.getCourseCount();
		int[][] schedule = solution.getSolution();
		boolean[] lectureInTimeslot = new boolean[problem.getCourseCount()];
		
		// repair
		for (int t=0; t<timeslotCount; t++) {
			for (int cl=0; cl<classroomCount; cl++) {
				// mutation can cause an invalid course ID
				if (schedule[t][cl] > courseCount) {
					schedule[t][cl] = 0;
				} else if (schedule[t][cl] > 0) {
					// cancel lecture if there is already one in this time slot for this course
					if (lectureInTimeslot[schedule[t][cl] - 1])
						schedule[t][cl] = 0;
					else
						lectureInTimeslot[schedule[t][cl] - 1] = true;
				}
			}
			Arrays.fill(lectureInTimeslot, false);
		}
		
		// refresh
		this.solution = new Solution(schedule);
		this.genes = toGenes();
		this.fitnessValue = fitness();
	}

	/**
	 * Converts the solution in a sequence of genes.
	 * 
	 * @return the sequence of genes
	 */
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
	
	/**
	 * Converts the sequence of genes in a solution.
	 * 
	 * @return the solution
	 */
	private Solution toSolution() {
		int timeslotCount = problem.getTimeslotsCount();
        int classroomCount = problem.getClassroomCount();
        int genesPerCourse = genesPerCourse();
        int[][] schedule = new int[timeslotCount][classroomCount];
        
        for (int t=0; t<timeslotCount; t++) {
        	for (int cl=0; cl<classroomCount; cl++) {
        		int course = 0;
        		int baseIndex = (t * classroomCount + cl) * genesPerCourse;
        		
        		// translate binary string to integer
        		for (int g=0; g<genesPerCourse; g++)
        			course = course*2 + (genes.charAt(baseIndex + g) == '0' ? 0 : 1);
        		
        		schedule[t][cl] = course;
        	}
        }
        
        return new Solution(schedule);
	}
	
	/**
	 * Computes the minimum number of genes to represent a course.
	 * 
	 * @return number of genes per course
	 */
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
	
	
	/*
	 * Getters and setters
	 */
	
	public String getGenes() {
		return genes;
	}
	
	public double getFitnessValue() {
		return fitnessValue;
	}
	
	public Solution getSolution() {
		return solution;
	}
	
}
