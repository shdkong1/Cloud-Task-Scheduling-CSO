package id.ac.its.cloudCSO;

import javafx.util.Pair;
import org.apache.commons.math3.stat.StatUtils;

import java.lang.Math;
import java.util.ArrayList;
import java.util.Random;

public class Cat {

    private ArrayList<Integer> position;
    private ArrayList<Pair<Integer, Integer>> velocity;
    private Behavior flag;
    private final Evaluator eval;
    private final Parameters params;
    private final int dimensionSize;
    private final Random random;

    public Cat(ArrayList<Integer> pos, ArrayList<Pair<Integer, Integer>> vel,
               Behavior flag, Parameters params) {
        this.position = pos;
        this.velocity = vel;
        this.flag = flag;
        this.params = params;

        this.eval = new Evaluator();
        this.random = new Random();
        this.dimensionSize = this.position.size();
    }

    public void move() {
        if (this.flag == Behavior.SEEKING) {
            ArrayList<ArrayList<Integer>> candidateMoves = new ArrayList<>();

            for (int i = 0; i < params.seekingMemPool; i++) {
                ArrayList<Integer> positionCopy = new ArrayList<>();
                int srd = random.nextInt(dimensionSize + 1);
                int cdc = (int) (dimensionSize * params.countDimensionChg);
                int pos = (srd + cdc) % dimensionSize;

                if (i == 0) {
                    candidateMoves.add(positionCopy);
                    continue;
                }

                for (int j = 0; j < dimensionSize; j++) {
                    if ((srd < pos) && (j >= srd) && (j < pos)) {
                        positionCopy.add(position.get(pos - (j - srd)));
                    } else if ((j >= pos) && (j < srd)) {
                        positionCopy.add(position.get(srd - (j - pos)));
                    }
                    positionCopy.add(position.get(j));
                }
                candidateMoves.add(positionCopy);
            }

            double[] fitnessValues = new double[candidateMoves.size()];
            for (int i = 0; i < candidateMoves.size(); i++) {
                fitnessValues[i] = eval.evaluate();
            }
            double fitMin = StatUtils.min(fitnessValues);
            double fitMax = StatUtils.max(fitnessValues);

            double[] probabilities = new double[candidateMoves.size()];
            for (int i = 0; i < candidateMoves.size(); i++) {
                if (fitMin == fitMax) {
                    probabilities[i] = 1.0;
                }
                else {
                    probabilities[i] = Math.abs(fitnessValues[i] - fitMin) / (fitMax - fitMin);
                }
            }
            double probSum = StatUtils.sum(probabilities);
            for (int i = 0; i < probabilities.length; i++) {
                probabilities[i] = probabilities[i] / probSum;
            }
        }
    }
}

