package generator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import solvers.annealing.Annealing;
import solvers.genetic.Genetic;
import solvers.hill.Hill;
import solvers.lp.ILP;

public class Generator {
    public static Generator predefined() {
        return new Generator(new int[] { 10, 15 }, // students
                new int[] { 7, 8 }, // courses
                new int[] { 4, 5 }, // days
                new int[] { 4, 5 }, // hoursPerDay
                new int[] { 5, 6 }, // classrooms
                new int[] { 3, 5 }, // rangeStudentsCourseCount
                new int[] { 7, 15 } // rangeCoursesLecturesCount
        );
    }

    public static void main(String[] args) {
        final Generator generator = Generator.predefined();

        final Problem p = generator.generate();
        final Annealing annealing = new Annealing(100000, .01, p);
        final Solution solA = annealing.solve();

        System.out.println("ANN: isvalid: " + Evaluator.isValid(p, solA));
        System.out.println("ANN " + Evaluator.evaluate(p, solA));

        final ILP ilp = new ILP(p);
        final Solution solILP = ilp.solve();

        System.out.println("ILP: isvalid: " + Evaluator.isValid(p, solILP));
        System.out.println("ILP " + Evaluator.evaluate(p, solILP));
        System.out.println(solILP.toString());
    }
    // 0 means no course
    private final int[] rangeStudents;
    private final int[] rangeCourses;
    private final int[] rangeDays;
    private final int[] rangeHoursPerDay;
    private final int[] rangeClassrooms;
    private final int[] rangeStudentsCourseCount;
    private final int[] rangeCoursesLecturesCount;

    private final Random rnd = new Random();

    public Generator(final int[] rangeStudents, final int[] rangeNumCourses, final int[] rangeDays,
            final int[] rangeHoursPerDay, final int[] rangeClassrooms, final int[] rangeStudentsCourseCount,
            final int[] rangeCoursesLecturesCount) {
        this.rangeStudents = rangeStudents;
        this.rangeCourses = rangeNumCourses;
        this.rangeDays = rangeDays;
        this.rangeHoursPerDay = rangeHoursPerDay;
        this.rangeClassrooms = rangeClassrooms;
        this.rangeStudentsCourseCount = rangeStudentsCourseCount;
        this.rangeCoursesLecturesCount = rangeCoursesLecturesCount;
    }

    public Problem generate() {
        Problem p = null;
        do {
            p = new Problem(getRndNumber(rangeStudents), getRndNumber(rangeCourses), getRndNumber(rangeDays),
                    getRndNumber(rangeHoursPerDay), getRndNumber(rangeClassrooms));
            generateCoursePerStudents(p.getStudents(), p.getCourseCount());
            generateNumOfLecturesPerCourses(p.getCourses());
            generateGroups(p.getGroups(), p.getGroupsCount(), p.getStudents());
        } while (!isValid(p));
        return p;
    }

    private void generateGroups(Set<List<Integer>> groups, Map<List<Integer>, Integer> groupsCount, int[][] students) {
        for (int[] group : students) {
            List<Integer> key = Arrays.stream(group).boxed().collect(Collectors.toList());
            groups.add(key);
            groupsCount.put(key, groupsCount.getOrDefault(key, 0) + 1);
        }
    }

    //
    private void generateCoursePerStudents(int[][] s, final int courseCount) {
        final int studentCount = s.length;
        ArrayList<Integer> courseList = new ArrayList<Integer>();
        for (int courseIndex = 1; courseIndex <= courseCount; courseIndex++) {
            courseList.add(courseIndex);
        }

        for (int i = 0; i < studentCount; i++) {
            final int registeredCoursesCount = getRndNumber(rangeStudentsCourseCount);
            s[i] = new int[registeredCoursesCount];
            Collections.shuffle(courseList);
            for (int j = 0; j < registeredCoursesCount; j++) {
                s[i][j] = courseList.get(j);
            }
            Arrays.sort(s[i]);
        }
    }

    // courseLectureCount
    private void generateNumOfLecturesPerCourses(final int[] courses) {
        final int len = courses.length;
        for (int i = 0; i < len; i++) {
            courses[i] = getRndNumber(rangeCoursesLecturesCount);
        }
    }

    // read from a file and outputs the problem
    private int getRndNumber(final int[] range) {
        final int diff = range[1] - range[0];
        return range[0] + rnd.nextInt(diff);
    }

    static boolean isValid(final Problem p) {
        final int capacity = p.getClassroomCount() * p.getTimeslotsCount();
        int sum = 0;
        final int[] c = p.getCourses();
        for (int i : c) {
            sum += i;
        }

        return capacity >= sum;
    }
}
