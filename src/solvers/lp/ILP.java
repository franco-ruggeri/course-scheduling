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
//        int[][] timeSchedule = new int[timeslotsNum][classroomsNum];
        LPWizard lpw = new LPWizard();

        final Map<List<Integer>, Integer> map = p.getGroupsCount();
        final int timeSlots = p.getTimeslotsCount();
        final int classRoomCount = p.getClassroomCount();
        final int[] pCourses = p.getCourses();
        final int courseCount = p.getCourseCount();
        int sg = 0;

        //StudentGroups:
        for (Map.Entry<List<Integer>, Integer> entry : map.entrySet()) {
            //Objective:
            final int SGCount = entry.getValue();
            final List<Integer> courses = entry.getKey();

            for (int t = 0; t < timeSlots; t++) {
                final String constraint = getC(sg, t);
                lpw.plus(constraint, SGCount);
                lpw.addConstraint("pos"+t+"sg"+sg, 0, "<=").plus(constraint);
                lpw.setInteger(constraint);
                final LPWizardConstraint currentConst = lpw.addConstraint("const#"+constraint, 1, ">=");//constraint for constraint
                currentConst.plus(constraint, -1);
                for (final int course: courses){
                    final String courseName = getT(t, course);
                    currentConst.plus(courseName, 1);
                    lpw.setBoolean(courseName);
                }
            }
            sg++;
        }


        //for every course, for every timeslot
        for (int c = 0; c < courseCount; c++) {
            final LPWizardConstraint courseConst = lpw.addConstraint("cCourse"+c, pCourses[c], "=");
            for (int t = 0; t < timeSlots; t++) {
                courseConst.plus(getT(t, c));
            }
            courseConst.setAllVariablesBoolean();
        }

        for (int t = 0; t < timeSlots; t++) {
            final LPWizardConstraint classRoomConst = lpw.addConstraint("crC"+t, classRoomCount, ">=");
            for (int c = 0; c < courseCount; c++) {
                classRoomConst.plus(getT(t,c));
            }
            classRoomConst.setAllVariablesBoolean();
        }

        lpw.setMinProblem(true);
        //lpw.setAllVariablesInteger();
        LPSolution solution = lpw.solve();
//        System.out.println(lpw.getLP().convertToCPLEX());
//        System.out.println("solution");
//        System.out.println(solution);
//        System.out.println(solution.getBoolean(getT(0, 0)));

        //solution to Solution
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

        //System.out.println(solution.getBoolean("x1"));
        //
        // long value = solution.getInteger(solutionInteger);

        return new Solution(sol);
    }

    private String getT(final int time, final int course){
        return time + "t" + course;
    }
    private String getC(final int i, final int j){
        return i + "c" + j;
    }
}