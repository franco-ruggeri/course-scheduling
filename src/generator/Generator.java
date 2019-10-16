package generator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

public class Generator {
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

    private static boolean isValid(final Problem p) {
        final int capacity = p.getClassroomCount() * p.getTimeslotsCount();
        int sum = 0;
        final int[] c = p.getCourses();
        for (int i : c) {
            sum += i;
        }
        return capacity >= sum;
    }
}
