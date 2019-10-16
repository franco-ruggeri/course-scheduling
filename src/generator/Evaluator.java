package generator;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Evaluator {
    final static Heuristics CHOSEN = Heuristics.MAXLECTURES;

    enum Heuristics {
        OVERLAPPING, LESSTIMESLOTS, MAXLECTURES
    }
    // OVERLAPPING: sum of lectures that students can attend.
    // LESSTIMESLOTS: exampele of potential heurisic modifier
    // MAXLECTURES: total of lectures been taken
    // maximizing

    // returns what aiming to minimize; e.g. Overlapping would return one (0x1 <<
    // 0), Lesstimeslots would return (0x1 << 1)
    static int getGoals() {
        return 0;
    }

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

    static int maxLectures(final Problem p, final Solution s) {
        int sum = 0;
        // schedule found
        int[][] schedule = s.getSolution();
        // the different sets of courses the students take
        Set<List<Integer>> groups = p.getGroups();
        // System.err.println(groups);
        // the number of students that take each set of courses
        Map<List<Integer>, Integer> groupsCount = p.getGroupsCount();
        int[] coursesCount = p.getCourses();
        // System.err.println(groupsCount);
        // System.err.println(groupsCount);
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
        // return sum;
        int total = 0;
        for (List<Integer> group : groups) {
            for (int courseCode : group) {
                total += groupsCount.get(group) * coursesCount[courseCode - 1];
            }
        }
        double optimal = Math.pow(((double) sum / (double) total), 2);
        return (int) ((optimal > .1) ? sum * optimal : sum);
    }

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

    public static boolean isValid(final Problem p, final Solution s) {
        int timeslots = p.getTimeslotsCount();
        int cl = p.getClassroomCount();
        int[][] schedule = s.getSolution();
        int courseCount = p.getCourseCount();
        int[] lectures = new int[courseCount + 1];
        int[] courses = p.getCourses();
        for (int i = 0; i < timeslots; i++) {
            for (int course : courses) {
                int ocurrance = 0;
                for (int j = 0; j < cl; j++) {
                    if (course == schedule[i][j]) {
                        ocurrance++;
                    }
                    lectures[schedule[i][j]]++;
                }
                if (ocurrance > 1)
                    return false;
            }
        }
        for (int c = 1; c < courseCount; c++) {
            if (courses[c - 1] != lectures[c])
                return false;
        }
        return true;
    }

    public static int lecturesTaken(final Problem p, final Solution s) {
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

    public static void main(String[] args) {
        int[][] students = new int[][] { { 1, 2, 3 }, { 2, 3, 1 }, { 4, 5, 6 }, { 1, 2, 3, 4, 5 } };
        Map<List<Integer>, Integer> groupsCount = new HashMap<List<Integer>, Integer>();
        Set<List<Integer>> groups = new HashSet<List<Integer>>();

        for (int[] group : students) {
            List<Integer> key = Arrays.stream(group).boxed().collect(Collectors.toList());
            groups.add(key);
            groupsCount.put(key, groupsCount.getOrDefault(key, 0) + 1);
        }
        for (List<Integer> key : groups) {
            System.out.println(key + ": " + groupsCount.get(key));
        }
    }
}
