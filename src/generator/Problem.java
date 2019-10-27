package generator;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/*
 * Represents a problem as a set of parameters. Some data structures are also 
 * generated to simplify the analysis of solutions.
 */
public class Problem {
	/*
	 * Important: course IDs start from 1, 0 is reserved for no course.
	 */
	
	private final int timeslotsCount; 	// number of time slots
    private final int classroomCount;	// number of classrooms
    private final int studentCount;		// number of students
    private final int courseCount; 		// number of courses
    
    // number of lectures for courses (each course requires its own number)
    private final int[] lecturesPerCourse;
    
    /*
     * A student is represented as array of courses he wishes to take.
     * So, an array of students is an array of array of courses.
     */
    private final int[][] students;
    
	/*
	 * To analyze more efficiently the overlaps, it is convenient to group students
	 * that take exactly the same set of courses. The key of this map is the list of
	 * courses taken by the group of students (in other words, a type of student),
	 * while the value is the size of the group.
	 */
    private Map<List<Integer>, Integer> studentGroups = new HashMap<>();
    
    // additional info for visualization of schedule
    private final int dayCount; 		// number of days of the schedule
    private final int timeslotsPerDay; 	// number of time slots per day

    public Problem(final int studentCount, final int courseCount, final int dayCount, final int timeslotsPerDay,
            final int classroomCount) {
        this.dayCount = dayCount;
        this.timeslotsPerDay = timeslotsPerDay;
        this.timeslotsCount = dayCount * timeslotsPerDay;
        this.classroomCount = classroomCount;
        this.studentCount = studentCount;
        this.courseCount = courseCount;
        this.students = new int[studentCount][];
        this.lecturesPerCourse = new int[courseCount];
    }
    
    public Problem(final int studentCount, final int courseCount, final int days, final int hoursPerDay,
            final int classroomCount, final int[][] students, final int[] lecturesPerCourse) {
        this.dayCount = days;
        this.timeslotsPerDay = hoursPerDay;
        this.timeslotsCount = days * hoursPerDay;
        this.classroomCount = classroomCount;
        this.studentCount = studentCount;
        this.courseCount = courseCount;
        this.students = students;
        this.lecturesPerCourse = lecturesPerCourse;
        for (int[] group : students) {
            List<Integer> key = Arrays.stream(group).boxed().collect(Collectors.toList());
            this.studentGroups.put(key, studentGroups.getOrDefault(key, 0) + 1);
        }
    }
    
	/**
	 * Checks if a problem is feasible, i.e. if there are enough classrooms and time
	 * slots for the total lectures.
	 * 
	 * @return true if the problem is valid
	 */
    public boolean isValid() {
        final int capacity = classroomCount * timeslotsCount;
        int sum = Arrays.stream(lecturesPerCourse).sum();
        return capacity >= sum;
    }
    
    @Override
    public String toString() {
        String s = "#Courses: " + courseCount + "\t#TimeSlots: " + timeslotsCount + "\t#Classrooms: " + classroomCount
                + "\t#Students: " + studentCount + "\n";
        s += "Number of lecture(s) per course:\n";
        s += "[";
        for (int i = 0; i < courseCount; i++) {
            s += " " + lecturesPerCourse[i];
        }
        s += " ]\n";

        s += "Students: \n";
        s += "Index of curr student :: [ courses per student]\n";
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

    @Override
    public boolean equals(Object o){
    	if (!(o instanceof Problem))
    		return false;
    	
    	Problem p = (Problem) o;
        return (studentCount == p.getStudentCount() &&
                courseCount == p.getCourseCount() &&
                timeslotsCount == p.getTimeslotsCount() &&
                classroomCount == p.getClassroomCount() &&
                students.equals(p.getStudents()) &&
                lecturesPerCourse.equals(p.getLecturesPerCourse()));
    }

    
    /*
     * Getters
     */
    
	public int getTimeslotsCount() {
		return timeslotsCount;
	}

	public int getClassroomCount() {
		return classroomCount;
	}

	public int getStudentCount() {
		return studentCount;
	}

	public int getCourseCount() {
		return courseCount;
	}

	public int[] getLecturesPerCourse() {
		return lecturesPerCourse;
	}

	public int[][] getStudents() {
		return students;
	}

	public Map<List<Integer>, Integer> getStudentGroups() {
		return studentGroups;
	}

	public int getDayCount() {
		return dayCount;
	}

	public int getTimeslotsPerDay() {
		return timeslotsPerDay;
	}
	
}
