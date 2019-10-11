package generator;
import java.util.Random;

public class Generator {
    public static void main(String[] args) {
        final Generator generator = new Generator(
                new int[]{1, 100},//students
                new int[]{1, 100},//courses
                new int[]{1, 100},//timeSlots
                new int[]{1, 100}//classrooms
                );
        for (int i = 0; i < 20; i++) {
            final Problem oneProblem = generator.generate();
            String path = "problem_" + i + ".txt";
            Generator.saveProblem(oneProblem, path);
        }
    }
    private final int[] rangeStudents;
    private final int[] rangeCourses;
    private final int[] rangeTimeSlots;
    private final int[] rangeClassrooms;

    private final Random rnd = new Random();

    public Generator(final int[] rangeStudents, final int[] rangeCourses, final int[] rangeTimeSlots, final int[] rangeClassrooms) {
        this.rangeStudents = rangeStudents;
        this.rangeCourses = rangeCourses;
        this.rangeTimeSlots = rangeTimeSlots;
        this.rangeClassrooms = rangeClassrooms;
    }

    public Problem generate() {
        final Problem p = new Problem(
                getRndNumber(rangeStudents),
                getRndNumber(rangeCourses),
                getRndNumber(rangeTimeSlots),
                getRndNumber(rangeClassrooms)
                );



        return p;
    }
    //read from a file and outputs the problem

    private int getRndNumber(final int[] range){
        final int diff = range[1] - range[0];
        return range[0] + rnd.nextInt(diff);
    }

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

}
