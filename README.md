# AI.A3.PR
Group 10.

The source code may be found int the src folder.
## Generator
The code for the Generator may be found in the src/generator folder/package.

### Generator object
In order to create a new Generator, the parameters of the Problem you want to generatre must be set into the the 
constructor and 2d integers to change the ranges of the parameters.
A static method may be found in Generator [Generator.predefined()] where parameters may easily be modified that returns a new Generator.

In order to get a Problem [Problem.java] made from the Generator using the parameters set when first constructing the generator, the <Generator>.generate() function is used. 
The testing was done using the main function that can be found in Generator.java

### Saving and loading
Static saving and loading methods for both Problem and Solution may be found in Main.java

## Solvers
The code for each of the solvers may be found in their respective directories/package.
For each of the solvers, the solver parameters and a Problem must be inputted in the constructor.
use <Solver>.solve() to solve a Problem, a Solution [Solution.java] object will be returned.

 * Simulated Annealing: annealing
 
   Parameters:
   * Temperature
   * Cooling rate
  
 * Genetic Algorithm: genetic
 
   Parameters:
   * Population size
   * Mutation probability
   * Fitness value that stops the algorithm
   * Maximum time in ms
 
 * Linear Programming: lp
 
   No parameters, the Problem object is enough.
   
   In order to run LP, the external jars (from: SCPSolver; http://scpsolver.org/) need to be added to the project. They can be found (online and) in src/solvers/lp/ .
   
   Adding external jars to intellij: https://stackoverflow.com/questions/1051640/correct-way-to-add-external-jars-lib-jar-to-an-intellij-idea-project
   If having the undefined symbol problem on ubuntu: https://github.com/draeger-lab/SBSCL/issues/5 [libglpkjni_x64 file can be found alongside the jars]
   