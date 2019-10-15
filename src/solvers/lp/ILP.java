package solvers.lp;

import generator.Problem;
import generator.Solution;
import scpsolver.constraints.LinearBiggerThanEqualsConstraint;
import scpsolver.constraints.LinearSmallerThanEqualsConstraint;
import scpsolver.lpsolver.LinearProgramSolver;
import scpsolver.lpsolver.SolverFactory;
import scpsolver.problems.LPSolution;
import scpsolver.problems.LPWizard;
import scpsolver.problems.LinearProgram;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ILP {

    private final Problem problem;
    private int[][] CoursesOfstudents;
    private int[] lecturesNumOfCourses;
    private final int timeslotsNum;
    private final int classroomsNum;
    private final int coursesNum;


    public ILP(Problem problem) {
        this.problem = problem;
        this.CoursesOfstudents = problem.getStudents();
        this.lecturesNumOfCourses = problem.getCourses();
        this.coursesNum = problem.getCourseCount();
        this.timeslotsNum = problem.getTimeslotsCount();
        this.classroomsNum = problem.getClassroomCount();
    }

    public Solution solveIntegerLinearProgram() {
        int[][] timeSchedule = new int[timeslotsNum][classroomsNum];
        LPWizard lpw = new LPWizard();
        for (int indexOfStudent = 0; indexOfStudent < CoursesOfstudents.length; indexOfStudent++) {
           lpw=  initForOneStudent(lpw);
        }


        lpw.plus("x1", 5.0);
        lpw.plus("x2", 10.0);
        lpw.addConstraint("c1", 8, "<=").plus("x1", 3.0).plus("x2", 1.0);
        lpw.addConstraint("c2", 4, "<=").plus("x2", 4.0);
        lpw.addConstraint("c3", 2, ">=").plus("x1", 2.0);
        lpw.setMinProblem(true);
        //lpw.setAllVariablesInteger();
        LPSolution solution = lpw.solve();

        System.out.println(solution);
        //System.out.println(solution.getBoolean("x1"));
        //
        // long value = solution.getInteger(solutionInteger);

        return null;
    }

    public LPWizard initForOneStudent(LPWizard lpw){

        return null;
    }

    public static void main(String args[]) {

        // ILP.solveIntegerLinearProgram();

    }

}
