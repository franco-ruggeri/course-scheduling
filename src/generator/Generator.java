package generator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Generator of random problems given ranges of parameters
 */
public class Generator {
    private final int[] rangeStudents;
    private final int[] rangeCourses;
    private final int[] rangeDays;
    private final int[] rangeHoursPerDay;
    private final int[] rangeClassrooms;
    private final int[] rangeStudentsCourseCount;
    private final int[] rangeCoursesLecturesCount;
    private static final Random random = new Random();
    
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

    //Generates a new problem based on the given parameters in the constructor
    public Problem generate() {
        Problem p = null;
        do {
            p = new Problem(getRandomInRange(rangeStudents), getRandomInRange(rangeCourses), getRandomInRange(rangeDays),
                    getRandomInRange(rangeHoursPerDay), getRandomInRange(rangeClassrooms));
            generateCoursePerStudents(p.getStudents(), p.getCourseCount());
            generateNumOfLecturesPerCourses(p.getLecturesPerCourse());
            generateGroups(p.getStudentGroups(), p.getStudents());
        } while (!p.isValid());
        return p;
    }

    //Generates groups of students (Students are in the same group if they take the same set of courses)
    private void generateGroups(Map<List<Integer>, Integer> groupsCount, int[][] students) {
        for (int[] group : students) {
            List<Integer> key = Arrays.stream(group).boxed().collect(Collectors.toList());
            groupsCount.put(key, groupsCount.getOrDefault(key, 0) + 1);
        }
    }

    //Generates the students and which courses they should take based on the constructor parameters.
    private void generateCoursePerStudents(int[][] s, final int courseCount) {
        final int studentCount = s.length;
        ArrayList<Integer> courseList = new ArrayList<Integer>();
        for (int courseIndex = 1; courseIndex <= courseCount; courseIndex++) {
            courseList.add(courseIndex);
        }

        for (int i = 0; i < studentCount; i++) {
            final int registeredCoursesCount = getRandomInRange(rangeStudentsCourseCount);
            s[i] = new int[registeredCoursesCount];
            Collections.shuffle(courseList);
            for (int j = 0; j < registeredCoursesCount; j++) {
                s[i][j] = courseList.get(j);
            }
            Arrays.sort(s[i]);
        }
    }

    // Generates the amount of lectures that each course should have
    private void generateNumOfLecturesPerCourses(final int[] courses) {
        final int len = courses.length;
        for (int i = 0; i < len; i++) {
            courses[i] = getRandomInRange(rangeCoursesLecturesCount);
        }
    }

    // returns a random number given a range.
    private int getRandomInRange(final int[] range) {
        final int diff = range[1] - range[0];
        return range[0] + random.nextInt(diff);
    }

}
