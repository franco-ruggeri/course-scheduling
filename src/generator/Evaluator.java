package generator;

import java.util.Arrays;
import java.util.List;
import java.util.Map; 

/**
 * Evaluator of solutions given a problem.
 */
public class Evaluator {
	private Problem problem;
	
	// total number of desired lectures (i.e. sum of lectures for courses)
	private int totalLectures;
	
	// total number of lectures enrolled by students (e.g. 3 students follow the same lectures => 3) 
	private int totalEnrolledLectures;
	
    public Evaluator(Problem problem) {
    	this.problem = problem;
    	this.totalLectures = countLectures();
    	this.totalEnrolledLectures = countEnrolledLectures();
    }
    
    /**
	 * Calculates the goodness of a solution.
	 * 
	 * @param solution solution to evaluate
	 * @return goodness of the solution
	 */
    public int evaluate(Solution solution) {
    	int total = 0;
    	int courseCount = problem.getCourseCount();
    	int[] desiredLecturesPerCourse = problem.getLecturesPerCourse();
    	int[] scheduledLecturesPerCourse = countScheduledLecturesPerCourse(solution);

    	// add number of taken lectures by each student
        total += countTakenLectures(solution);
        
        // penalty for infeasible lectures (i.e. more than one lectures of the same course in the same time slot)
        total -= countInfeasibleLectures(solution);
        
        // penalty if a course does not have the desired number of lectures
        for (int c=0; c<courseCount; c++)
        	total -= Math.abs(desiredLecturesPerCourse[c] - scheduledLecturesPerCourse[c]);
        
        return total;
    }

	/**
     * Calculates the percentage of infeasible lectures.
     * # infeasible lectures / # lectures * 100
     *  
     * @param solution solution
     * @return percentage of infeasible lectures
     */
    public double percentageInfeasibleLectures(Solution solution) {
    	return countInfeasibleLectures(solution) / (double) totalLectures * 100.0;
    }
    
    /**
     * Calculates the percentage of overlaps.
     * # overlaps / # enrolled lectures * 100
     * 
     * @param solution solution
     * @return percentage of overlaps
     */
    public double percentageOverlaps(Solution solution) {
    	if (countOverlaps(solution) / (double) totalEnrolledLectures * 100.0 < 0)
    		System.err.println(countOverlaps(solution) + ", " + totalEnrolledLectures);
    	return countOverlaps(solution) / (double) totalEnrolledLectures * 100.0;
    }

    /**
     * Calculates the percentage of correctly scheduled lectures.
     * (# schedule lectures - # infeasible lectures) / # lectures * 100
     *  
     * @param solution solution
     * @return percentage of correctly scheduled lectures
     */
	public double percentageScheduledLectures(Solution solution) {
		return (countScheduledLectures(solution) - countInfeasibleLectures(solution)) / (double) totalLectures * 100.0;
	}
	
	/**
	 * Calculates the percentage of courses with a number of scheduled lectures
	 * equal to the desired one.
	 * 
	 * @param solution solution
	 * @return percentage of courses with a number of scheduled lectures equal to
	 *         the desired one
	 */
	public double percentageCoursesWithRightNumberOfLectures(Solution solution) {
		int courseOk = 0;
		int courseCount = problem.getCourseCount();
		int[] desiredLecturesPerCourse = problem.getLecturesPerCourse();
    	int[] scheduledLecturesPerCourse = countScheduledLecturesPerCourse(solution);
    	
    	for (int c=0; c<courseCount; c++)
    		if (scheduledLecturesPerCourse[c] == desiredLecturesPerCourse[c])
    			courseOk++;
		
		return courseOk / (double) courseCount * 100.0;
	}
    
    /**
     * Calculates the sum of overlaps for the students.
     * 
     * @param solution solution
     * @return sum of overlaps for the students
     */
	private int countOverlaps(Solution solution) {
		return totalEnrolledLectures - countTakenLectures(solution);
	}

    /**
     * Calculates the total number of lectures in a problem.
     * 
     * @return total number of lectures in the problem
     */
    private int countLectures() {
        return Arrays.stream(problem.getLecturesPerCourse()).sum();
    }
    
    /**
     * Calculates the sum of lectures that the students should be able to take.
     * 
     * @return sum of lectures that the students should be able to take
     */
    private int countEnrolledLectures() {
    	int total = 0;
        int[] lecturesPerCourse = problem.getLecturesPerCourse();
        Map<List<Integer>, Integer> studentGroups = problem.getStudentGroups();
        
    	for (Map.Entry<List<Integer>, Integer> e : studentGroups.entrySet()) {
        	List<Integer> studentGroupCourses = e.getKey();
        	int studentGroupSize = e.getValue();
        	
            for (int course : studentGroupCourses)
            	total += studentGroupSize * lecturesPerCourse[course-1];
        }
    	
        return total;
    }

    /**
     * Calculates the total number of lectures scheduled in the solution.
     * 
     * @param solution solution
     * @return total number of lectures scheduled in the solution
     */
    private int countScheduledLectures(Solution solution) {
        return (int) Arrays.stream(solution.getSchedule())
        		.flatMapToInt(a -> Arrays.stream(a))
        		.filter(c -> c > 0)		// 0 means no course
        		.count();
    }

    /**
     * Calculates the sum of lectures that the students can take given the solution.
     * 
     * @param solution Solution
     * @return sum of lectures that each the students can take given the solution
     */
    private int countTakenLectures(Solution solution) {
        int total = 0;
        Map<List<Integer>, Integer> studentGroups = problem.getStudentGroups();
        
        for (int[] timeslot : solution.getSchedule()) {
        	for (Map.Entry<List<Integer>, Integer> e : studentGroups.entrySet()) {
            	List<Integer> studentGroupCourses = e.getKey();
            	int studentGroupSize = e.getValue();
                
                for (int course : timeslot) {
                	if (studentGroupCourses.contains(course)) {
                		total += studentGroupSize;
                		break;	// next group, this group cannot take other lectures
                	}
                }
            }
        }

		return total;
    }
	
	/**
	 * Calculates the sum of infeasible lectures. A lecture is infeasible if there
	 * is already one lecture of the same course in the same time slot.
	 * 
	 * @param solution solution
	 * @return sum of infeasible lectures
	 */
	private int countInfeasibleLectures(Solution solution) {
		int total = 0;
		boolean[] lectureInTimeslot = new boolean[problem.getCourseCount()];
		
		for (int[] timeslot : solution.getSchedule()) {
			Arrays.fill(lectureInTimeslot, false);
			for (int course : timeslot) {
				if (course > 0) {
					if (lectureInTimeslot[course-1])
						total++;
					else
						lectureInTimeslot[course-1] = true;
				}
			}
		}
		
		return total;
	}
	
	/**
	 * Counts the number of scheduled lectures for each course.
	 * 
	 * @param solution solution
	 * @return array of counts
	 */
	private int[] countScheduledLecturesPerCourse(Solution solution) {
    	int[] scheduledLecturesPerCourse = new int[problem.getCourseCount()];
    	
    	Arrays.stream(solution.getSchedule())
    		.flatMapToInt(t -> Arrays.stream(t))
    		.filter(c -> c > 0)
    		.forEach(c -> scheduledLecturesPerCourse[c-1]++);
    	
    	return scheduledLecturesPerCourse;
	}
	
}
