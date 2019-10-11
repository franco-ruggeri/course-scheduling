package generator;

import java.util.ArrayList;

public class Problem {
    private int studentCount;
    private int courseCount; // each course form 0 to count at University
    private  int timeslotsCount;  // timeslots of all courses in total
    private int classRoomCount;

    //each int[] is the array of the courses the students will take
    private ArrayList<int[]> students;


    // each course needs to happen x amount of times
    private int[] courses;

    public Problem(int timeslotsCount, int classRoomCount) {
        this.timeslotsCount = timeslotsCount;
        this.classRoomCount = classRoomCount;
    }
}
