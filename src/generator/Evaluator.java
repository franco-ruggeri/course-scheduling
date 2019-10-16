package generator;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The evaluator class let us validate a solution or give a numerical value by
 * using different heuristic funcitons
 */
public class Evaluator {
    final static Heuristics CHOSEN = Heuristics.MAXLECTURES;

    enum Heuristics {
        OVERLAPPING, LESSTIMESLOTS, MAXLECTURES, FITNESS_FUNCTION
    }
    // OVERLAPPING: sum of lectures that students can attend.
    // LESSTIMESLOTS: example of potential heurisic modifier
    // MAXLECTURES: total of lectures been taken
    // GA: heuristic ad hoc for genetic algorithm
    // maximizing

    // returns what aiming to minimize; e.g. Overlapping would return one (0x1 <<
    // 0), Lesstimeslots would return (0x1 << 1)
    static int getGoals() {
        return 0;
    }

    /**
     * Funtion to get a numerical value or cost
     * 
     * @param p Problem
     * @param s Solution to evaluate
     * @return Heuristic Value
     */
    public static int evaluate(final Problem p, final Solution s) {
        switch (CHOSEN) {
        case OVERLAPPING:
            return minOverlaps(p, s);
        // case LESSTIMESLOTS:
        // return minTimeslots(p,s);
        case MAXLECTURES:
            return maxLectures(p, s);
        default:
            return 0;
        }
    }

    /**
     * Calculates the cost or value of a solution by adding up the number of
     * lectures that each students can take
     * 
     * @param p Problem
     * @param s Solution to evaluate
     * @return sum of each students lectures
     */
    static int maxLectures(final Problem p, final Solution s) {
        int sum = 0;
        // schedule found
        int[][] schedule = s.getSolution();
        // the different sets of courses the students take
        Set<List<Integer>> groups = p.getGroups();
        // the number of students that take each set of courses
        Map<List<Integer>, Integer> groupsCount = p.getGroupsCount();
        int[] coursesCount = p.getCourses();
        // for each timeslot in the schedule
        for (int[] timeslot : schedule) {
            // againts each gruop
            for (List<Integer> group : groups) {
                int overlaps = 0;
                // for every course given in the current timeslot
                for (int course : timeslot) {
                    // if the course is part of the group we increase overlaps
                    if (group.contains(course)) {
                        overlaps++;
                    }
                }
                // if there are no overlaps we add the number of student in the group to the
                // final result
                if (overlaps >= 1) {
                    sum += groupsCount.get(group);
                }
            }
        }
        // to penalize answers far from the optimal solution
        int total = 0;
        // we compute the total of lectures needed to be taken
        for (List<Integer> group : groups) {
            for (int courseCode : group) {
                total += groupsCount.get(group) * coursesCount[courseCode - 1];
            }
        }
        double optimal = Math.pow(((double) sum / (double) total), 2);
        return (int) ((optimal > .1) ? sum * optimal : sum);
    }

    /**
     * Calculates the cost or value based on the overlasp.
     * 
     * It Adds up teh overlaps of each students, or the number of lectures that each
     * student cant take.
     * 
     * @param p Problem
     * @param s Solution to Evaluate
     * @return Number of overlaps
     */
    static int minOverlaps(final Problem p, final Solution s) {
        int sum = 0;
        // schedule found
        int[][] schedule = s.getSolution();
        // the different sets of courses the students take
        Set<List<Integer>> groups = p.getGroups();
        // the number of students that take each set of courses
        Map<List<Integer>, Integer> groupsCount = p.getGroupsCount();
        int[] coursesCount = p.getCourses();
        // for each timeslot in the schedule
        for (int[] timeslot : schedule) {
            // againts each gruop
            for (List<Integer> group : groups) {
                int overlaps = 0;
                // for every course given in the current timeslot
                for (int course : timeslot) {
                    // if the course is part of the group we increase overlaps
                    if (group.contains(course)) {
                        overlaps++;
                    }
                }
                // if there is any overlap
                if (overlaps > 1) {
                    // we add the number of students in the group times the number of overlaps to
                    // the final result
                    sum += (overlaps - 1) * groupsCount.get(group);
                }
            }
        }
        int total = 0;
        for (List<Integer> group : groups) {
            for (int courseCode : group) {
                total += groupsCount.get(group) * coursesCount[courseCode - 1];
            }
        }
        double optimal = Math.pow(((double) sum / (double) total), 2);
        return (int) (sum * optimal);
    }

