# AI.A3.PR

## General info
Project of Artificial Intelligence at KTH.

* General topic: advanced planning
* Specific topic: planning with limited resources
* Case study: course scheduling in university
* Algorithms: simulated annealing, genetic algorithm, linear programming

Authors:
* Dingli Mao
* Arturo Rivas Rojas
* Franco Ruggeri
* Ryan Yared

## Structure of the code
Packages:
* default package: contains the Main class to run the test cases
* generator: contains general classes useful to generate the problem and evaluate the solutions
* solvers: contains the implementation of the algorithms

## How to run the test cases
On Windows:
* Import the project into an IDE (e.g. Eclipse, IntelliJ)
* Add the external jars in *src/solvers/lp/* to the build path
   + In Eclipse: right click on the project -> build path -> configure build path -> tab libraries -> add jars
* Run the Main class in one of the following configurations:
   + Generate problems and run GA + Simulated Annealing: set flag *ILP=false*
   + Run ILP on a generated problem: set flag *ILP=true* and *ILP_PROBLEM=<filename_problem>* (e.g. *problem_0_0.txt*)

On Ubuntu, you can follow the same steps, but the ILP libraries may give you some problems. In that case, you can anyway run the other algorithms by by setting the flag *ILP=true*.

*Remark*: ILP takes a while to finish and in some test cases does not converge. This is the reason why it is run a part and not together with the other algorithms. You should stop manually after a predefined amount of time.

## Output
Problems, solutions and performance will be saved into a subfolder called *output*.

## Customized run

### Generate a new problem
In order to create a new problem, you need a *Generator*. The Generator accepts a range of parameters and creates a valid problem choosing random numbers within those ranges. This can be done using the method *generate()*.

The suggested way is to modify the code in the *Main* class.

### Run a solver
All the algorithms implement the *Solver* interface, that declares just one method: *solve()*. To run a new Solver, just create a new object among the available algorithms and use that method. The algorithms have a set of parameters that can be passed to the constructor.

 * Simulated Annealing:
   + Temperature
   + Cooling rate
  
 * Genetic Algorithm:
   + Population size
   + Mutation probability
   + Fitness value that stops the algorithm
   + Maximum time in ms
 
 * Linear Programming:
   + No parameters, the Problem object is enough.
   
