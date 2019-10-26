package generator;

/**
 * Represents a schedule.
 */
public class Solution {
	/*
	 * We represent the schedule as a TxCL matrix, where each cell represents a
	 * lecture of a course. This way, there cannot be 2 lectures in the same
	 * classroom at the same time, so we satisfy one of the constraints implicitly.
	 */
    private int[][] schedule;

    public Solution(int[][] solution){
        setSchedule(solution);
    }

    public void setSchedule(int[][] solution) {
        this.schedule = solution;
    }

    public int[][] getSchedule() {
        return schedule;
    }

    @Override
    public String toString() {
    	StringBuffer sb = new StringBuffer();
    	for (int t=0; t<schedule.length; t++) {
    		for (int cl=0; cl<schedule[t].length; cl++)
    			sb.append(schedule[t][cl]).append("\t");
    		sb.append("\n");
    	}
    	return sb.toString();
    }
}