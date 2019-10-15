package solvers.hill;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import generator.Evaluator;
import generator.Problem;
import generator.Solution;

/**
 * Hill
 */
public class Hill {

    private int bestNeighbors;
    private final int courses;
    private final int[] coursesCount;
    private final int timeslots;
    private final int classrooms;
    private final Problem p;

    public Hill(int bestNeighbors, Problem p) {
        this.bestNeighbors = bestNeighbors;
        this.p = p;
        this.courses = p.getCourseCount();
        this.coursesCount = p.getCourses();
        this.timeslots = p.getTimeslotsCount();
        this.classrooms = p.getClassroomCount();
    }

    public Solution solve() {
        int[][] schedule = new int[timeslots][classrooms];
        int[][] newSchedule = new int[timeslots][classrooms];
        int[][] bestSchedule = new int[timeslots][classrooms];
        int cost = 0;
        int newCost = 0;
        int bestCost = 0;
        boolean better = true;
        init(schedule);
        cost = Evaluator.evaluate(p, new Solution(schedule));
        bestCost = cost;
        while (better) {
            better = false;
            for (int k = 0; k < bestNeighbors; k++) {
                for (int i = 0; i < timeslots; i++) {
                    newSchedule[i] = Arrays.copyOf(schedule[i], schedule[i].length);
                }
                swap(newSchedule);
                newCost = Evaluator.evaluate(p, new Solution(newSchedule));
                // System.err.println("Cost = "+cost);
                // System.err.println("New Cost = "+newCost);
                // System.err.println("Best Cost = "+bestCost);
                if (newCost > cost) {
                    for (int i = 0; i < timeslots; i++) {
                        schedule[i] = Arrays.copyOf(newSchedule[i], newSchedule[i].length);
                    }
                    if (newCost > bestCost) {
                        for (int i = 0; i < timeslots; i++) {
                            bestSchedule[i] = Arrays.copyOf(newSchedule[i], newSchedule[i].length);
                        }
                        bestCost = newCost;
                    }
                    cost = newCost;
                    better = true;
                }
            }
        }

        // for (int[] timeslot : schedule) {
        // for (int lecture : timeslot) {
        // System.out.print(lecture + "\t");
        // }
        // System.out.println();
        // }
        int total = 0;
        Map<List<Integer>, Integer> groups = p.getGroupsCount();
        // int sum = 0;
        // for (int value : groups.values()) {
        // sum+= value;
        // }a
        // System.err.println(sum);
        // System.err.println(p.getStudentCount());
        for (List<Integer> group : p.getGroups()) {
            for (int courseCode : group) {
                total += groups.get(group) * coursesCount[courseCode - 1];
            }
        }
        System.err.println("Total of lectures enrolled = " + total);
        System.err.println("Lectures taken = " + Evaluator.lecturesTaken(p, new Solution(bestSchedule)));
        return new Solution(bestSchedule);
    }

    private void init(int[][] schedule) {
        int aux = 0;
        int randCourse = 0;
        Map<Integer, Integer> coursesMap = new HashMap<Integer, Integer>();
        for (int i = 1; i <= courses; i++) {
            coursesMap.put(i, coursesCount[i - 1]);
        }
        for (int t = 0; t < timeslots; t++) {
            for (int cl = 0; cl < classrooms; cl++) {
                aux = 0;
                while (aux == 0) {
                    randCourse = ThreadLocalRandom.current().nextInt(courses) + 1;
                    aux = coursesMap.getOrDefault(randCourse, 0);
                }
                schedule[t][cl] = randCourse;
                coursesMap.put(randCourse, coursesMap.get(randCourse) - 1);
                if (coursesMap.get(randCourse) == 0)
                    coursesMap.remove(randCourse);
                else {
                    schedule[timeslots - 1 - t][classrooms - 1 - cl] = randCourse;
                    coursesMap.put(randCourse, coursesMap.get(randCourse) - 1);
                    if (coursesMap.get(randCourse) == 0)
                        coursesMap.remove(randCourse);
                }
                if (coursesMap.isEmpty())
                    return;
            }
        }
    }

    private void swap(int[][] schedule) {
        final int randTimeslot1 = ThreadLocalRandom.current().nextInt(timeslots);
        final int randTimeslot2 = ThreadLocalRandom.current().nextInt(timeslots);
        final int randClassroom1 = ThreadLocalRandom.current().nextInt(classrooms);
        final int randClassroom2 = ThreadLocalRandom.current().nextInt(classrooms);
        int aux = schedule[randTimeslot1][randClassroom1];
        schedule[randTimeslot1][randClassroom1] = schedule[randTimeslot2][randClassroom2];
        schedule[randTimeslot2][randClassroom2] = aux;
    }
}