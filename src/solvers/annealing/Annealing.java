
package solvers.annealing;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import generator.Evaluator;
import generator.Problem;
import generator.Solution;
import solvers.Solver;

/**
 * Simulated Annealing
 * Is a seach method to solve combinatorial problems inpired by the metalurgic process of annealing
 * were a metal is heated to a high temperature and then is cooled gradualy to reach the best possible struture
 * The analogy will be tha next:
 * Solutions of a combinatorial problem ~ physical states
 * Heuristic Value ~ Energy of a state
 */
public class Annealing implements Solver {

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

    public Solution solve() {
        int[][] schedule;
        int[][] newSchedule = new int[timeslots][classrooms];
        int[][] bestSchedule = new int[timeslots][classrooms];
        int cost = 0;
        int newCost = 0;
        int bestCost = 0;
        double keep = 0;
        double r = 0;
        // generate random schedule to start with
        schedule = init();
        cost = Evaluator.evaluate(p, new Solution(schedule));
        bestCost = cost;
        // while we are not frozen
        while (temperature > 1) {
            // generate a neighbor solution by swaping two random lectures
            newSchedule = swap(schedule);
            newCost = Evaluator.evaluate(p, new Solution(newSchedule));
            // if the new cost is better
            if (newCost > cost) {
                // we make the new schedule the schedule
                for (int i = 0; i < timeslots; i++) {
                    schedule[i] = Arrays.copyOf(newSchedule[i], newSchedule[i].length);
                }
                // if necessary we update the best schedule
                if (newCost > bestCost) {
                    for (int i = 0; i < timeslots; i++) {
                        bestSchedule[i] = Arrays.copyOf(newSchedule[i], newSchedule[i].length);
                    }
                    bestCost = newCost;
                }
                cost = newCost;
            } else { // if not we use the temperature and randomness
                keep = Math.exp((cost - newCost) / temperature);
                r = ThreadLocalRandom.current().nextDouble();
                if (keep > r) {
                    for (int i = 0; i < timeslots; i++) {
                        schedule[i] = Arrays.copyOf(newSchedule[i], newSchedule[i].length);
                    }
                    cost = newCost;
                }
            }
            // we decrease the temperature
            temperature *= 1 - coolingRate;
        }
        System.err.println("Total of lectures enrolled = " + Evaluator.countEnrolledLectures(p));
        System.err.println("Lectures taken = " + Evaluator.countTakenLectures(p, new Solution(bestSchedule)));
        return new Solution(bestSchedule);
    }

    /**
     * Generate random schedule
     * @return valid schedule
     */
    private int[][] init() {
        int[][] schedule;
        do {
            schedule = new int[timeslots][classrooms];
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
                        return schedule;
                }
            }
        } while (!Evaluator.isValid(p, new Solution(schedule)));
        return null;
    }

    /**
     * Swap two random Lectures
     * @param schedule
     * @return Random neighbor schedule
     */
    private int[][] swap(int[][] schedule) {
        int[][] newSchedule = new int[schedule.length][];
        for (int i = 0; i < timeslots; i++) {
            newSchedule[i] = Arrays.copyOf(schedule[i], schedule[i].length);
        }
        int randTimeslot1 = ThreadLocalRandom.current().nextInt(timeslots);
        int randTimeslot2 = ThreadLocalRandom.current().nextInt(timeslots);
        int randClassroom1 = ThreadLocalRandom.current().nextInt(classrooms);
        int randClassroom2 = ThreadLocalRandom.current().nextInt(classrooms);
        int aux = newSchedule[randTimeslot1][randClassroom1];
        newSchedule[randTimeslot1][randClassroom1] = newSchedule[randTimeslot2][randClassroom2];
        newSchedule[randTimeslot2][randClassroom2] = aux;
        return newSchedule;
    }
}