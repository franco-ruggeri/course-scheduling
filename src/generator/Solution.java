package generator;

public class Solution {
    //2d array where each value represents the course being taught at that timeslot [i] and classroom [j] in solution [i][j]
    private int[][] solution;

    public Solution(int[][] solution){
        setSolution(solution);
    }

    public void setSolution(int[][] solution) {
        this.solution = solution;
    }

    public int[][] getSolution() {
        return solution;
    }
    
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