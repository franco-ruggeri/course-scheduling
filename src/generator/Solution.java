package generator;

public class Solution {
    //2d array where each value represents the course being taught at that timeslot [i] and classroom [j] in solution [i][j]
    private int[][] solution;

    //Constructor
    public Solution(int[][] solution){
        setSolution(solution);
    }

    //Setter
    public void setSolution(int[][] solution) {
        this.solution = solution;
    }

    //Getter
    public int[][] getSolution() {
        return solution;
    }

    //Turns the solution into a string.
    @Override
    public String toString() {
    	StringBuffer sb = new StringBuffer();
    	for (int t=0; t<solution.length; t++) {
    		for (int cl=0; cl<solution[t].length; cl++)
    			sb.append(solution[t][cl]).append("\t");
    		sb.append("\n");
    	}
    	return sb.toString();
    }
}