    /**
     * This Function evaluates if a solution is consistent to the problem that want
     * to be solve
     * 
     * @param p Problem to solve
     * @param s Solution to validate
     * @return Boolean
     */
    public static boolean isValid(final Problem p, final Solution s) {
        int timeslots = p.getTimeslotsCount();
        int cl = p.getClassroomCount();
        int[][] schedule = s.getSolution();

        int courseCount = p.getCourseCount();
        int[] lectures = new int[courseCount];
        int[] courses = p.getCourses();
        // for each time slot
        for (int i = 0; i < timeslots; i++) {
            // for each course
            for (int course = 1; course <= courseCount; course++) {
                boolean ocurrance = false;
                // for every classroom
                for (int j = 0; j < cl; j++) {
                    // if the course is given
                    if (course == schedule[i][j]) {
                        // if the same course is given 
                        // 2 or more time in the same time slot
                        if (ocurrance) {
                            return false;
                        }
                        // if the couser is given in the time slot
                        ocurrance = true;
                        // we count the number of lectures per course
                        lectures[course - 1]++;
                    }
                }
            }
        }
        // we verify that the number of lectures per course is equal
        // to the stablished in the problem
        for (int c = 1; c < courseCount; c++) {
            // if not we return false
            if (courses[c] != lectures[c])
                return false;
        }
        return true;
    }

    /**
     * Total of lectures in the problem
     * 
     * @param p Problem
     * @return
     */
    public static int countDesiredLectures(final Problem p) {
        return Arrays.stream(p.getCourses()).sum();
    }

    /**
     * Total amount of lectures in the given solution
     * 
     * @param p Problem
     * @param s Solution
     * @return
     */
    public static int countScheduledLectures(final Problem p, final Solution s) {
        return (int) Arrays.stream(s.getSolution()).flatMapToInt(a -> Arrays.stream(a)).filter(c -> c > 0).count();
    }

    /**
     * Computes the sum of lectures that each student can take
     * 
     * @param p Problem
     * @param s Solution
     * @return MaxLectures
     */
    public static int countTakenLectures(final Problem p, final Solution s) {
        int sum = 0;
        // schedule found
        int[][] schedule = s.getSolution();
        // the different sets of courses the students take
        Set<List<Integer>> groups = p.getGroups();
        // the number of students that take each set of courses
        Map<List<Integer>, Integer> groupsCount = p.getGroupsCount();
        for (int[] timeslot : schedule) {
            // againts each gruop
            for (List<Integer> group : groups) {
                int overlaps = 0;
                // for every course given in the current timeslot
                for (int course : timeslot) {
                    // if the course is part of the group we increase overlaps
                    if (group.contains(course)) {
                        overlaps++;
                    }
                }
                // if there are no overlaps we add the number of student in the group to the
                // final result
                if (overlaps >= 1) {
                    sum += groupsCount.get(group);
                }
            }
        }
        return sum;
    }

    /**
     * Return the sum of lecutres that the student should be able to take
     * 
     * @param p Problem
     * @return
     */
    public static int countEnrolledLectures(final Problem p) {
        int[] coursesCount = p.getCourses();
        int total = 0;
        Map<List<Integer>, Integer> groups = p.getGroupsCount();
        // for each group
        for (List<Integer> group : p.getGroups()) {
            // for each course that the group takes
            for (int courseCode : group) {
                // we add to the total the students in that gruop times the number of lectures
                // of the course
                total += groups.get(group) * coursesCount[courseCode - 1];
            }
        }
        return total;
    }
}
