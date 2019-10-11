package generator;

import java.util.ArrayList;

public class Problem {
    private final int studentCount;
    private final int courseCount; // each course is assigned a number from 1 [inc] to coureCount [inc] at University
    private final int timeslotsCount;  // timeslots of all courses in total
    private final int classroomCount;

    //each int[] is the array of the courses the students will take
    private final int[][] students;

    // each course needs to happen x amount of times
    private final int[] courses;

    public Problem(final int studentCount, final int courseCount, final int timeslotsCount, final int classRoomCount) {
        this.timeslotsCount = timeslotsCount;
        this.classroomCount = classRoomCount;
        this.studentCount = studentCount;
        this.courseCount = courseCount;
        this.students = new int[studentCount][];
        this.courses = new int[courseCount];
    }

    public String toString(){
        String s = "#Courses: " + courseCount + "\t#TimeSlots: " + timeslotsCount + "\t#ClassRoom: " + classroomCount +
                "\t#Students: " + studentCount + "\n";
        s+= "Courses:\n";
        s+="[";
        for (int i = 0; i < courseCount; i++) {
            s+= " " + courses[i];
        }
        s+= " ]\n";

        s+= "Students: \n";
        for (int i = 0; i < studentCount; i++) {
            s += i + "\t:: [";
            final int sLength = students[i].length;
            for (int j = 0; j < sLength; j++) {
                s += " " + students[i][j];
            }
            s += " ]\n";
        }
        return s;
    }

    public int getCourseCount() {
        return courseCount;
    }

    public int getTimeslotsCount() {
        return timeslotsCount;
    }

    public int getClassroomCount() {
        return classroomCount;
    }

    public int[][] getStudents() {
        return students;
    }

    public int[] getCourses() {
        return courses;
    }
}
