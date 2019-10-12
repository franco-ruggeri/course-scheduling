package generator;

public class Evaluator {
    enum Heuristics{
        OVERLAPPING, LESSTIMESLOTS
    }
    //OVERLAPPING: sum of lectures that students can attend.
    //LESSTIMESLOTS: exampele of potential heurisic modifier
    // maximizing

    //returns what aiming to minimize; e.g. Overlapping would return one (0x1 << 0), Lesstimeslots would return (0x1 << 1)
    static int getGoals() {
        return 0;
    }

    static int evaluate(final Problem p, final Solution s) {

        return 0;
    }

    static boolean isValid (final Problem p, final Solution s){

        return false;
    }
}
