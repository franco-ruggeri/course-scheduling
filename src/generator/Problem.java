package generator;

import java.util.ArrayList;

public class Problem {
    private int studentCount;
    private int courseCount; // each course form 0 to count
    private int timeslotsCount;
    private int classRoomCount;

    //each int[] is the array of the courses the students will take
    private ArrayList<int[]> students;


    // each course needs to happen x amount of times
    private int[] courses;
}
