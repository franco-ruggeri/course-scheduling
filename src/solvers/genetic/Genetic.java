package solvers.genetic;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import generator.Evaluator;
import generator.Problem;
import generator.Solution;

/**
 * Implementation of genetic algorithms (GA) following the pseudo-code in
 * chapter 4 of "Artificial Intelligence, a modern approach - Russell, Norvig".
 */
public class Genetic {
    private final Problem problem;
    private final int populationSize;
    private List<Individual> population;
    private double mutationProbability;
    private final int enoughFitness;
    private final long maxTime;
    private Random random;
    
    private class Individual {
    	String genes;
    	int fitnessValue;
    	
    	Individual(Solution state) {
			/*
			 * A state is a matrix of courses (rows and columns are timeslots and
			 * classrooms, respectively). To represent it as sequence of genes, we need
			 * #Timeslots x #Classrooms x log2(#Courses) bits.
			 */
    		int courseCount = problem.getCourseCount();
            int timeslotCount = problem.getTimeslotsCount();
            int classrooomCount = problem.getClassroomCount();
            int[][] schedule = state.getSolution();
            StringBuffer sb = new StringBuffer();
    		for (int t=0; t<timeslotCount; t++)
    			for (int cl=0; cl<classrooomCount; cl++)
    				for (int c=0; c<courseCount; c++)
    					sb.append(Integer.valueOf(schedule[t][cl]).toString());
    		// TODO non e' completo l'encoding, in questo modo ho #timeslots x #classrooms x 32 bit
    		genes = sb.toString();
    	}
    	
    	Individual(String genes) {
    		this.genes = genes;
    		this.fitnessValue = fitness(genes);
		}

    }
    
	public Genetic(Problem problem, int populationSize, double mutationProbability, int enoughFitness, long maxTime) {
        this.problem = problem;
        this.populationSize = populationSize;
        this.mutationProbability = mutationProbability;
        this.enoughFitness = enoughFitness;
        this.maxTime = maxTime;
        this.random = new Random();
        
        // init population with random states
        population = new LinkedList<>();
        int courseCount = problem.getCourseCount();
        int timeslotCount = problem.getTimeslotsCount();
        int classrooomCount = problem.getClassroomCount();
        for (int i=0; i<populationSize; i++) {
			int[][] schedule = new int[timeslotCount][classrooomCount];
			for (int t=0; t<timeslotCount; t++)
				for (int cl=0; cl<classrooomCount; cl++)
					schedule[t][cl] = random.nextInt(courseCount);
			population.add(new Individual(new Solution(schedule)));
        }
    }

	public Solution simulate() {
		Individual bestIndividual = null;
		double startTime = System.currentTimeMillis();
		
    	do {
    		// evolution
	    	List<String> newPopulation = new LinkedList<>();
	    	for (int i=0; i<populationSize; i++) {
	    		String child = reproduce(select(), select());
	    		
				/*
				 * Mutation should happen with a certain probability. In order to obtain this,
				 * we extract a random value with uniform distribution (0,1). Recall that for a
				 * random variable X~U(0,1), F(x) = P(X<=x) = x.
				 */
	    		if (random.nextDouble() <= mutationProbability)
	    			child = mutate(child);
	    		newPopulation.add(child);
	    	}
	    	
	    	// update population
	    	population.clear();
			newPopulation.forEach(i -> population.add(new Individual(i)));
			
			// get best individual
			int maxFitnessValue = population.stream().mapToInt(i -> i.fitnessValue).max().getAsInt();
	    	bestIndividual = population.stream().filter(i -> i.fitnessValue == maxFitnessValue).findFirst().get();
	    	
	    	// terminate when time runs out or when a good-enough individual has been found
    	} while (System.currentTimeMillis() - startTime < maxTime && bestIndividual.fitnessValue < enoughFitness);
    	
	    return genesToState(bestIndividual.genes);
    }

	private String select() {
		/*
		 * Fitness proportionate selection (roulette-wheel selection):
		 * 1. sum all fitness values -> s
		 * 2. generate random value in (0, S) -> r
		 * 3. go through the population summing the fitness values and stop when the sum
		 *  is greater than r
		 */
		int s = population.stream().mapToInt(i -> i.fitnessValue).sum();
		int r = random.nextInt(s);
		return population.stream().filter(i -> i.fitnessValue > r).findFirst().get().genes;
	}
	
	private String reproduce(String x, String y) {
		int n = x.length();
		int c = random.nextInt(n);	// random crossover point
		return x.substring(0, c) + y.substring(c+1, n);
	}
	
	private String mutate(String genes) {
		int n = genes.length();
		int m = random.nextInt(n);						// index of mutating gene
		char g = genes.charAt(m) == '0' ? '1' : '0';	// mutated gene
		return genes.substring(0, m-1) + g + genes.substring(m+1, n);
	}
	
	int fitness(String genes) {
		return Evaluator.evaluate(problem, genesToState(genes));
	}
	
	private Solution genesToState(String genes) {
		// TODO
		return null;
	}
	
}
