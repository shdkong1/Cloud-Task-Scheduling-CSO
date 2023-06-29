package id.ac.its.cloudCSO;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.lang.Math;

/**
 * An implementation of Discrete Cat Swarm Optimization Algorithm.
 * The original CSO algorithm is proposed by Chu et al. in 2006. The discrete variation implemented here is based
 * on adaptations made by Bouzidi in his 2019 paper on CSO for Open Shop Scheduling Problem which, in turn, adapted the
 * changes to its calculations based on adaptations for Discrete Particle Swarm Optimization proposed by Clerc.
 *
 * @author shidqi
 */
public class CSOAlgorithm {

    private int iterations;
    private int numCats;
    private Parameters params;
    private Cat[] cats;
    private double[] scores;
    private Cat bestCat;
    private double bestScore;
    private int cloudletIterator;
    private int datacenterIterator;
    private Evaluator eval;
    private final Random random;

    /**
     * Creates a new CSO Algorithm instance.
     * In theory, an algorithm instance can be run multiple times. However, for this simulation, each instance
     * is only run once and for every iteration in simulation, a new instance is created (see <code>SimulationCSO.java</code>.)
     *
     * @param iter      Number of iterations for each run.
     * @param numCats   Number of cats.
     * @param dciter    Iterator for datacenters.
     * @param cletiter  Iterator for cloudlets.
     * @param params    Algorithm parameters.
     * @param eval      Evaluation function wrapped inside an <code>Evaluator</code> class.
     */
    public CSOAlgorithm(int iter, int numCats, int dciter, int cletiter, Parameters params, Evaluator eval) {
        this.iterations = iter;
        this.numCats = numCats;
        this.datacenterIterator = dciter;
        this.cloudletIterator = cletiter;
        this.params = params;
        this.eval = eval;

        this.random = new Random();
    }

    /**
     * Runs the algorithm instance based on a start position and returns the best cat in population.
     *
     * @param position  Starting position for all cats.
     * @return          Best cat in the population.
     */
    public Cat run(ArrayList<Integer> position) {
        int numSeeking = (int) ((params.mixtureRatio * numCats) / 100);
        bestScore = 0;
        bestCat = null;
        scores = new double[numCats];
        cats = new Cat[numCats];

        Behavior[] behaviors = new Behavior[numCats];
        Arrays.fill(behaviors, Behavior.TRACING);
        for (int i = 0; i < numSeeking; i++) {
            behaviors[random.nextInt(numCats)] = Behavior.SEEKING;
        }

        for (int i = 0; i < numCats; i++) {
            cats[i] = new Cat(
                    position,
                    new ArrayList<>(),
                    behaviors[i],
                    datacenterIterator,
                    cloudletIterator,
                    params,
                    eval
            );
        }

        for (int i = 0; i < iterations; i++) {
            int catIter = 0;
            for (Cat c: cats) {
                double score = eval.evaluate(c.getPosition(), datacenterIterator, cloudletIterator);
                scores[catIter] = Math.max(score, scores[catIter]);
                if (score >= bestScore) {
                    bestScore = score;
                    bestCat = c;
                }
                catIter++;
            }

            for (Cat c: cats) {
                c.move(bestCat.getPosition());
            }

            Arrays.fill(behaviors, Behavior.TRACING);
            for (int j = 0; j < numSeeking; j++) {
                behaviors[random.nextInt(numCats)] = Behavior.SEEKING;
            }
            catIter = 0;
            for (Cat c: cats) {
                c.setFlag(behaviors[catIter]);
                catIter++;
            }
        }

        return bestCat;
    }
}
