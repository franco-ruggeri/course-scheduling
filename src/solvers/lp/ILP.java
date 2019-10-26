package solvers.lp;

import generator.Problem;
import generator.Solution;
import scpsolver.problems.LPSolution;
import scpsolver.problems.LPWizard;
import scpsolver.problems.LPWizardConstraint;
import solvers.Solver;

import java.util.List;
import java.util.Map;

public class ILP implements Solver {
    private final Problem p;

    public ILP(Problem p) {
        this.p = p;
    }

    public Solution solve() {
        LPWizard lpw = new LPWizard();

        final Map<List<Integer>, Integer> map = p.getStudentGroups(); //Get groups
        final int timeSlots = p.getTimeslotsCount();
        final int classRoomCount = p.getClassroomCount();
        final int[] pCourses = p.getLecturesPerCourse(); //get the amount of lectures per course
        final int courseCount = p.getCourseCount();
        int sg = 0; //Student group number

        //StudentGroups:
        for (Map.Entry<List<Integer>, Integer> entry : map.entrySet()) {
            final int SGCount = entry.getValue();
            final List<Integer> courses = entry.getKey();

            for (int t = 0; t < timeSlots; t++) {
                final String constraint = getC(sg, t); // Since the solver uses names, want the names to be the same/accurate.
                lpw.plus(constraint, SGCount);//Objective function |SG| (Cij)
                lpw.addConstraint("pos"+t+"sg"+sg, 0, "<=").plus(constraint);//New constraint, Cij >= 0
                lpw.setInteger(constraint);//Cij is an integer variable
                //Constraints that will increase the obj function the more conflicts there are
                final LPWizardConstraint currentConst = lpw.addConstraint("const#"+constraint, 1, ">=");
                currentConst.plus(constraint, -1);
                for (final int course: courses){
                    final String courseName = getT(t, course);
                    currentConst.plus(courseName, 1);
                    lpw.setBoolean(courseName);
                }
            }
            sg++;
        }

        //Constraint so that the amount of lectures per course is equal to the amount given in the problem set.
        for (int c = 0; c < courseCount; c++) {
            final LPWizardConstraint courseConst = lpw.addConstraint("cCourse"+c, pCourses[c], "=");
            for (int t = 0; t < timeSlots; t++) {
                courseConst.plus(getT(t, c));
            }
            courseConst.setAllVariablesBoolean();
        }

        //Constraint so that there may not be more lectures than there are classrooms for a time slot t.
        for (int t = 0; t < timeSlots; t++) {
            final LPWizardConstraint classRoomConst = lpw.addConstraint("crC"+t, classRoomCount, ">=");
            for (int c = 0; c < courseCount; c++) {
                classRoomConst.plus(getT(t,c));
            }
            classRoomConst.setAllVariablesBoolean();
        }

        //Minimize the objective function
        lpw.setMinProblem(true);
        //lpw.setAllVariablesInteger();
        //Solver
        LPSolution solution = lpw.solve();
        //DEBUGGING tools
//        System.out.println(lpw.getLP().convertToCPLEX());
//        System.out.println("solution");
//        System.out.println(solution);
//        System.out.println(solution.getBoolean(getT(0, 0)));

        //LP solution to Solution object
        int[][] sol = new int[timeSlots][classRoomCount];
        for (int t = 0; t < timeSlots; t++) {
            int currentClassRoom = 0;
            for (int c = 0; c < courseCount; c++) {
                if (solution.getBoolean(getT(t, c))){
                    sol[t][currentClassRoom] = c + 1;
                    currentClassRoom++;
                }
            }
        }

        return new Solution(sol);
    }

    private String getT(final int time, final int course){
        return time + "t" + course;
    }
    private String getC(final int i, final int j){
        return i + "c" + j;
    }
}