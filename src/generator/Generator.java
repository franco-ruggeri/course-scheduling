package generator;

import java.util.Random;
import java.util.ArrayList;
import java.util.Collections;

public class Generator {
    public static void main(String[] args) {
        final Generator generator = new Generator(
                new int[]{1, 100},  //students
                new int[]{100, 1000},  //courses
                new int[]{1, 100},  //timeSlots
                new int[]{1, 100},  //classrooms
                new int[]{1, 100},  //rangeStudentsCourseCount
                new int[]{1000, 10000}, // rangeIndexOfCourse
                new int[]{1, 100}   //rangeCoursesLecturesCount
        );
        for (int i = 0; i < 1; i++) {
            final Problem p = generator.generate();
            System.out.println(p.toString());
//           String path = "problem_" + i + ".txt";
//            Generator.saveProblem(oneProblem, path);
        }
    }

    //0 means no course
    private final int[] rangeStudents;
    private final int[] rangeCourses;
    private final int[] rangeTimeSlots;
    private final int[] rangeClassrooms;
    private final int[] rangeStudentsCourseCount;
    private final int[] rangeIndexOfCourse;
    private final int[] rangeCoursesLecturesCount;

    private final Random rnd = new Random();

    public Generator(final int[] rangeStudents, final int[] rangeNumCourses, final int[] rangeTimeSlots,
                     final int[] rangeClassrooms, final int[] rangeStudentsCourseCount, final int[] rangeIndexOfCourse,
                     final int[] rangeCoursesLecturesCount) {
        this.rangeStudents = rangeStudents;
        this.rangeCourses = rangeNumCourses;
        this.rangeTimeSlots = rangeTimeSlots;
        this.rangeClassrooms = rangeClassrooms;
        this.rangeStudentsCourseCount = rangeStudentsCourseCount;
        this.rangeIndexOfCourse = rangeIndexOfCourse;
        this.rangeCoursesLecturesCount = rangeCoursesLecturesCount;
    }

    public Problem generate() {
        final Problem p = new Problem(
                getRndNumber(rangeStudents),
                getRndNumber(rangeCourses),
                getRndNumber(rangeTimeSlots),
                getRndNumber(rangeClassrooms)
        );

        generateCoursePerStudents(p.getStudents(), p.getCourseCount());
        generateNumOfLecturesPerCourses(p.getCourses());
        return p;
    }
    //
    private void generateCoursePerStudents(final int[][] s, final int courseCount) {
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
        }
    }

    //courseLectureCount
    private void generateNumOfLecturesPerCourses(final int[] courses) {
        final int len = courses.length;
        for (int i = 0; i < len; i++) {
            courses[i] = getRndNumber(rangeCoursesLecturesCount);
        }
    }

    //read from a file and outputs the problem
    private int getRndNumber(final int[] range) {
        final int diff = range[1] - range[0];
        return range[0] + rnd.nextInt(diff);
    }

    static Problem readProblem(final String loc) {
        return null;
    }


    static Solution readSolution(final String loc) {

        return null;
    }

    static void saveSolution(final Solution s, final String loc) {


    }

    static void saveProblem(final Problem p, final String loc) {

    }

}
