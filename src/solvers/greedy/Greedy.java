package solvers.greedy;

import generator.Problem;
import generator.Solution;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Greedy {
    public static void main(String[] args) {

    }
    final Problem p;

    public Greedy(Problem p) {
        this.p = p;
    }

    public Solution run(){
        Map<List<Integer>, Integer> treeMap = new TreeMap<List<Integer>, Integer>(p.getGroupsCount());
        for (Map.Entry<List<Integer>, Integer> entry : treeMap.entrySet()) {
            System.out.println("Key : " + entry.getKey()
                    + " Value : " + entry.getValue());
        }

        return null;
    }


}
