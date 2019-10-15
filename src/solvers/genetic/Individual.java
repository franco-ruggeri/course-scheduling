package solvers.genetic;

import java.util.Random;

import generator.Evaluator;
import generator.Problem;
import generator.Solution;

/**
 * Represents an individual of the population for GA.
 */
class Individual {
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
		this.fitnessValue = Evaluator.evaluate(problem, solution);
		this.problem = problem;
		this.solution = solution;
	}
	
	/**
	 * Constructs an individual with random genes.
	 * 
	 * @param problem
	 */
	public Individual(Problem problem) {
		int genesPerCourse = genesPerCourse(problem);
		int totGenes;
		StringBuffer sb = new StringBuffer();
		
        // we need #Timeslots x #Classrooms x log2(#Courses) genes
        totGenes = problem.getTimeslotsCount() * problem.getClassroomCount() * genesPerCourse;
        for (int g=0; g<totGenes; g++)
        	sb.append(random.nextInt(2) == 0 ? '0' : '1');
        
        this.problem = problem;
        this.genes = sb.toString();
        this.solution = toSolution(problem, genes);
        this.fitnessValue = Evaluator.evaluate(problem, this.solution);
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
		this.genes = x.genes.substring(0, c) + y.genes.substring(c+1, n);
		this.solution = toSolution(this.problem, this.genes);
		this.fitnessValue = Evaluator.evaluate(this.problem, this.solution);
	}
	
	
	/**
	 * Mutates one gene of the individual.
	 */
	public void mutate() {
		int n = genes.length();
		int m = random.nextInt(n);						// index of mutating gene
		char g = genes.charAt(m) == '0' ? '1' : '0';	// mutated gene
		genes = genes.substring(0, m-1) + g + genes.substring(m+1, n);
	}
	
	public String getGenes() {
		return genes;
	}
	
	public int getFitnessValue() {
		return fitnessValue;
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
        int classrooomCount = problem.getClassroomCount();
        int genesPerCourse = genesPerCourse(problem);;
        int[][] schedule = new int[timeslotCount][classrooomCount];
        
        for (int t=0; t<timeslotCount; t++) {
        	for (int cl=0; cl<classrooomCount; cl++) {
        		int course = 0;
        		int baseIndex = t * cl * genesPerCourse;
        		
        		// translate binary string to integer
        		for (int g=genesPerCourse-1; g>=0; g--)
        			course = course*2 + (genes.charAt(baseIndex + g) == '0' ? 0 : 1);
        		
        		schedule[t][cl] = course;
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
	static private String toGenes(Problem problem, Solution solution) {
        int timeslotCount = problem.getTimeslotsCount();
        int classrooomCount = problem.getClassroomCount();
        int genesPerCourse = genesPerCourse(problem);
        int[][] schedule = solution.getSolution();
        StringBuffer sb = new StringBuffer();
        
        // from end to start because of the conversion to binary (add in head)
		for (int t=timeslotCount-1; t>=0; t--) {
			for (int cl=classrooomCount-1; cl>=0; cl--) {
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
	
	static private int genesPerCourse(Problem problem) {
		int courseCount = problem.getCourseCount();
		int genesPerCourse;
		
		// log2(courseCount), truncated if not an integer
        genesPerCourse = (int) (Math.log(courseCount) / Math.log(2));
        // if it had been truncated, we have to add 1
        // e.g. for courseCount=7 we need 3 bits, but log2(7) is truncated to 2
        genesPerCourse = Math.pow(2, genesPerCourse) != courseCount ? genesPerCourse+1 : genesPerCourse;
        
        return genesPerCourse;
	}
	
}
