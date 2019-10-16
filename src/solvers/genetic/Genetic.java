package solvers.genetic;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import generator.Problem;
import generator.Solution;

/**
 * Implementation of a genetic algorithm (GA)
 */
public class Genetic {
    private final int populationSize;
    private List<Chromosome> population;
    private double mutationProbability;
    private final double enoughFitness;
    private final long maxTime;
    private static final Random random = new Random();
    
	public Genetic(Problem problem, int populationSize, double mutationProbability, double enoughFitness, long maxTime) {
        this.populationSize = populationSize;
        this.mutationProbability = mutationProbability;
        this.enoughFitness = enoughFitness;
        this.maxTime = maxTime;
        
        // init population with random states (complete representation, all slots filled)
        population = new LinkedList<>();
        for (int c=0; c<populationSize; c++)
			population.add(new Chromosome(problem));
        normalizeFitnessValues();
    }

	public Solution simulate() {
		Chromosome bestIndividual = null;
		double maxFitnessValue;
		long startTime = System.currentTimeMillis();
		
    	do {
    		// evolution
	    	List<Chromosome> newPopulation = new LinkedList<>();
	    	for (int i=0; i<populationSize; i++) {
	    		Chromosome offspring = reproduce(select(), select());
	    		
				/*
				 * Mutation should happen with a certain probability. In order to obtain this,
				 * we extract a random value with uniform distribution (0,1). Recall that for a
				 * random variable X~U(0,1), F(x) = P(X<=x) = x.
				 */
	    		if (random.nextDouble() <= mutationProbability)
	    			offspring.mutate();
	    		newPopulation.add(offspring);
	    	}
	    	
	    	// update population
	    	population = newPopulation;
	    	normalizeFitnessValues();
	    	
			// get best individual
			double aux = population.stream().mapToDouble(Chromosome::getFitnessValue).max().getAsDouble();
	    	bestIndividual = population.stream().filter(i -> i.getFitnessValue() == aux).findFirst().get();
	    	maxFitnessValue = aux;	// aux is used because closures require effective final variables
	    	
	    	// terminate when time runs out or when a good-enough individual has been found
//	    	System.err.println("Remaining time: " + (maxTime - System.currentTimeMillis() + startTime));
		} while (System.currentTimeMillis() - startTime < maxTime && maxFitnessValue < enoughFitness);
    	
	    return bestIndividual.getSolution();
    }

	private Chromosome select() {
		/*
		 * Fitness proportionate selection (roulette-wheel selection):
		 * 1. sum all fitness values -> sum
		 * 2. generate random value in (0, sum) -> rand
		 * 3. go through the population summing the fitness values -> partialSum
		 * 4. stop when partialSum > rand is greater than r
		 * 
		 * Because of the normalization, sum is always 1, so step 1 is not necessary.
		 */
		double rand = random.nextDouble();
		double partialSum = 0;
		for (Chromosome c : population) {
			partialSum += c.getFitnessValue();
			if (partialSum > rand)
				return c;
		}
		
		// should not arrive here
		throw new RuntimeException("Wrong selection algorithm");
	}
	
	private Chromosome reproduce(Chromosome x, Chromosome y) {
		return new Chromosome(x, y);
	}
	
	private void normalizeFitnessValues() {
		System.err.print("Fitness values: ");
		population.stream().mapToDouble(Chromosome::getFitnessValue).forEach(fv -> System.err.print(fv + " "));
		System.err.println();
		double sum = population.stream().mapToDouble(Chromosome::getFitnessValue).sum();
		System.err.println("Sum: " + sum);
		population.forEach(c -> c.setFitnessValue(c.getFitnessValue() / sum));
		System.err.println("Sum: " + population.stream().mapToDouble(Chromosome::getFitnessValue).sum());
	}
	
}
