package id.ac.its.cloudCSO;

import javafx.util.Pair;
import org.apache.commons.math3.stat.StatUtils;

import java.lang.Math;
import java.util.ArrayList;
import java.util.Objects;
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

    public void move(ArrayList<Integer> bestPosition) throws Exception {
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

            double[] cumulativeDist = new double[probabilities.length];
            cumulativeDist[0] = probabilities[0];
            for (int i = 1; i < probabilities.length; i++) {
                cumulativeDist[i] = cumulativeDist[i - 1] + probabilities[i];
            }
            double rand = random.nextDouble();
            int nextPosIdx = 0;
            while (nextPosIdx < cumulativeDist.length && rand > cumulativeDist[nextPosIdx]) {
                nextPosIdx++;
            }

            position = candidateMoves.get(nextPosIdx);
        }
        else if (this.flag == Behavior.TRACING) {
            double r1 = random.nextDouble();
            this.velocity = addVel(
                    multiply(params.w, velocity),
                    multiply(r1 * params.c, subtract(bestPosition, position))
            );
        }
        else {
            throw new Exception("Unreachable");
        }
    }

    private void addPos(ArrayList<Integer> pos,
                                      ArrayList<Pair<Integer, Integer>> vel) {
        for (Pair<Integer, Integer> pair: vel) {
            int idx1 = pos.indexOf(pair.getKey());
            int idx2 = pos.indexOf(pair.getValue());
            int temp = pos.get(idx1);
            pos.set(idx1, pos.get(idx2));
            pos.set(idx2, temp);
        }
    }

    private ArrayList<Pair<Integer, Integer>> addVel(ArrayList<Pair<Integer, Integer>> vel1,
                                                     ArrayList<Pair<Integer, Integer>> vel2) {
        if (vel1.equals(vel2))
            return new ArrayList<>();

        ArrayList<Pair<Integer, Integer>> newVel = new ArrayList<>();
        newVel.addAll(vel1);
        newVel.addAll(vel2);
        for (int idx2 = vel1.size(), idx1 = idx2 - 1; idx2 < newVel.size() && idx1 >= 0; idx1--, idx2++) {
            if (newVel.get(idx1).equals(newVel.get(idx2))) {
                newVel.remove(idx1);
                newVel.remove(idx2);
                idx2 = idx1 - 1;
            }
            else break;
        }
        return newVel;
    }

    private ArrayList<Pair<Integer, Integer>> subtract(ArrayList<Integer> pos1,
                                                       ArrayList<Integer> pos2) {
        ArrayList<Pair<Integer, Integer>> vel = new ArrayList<>();
        ArrayList<Integer> pos1Copy = new ArrayList<>(pos1);
        while (!pos1Copy.equals(pos2)) {
            for (int i = 0; i < pos1Copy.size(); i++) {
                if (!pos1Copy.get(i).equals(pos2.get(i))) {
                    int j = pos2.indexOf(pos1Copy.get(i));
                    int temp = pos1Copy.get(i);
                    pos1Copy.set(i, pos1Copy.get(j));
                    pos1Copy.set(j, temp);
                    vel.add(new Pair<>(pos1Copy.get(j), pos1Copy.get(i)));
                }
            }
        }
        return vel;
    }

    private ArrayList<Pair<Integer, Integer>> multiply(double num,
                                                       ArrayList<Pair<Integer, Integer>> vel) {
        if (num == 0) return new ArrayList<>();

        double decPart = num % 1;
        int intPart = (int) (num - decPart);
        int newLength = (int) (decPart * vel.size());
        return (ArrayList<Pair<Integer, Integer>>) vel.subList(0, newLength);
    }
}

