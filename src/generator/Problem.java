package generator;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Problem {
    private final int studentCount;
    private final int courseCount; // each course is assigned a number from 1 [inc] to coureCount [inc] at University
    private final int timeslotsCount;  // timeslots of all courses in total
    private final int classroomCount;

    //each int[] is the array of the courses the students will take
    // first value is index of student, second value is the list of index of a course
    private final int[][] students;

    // each course needs to happen x amount of times
    private final int[] courses;

    private Map<List<Integer>, Integer> groupsCount = new HashMap<List<Integer>, Integer>();
    private Set<List<Integer>> groups = new HashSet<List<Integer>>();

    public Problem(final int studentCount, final int courseCount, final int timeslotsCount, final int classRoomCount) {
        this.timeslotsCount = timeslotsCount;
        this.classroomCount = classRoomCount;
        this.studentCount = studentCount;
        this.courseCount = courseCount;
        this.students = new int[studentCount][];
        this.courses = new int[courseCount];
    }

    public Problem(final int studentCount, final int courseCount, final int timeslotsCount, final int classRoomCount,
                   final int[][] students, final int[] courses) {
        this.timeslotsCount = timeslotsCount;
        this.classroomCount = classRoomCount;
        this.studentCount = studentCount;
        this.courseCount = courseCount;
        this.students = students;
        this.courses = courses;
        for (int[] group:students){
            List<Integer> key = Arrays.stream(group).boxed().collect(Collectors.toList());
            this.groups.add(key);
            this.groupsCount.put(key, groupsCount.getOrDefault(key, 0) + 1);
        }
    }

    public String toString() {
        String s = "#Courses: " + courseCount + "\t#TimeSlots: " + timeslotsCount + "\t#Classrooms: " + classroomCount +
                "\t#Students: " + studentCount + "\n";
        s+= "Number of lecture(s) per course:\n";
        s+="[";
        for (int i = 0; i < courseCount; i++) {
            s+= " " + courses[i];
        }
        s+= " ]\n";

        s+= "Students: \n";
        s+= "Index of curr student :: [ courses per student]\n";
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

    public int getStudentCount() {
        return studentCount;
    }

    public Map<List<Integer>, Integer> getGroupsCount() {
        return groupsCount;
    }

    public Set<List<Integer>> getGroups() {
        return groups;
    }
}
