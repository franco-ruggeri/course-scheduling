
package solvers.annealing;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import generator.Evaluator;
import generator.Problem;
import generator.Solution;

/**
 * Annealing
 */
public class Annealing {

    private int temperature;
    private double coolingRate;
    private final int courses;
    private final int[] coursesCount;
    private final int timeslots;
    private final int classrooms;
    private final Problem p;

    public Annealing(int temperature, double coolingRate, Problem p) {
        this.temperature = temperature;
        this.coolingRate = coolingRate;
        this.p = p;
        this.courses = p.getCourseCount();
        this.coursesCount = p.getCourses();
        this.timeslots = p.getTimeslotsCount();
        this.classrooms = p.getClassroomCount();
    }

    public Solution simulate() {
        int[][] schedule = new int[timeslots][classrooms];
        int[][] newSchedule = new int[timeslots][classrooms];
        int[][] bestSchedule = new int[timeslots][classrooms];
        int cost = 0;
        int newCost = 0;
        int bestCost = 0;
        double keep = 0;
        double r = 0;
        init(schedule);
        bestCost = Evaluator.evaluate(p, new Solution(schedule));
        while (temperature > 1) {
            for (int i = 0; i < timeslots; i++) {
                newSchedule[i] = Arrays.copyOf(schedule[i], schedule[i].length);
            }
            swap(newSchedule);
            // System.err.println("Cost = "+cost);
            // System.err.println("New Cost = "+newCost);
            // System.err.println("Best Cost = "+bestCost);
            cost = Evaluator.evaluate(p, new Solution(schedule));
            newCost = Evaluator.evaluate(p, new Solution(newSchedule));
            if (newCost > cost) {
                for (int i = 0; i < timeslots; i++) {
                    schedule[i] = Arrays.copyOf(newSchedule[i], newSchedule[i].length);
                }
                if (newCost > bestCost) {
                    for (int i = 0; i < timeslots; i++) {
                        bestSchedule[i] = Arrays.copyOf(schedule[i], schedule[i].length);
                    }
                    bestCost = newCost;
                }
            } else {
                keep = Math.exp((cost - newCost) / temperature);
                r = ThreadLocalRandom.current().nextDouble();
                if (keep > r) {
                    for (int i = 0; i < timeslots; i++) {
                        schedule[i] = Arrays.copyOf(newSchedule[i], newSchedule[i].length);
                    }
                }
            }
            temperature *= 1 - coolingRate;
            // System.err.println("temperature = "+ temperature);
        }
        // for (int[] timeslot : schedule) {
        // for (int lecture : timeslot) {
        // System.out.print(lecture + "\t");
        // }
        // System.out.println();
        // }
        int total = 0;
        int sum = 0;
        Map<List<Integer>, Integer> groups = p.getGroupsCount();
        // for (int value : groups.values()) {
        //     sum+= value;
        // }a
        // System.err.println(sum);
        // System.err.println(p.getStudentCount());
        for (List<Integer> group : p.getGroups()) {
            for (int courseCode : group) {
                total += groups.get(group) * coursesCount[courseCode-1];
            }
        }
        System.err.println("Total of lectures enrolled = " + total);
        System.err.println("Lectures taken = " + bestCost);
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