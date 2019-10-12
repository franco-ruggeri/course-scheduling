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
}