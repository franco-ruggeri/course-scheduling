package generator;
import java.util.Random;

public class Generator {
    public static void main(String[] args) {
        final Generator generator = new Generator();
        for (int i = 0; i < 20; i++) {
            final Problem oneProblem = generator.generate();
            String path = "problem_" + i + ".txt";
            Generator.saveProblem(oneProblem, path);
        }
    }

    //read from a file and outputs the problem
    static Problem readProblem(final String loc) {

        return null;
    }


    static Solution readSolution(final String loc) {

        return null;
    }

    static void saveSolution(final Solution s, final String loc) {

    }

    static void saveProblem(final Problem p, final String loc) {

    }

    public Generator() {

    }

    public Problem generate() {
        Random random = new Random();
        int min =20;
        int max =50;
        int timeslotsCount =  random.nextInt((max - min)+1) +min;
        int classRoomCount = random.nextInt((max - min)+1) +min;
        Problem newProblem = new Problem(timeslotsCount ,classRoomCount);

        return newProblem;
    }

}
