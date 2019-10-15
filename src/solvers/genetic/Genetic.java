package solvers.genetic;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import generator.Problem;
import generator.Solution;

/**
 * Implementation of a genetic algorithm (GA) following the pseudo-code in
 * chapter 4 of "Artificial Intelligence, a modern approach - Russell, Norvig".
 */
public class Genetic {
    private final int populationSize;
    private List<Individual> population;
    private double mutationProbability;
    private final int enoughFitness;
    private final long maxTime;
    private Random random;
    
	public Genetic(Problem problem, int populationSize, double mutationProbability, int enoughFitness, long maxTime) {
        this.populationSize = populationSize;
        this.mutationProbability = mutationProbability;
        this.enoughFitness = enoughFitness;
        this.maxTime = maxTime;
        this.random = new Random();
        
        // init population with random states (complete representation, all slots filled)
        population = new LinkedList<>();
        for (int i=0; i<populationSize; i++)
			population.add(new Individual(problem));
//        population.stream().map(Individual::getSolution).forEach(System.err::println);
    }

	public Solution simulate() {
		Individual bestIndividual = null;
		int maxFitnessValue;
		double startTime = System.currentTimeMillis();
		
    	do {
    		// evolution
	    	List<Individual> newPopulation = new LinkedList<>();
	    	for (int i=0; i<populationSize; i++) {
	    		Individual child = reproduce(select(), select());
	    		
				/*
				 * Mutation should happen with a certain probability. In order to obtain this,
				 * we extract a random value with uniform distribution (0,1). Recall that for a
				 * random variable X~U(0,1), F(x) = P(X<=x) = x.
				 */
	    		if (random.nextDouble() <= mutationProbability)
	    			child.mutate();
	    		newPopulation.add(child);
	    	}
	    	
	    	// update population
	    	population = new LinkedList<>(newPopulation);
			
			// get best individual
			int aux = population.stream().mapToInt(Individual::getFitnessValue).max().getAsInt();
	    	bestIndividual = population.stream().filter(i -> i.getFitnessValue() == aux).findFirst().get();
	    	maxFitnessValue = aux;	// aux is used because closures require effective final variables
	    	
	    	// terminate when time runs out or when a good-enough individual has been found
		} while (System.currentTimeMillis() - startTime < maxTime && maxFitnessValue < enoughFitness);
    	
	    return bestIndividual.getSolution();
    }

	private Individual select() {
		/*
		 * Fitness proportionate selection (roulette-wheel selection):
		 * 1. sum all fitness values -> sum
		 * 2. generate random value in (0, s) -> rand
		 * 3. go through the population summing the fitness values -> partialSum
		 * 4. stop when partialSum > rand 
		 *  is greater than r
		 */
		int sum = population.stream().mapToInt(Individual::getFitnessValue).sum();
		int rand = random.nextInt(sum+1);
		int partialSum = 0;
		for (Individual i : population) {
			partialSum += i.getFitnessValue();
			if (partialSum > rand)
				return i;
		}
		
		// should not arrive here
		throw new RuntimeException("Wrong selection algorithm");
	}
	
	private Individual reproduce(Individual x, Individual y) {
		return new Individual(x, y);
	}
	
}
