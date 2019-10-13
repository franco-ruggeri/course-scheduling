package generator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Random;
import java.util.ArrayList;
import java.util.Collections;

public class Generator {
    public static void main(String[] args) {
        final Generator generator = new Generator(
                new int[]{1, 100},  //students
                new int[]{100, 101},  //courses
                new int[]{1, 100},  //timeSlots
                new int[]{1, 100},  //classrooms
                new int[]{100, 101},  //rangeStudentsCourseCount
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
    private final int[] rangeCoursesLecturesCount;

    private final Random rnd = new Random();

    public Generator(final int[] rangeStudents, final int[] rangeNumCourses, final int[] rangeTimeSlots,
                     final int[] rangeClassrooms, final int[] rangeStudentsCourseCount,
                     final int[] rangeCoursesLecturesCount) {
        this.rangeStudents = rangeStudents;
        this.rangeCourses = rangeNumCourses;
        this.rangeTimeSlots = rangeTimeSlots;
        this.rangeClassrooms = rangeClassrooms;
        this.rangeStudentsCourseCount = rangeStudentsCourseCount;
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
            Array.sort(s[i]);
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


    static Solution readSolution(final String loc) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(loc));
            Solution s = new Solution(stringToDoubleArray(reader.readLine()));
            reader.close();
            return s;
        } catch (Exception e){
            System.out.println(e);
        }
        return null;
    }

    static void saveSolution(final Solution s, final String loc) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(loc));
            final int[][] a = s.getSolution();
            writer.write(intArrayToString(a));
            writer.close();
        }catch (Exception e){
            System.out.println(e);
        }
    }

    static Problem readProblem(final String loc) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(loc));
            final String[] splitOne = reader.readLine().split(" ");
            final Problem p = new Problem(Integer.parseInt(splitOne[0]),
                    Integer.parseInt(splitOne[1]),
                    Integer.parseInt(splitOne[2]),
                    Integer.parseInt(splitOne[3]),
                    stringToDoubleArray(reader.readLine()),
                    stringToArray(reader.readLine())
            );
            reader.close();
            return p;
        } catch (Exception e){
            System.out.println(e);
        }
        return null;
    }

    static void saveProblem(final Problem p, final String loc) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(loc));
            writer.write(p.getStudentCount() + " " + p.getCourseCount() + " " + p.getTimeslotsCount() + " " + p.getClassroomCount() + "\n");
            writer.write(intArrayToString(p.getStudents()));
            writer.write(intArrayToString(p.getCourses()));
            writer.close();
        }catch (Exception e){
            System.out.println(e);
        }
    }

    static String intArrayToString(int[][] a){
        final int len = a.length;
        final int len2 = a[0].length;
        String ans = len + " " + len2;
        for (int i = 0; i < len * len2; i++){
            for (int j = 0; j < len2; j++) {
                ans += " " + a[i][j];
            }
        }
        ans+="\n";
        return ans;
    }

    static String intArrayToString(int[] a){
        final int len = a.length;
        String ans = Integer.toString(len);
        for (int i = 0; i < len; i++){
            ans += " " + a[i];
        }
        ans+="\n";
        return ans;
    }

    static int[] stringToArray(String s){
        final String[] split = s.split(" ");
        final int[] a = new int[Integer.parseInt(split[0])];
        for (int i = 0; i < a.length; i++) {
            a[i] = Integer.parseInt(split[i+1]);
        }
        return a;
    }

    static int[][] stringToDoubleArray(String s){
        final String[] split = s.split(" ");
        final int[][] a = new int[Integer.parseInt(split[0])][Integer.parseInt(split[1])];
        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < a[0].length; j++) {
                a[i][j] = Integer.parseInt(split[j+i*a.length+2]);
            }
        }
        return a;
    }
}
