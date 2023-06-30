package id.ac.its.cloudCSO;

import javafx.util.Pair;
import org.apache.commons.math3.stat.StatUtils;

import java.lang.Math;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

/**
 * An implementation of cats for use in CSO Algorithm. The calculations for <code>move()</code> are adapted from
 * Bouzidi et al.
 *
 * @author shidqi
 */
public class Cat {

    private ArrayList<Integer> position;
    private ArrayList<Pair<Integer, Integer>> velocity;
    private Behavior flag;
    private int datacenterIterator;
    private int cloudletIterator;
    private final Evaluator eval;
    private final Parameters params;
    private final int dimensionSize;
    private final Random random;

    public Cat(ArrayList<Integer> pos, ArrayList<Pair<Integer, Integer>> vel,
               Behavior flag, int dciter, int cletiter, Parameters params, Evaluator eval) {
        this.position = pos;
        this.velocity = vel;
        this.flag = flag;
        this.params = params;
        this.eval = eval;
        this.datacenterIterator = dciter;
        this.cloudletIterator = cletiter;

        this.random = new Random();
        this.dimensionSize = this.position.size();
    }

    public void move(ArrayList<Integer> bestPosition) {
        if (this.flag == Behavior.SEEKING) {
            ArrayList<ArrayList<Integer>> candidateMoves = new ArrayList<>();

            for (int i = 0; i < params.seekingMemPool; i++) {
                ArrayList<Integer> positionCopy = new ArrayList<>();
                int srd = random.nextInt(dimensionSize);
                int cdc = (int) (dimensionSize * params.countDimensionChg);
                int pos = (srd + cdc) % dimensionSize;

                if (i == 0) {
                    candidateMoves.add(this.position);
                    continue;
                }

                for (int j = 0; j < dimensionSize; j++) {
                    if ((srd < pos) && (j >= srd) && (j <= pos)) {
//                        positionCopy.add(position.get(pos - (j - srd)));
                        positionCopy.add(random.nextInt(dimensionSize));
                    } else if ((j >= pos) && (j <= srd)) {
//                        positionCopy.add(position.get(srd - (j - pos)));
                        positionCopy.add(random.nextInt(dimensionSize));
                    } else {
                        positionCopy.add(position.get(j));
                    }
                }
                candidateMoves.add(positionCopy);
            }

            double[] fitnessValues = new double[candidateMoves.size()];
            for (int i = 0; i < candidateMoves.size(); i++) {
                fitnessValues[i] = eval.evaluate(candidateMoves.get(i), datacenterIterator, cloudletIterator);
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
            addPos(this.position, this.velocity);
        }
        else {
//            throw new Exception("Unreachable");
        }
    }

    private void addPos(ArrayList<Integer> pos,
                                      ArrayList<Pair<Integer, Integer>> vel) {
        for (Pair<Integer, Integer> pair: vel) {
//            int idx1 = pos.indexOf(pair.getKey());
//            int idx2 = pos.indexOf(pair.getValue());
//            int temp = pos.get(idx1);
//            pos.set(idx1, pos.get(idx2));
//            pos.set(idx2, temp);
            pos.set(pair.getKey(), pair.getValue());
        }
    }

    private ArrayList<Pair<Integer, Integer>> addVel(ArrayList<Pair<Integer, Integer>> vel1,
                                                     ArrayList<Pair<Integer, Integer>> vel2) {
        if (vel1.equals(vel2))
            return vel1;

        ArrayList<Pair<Integer, Integer>> newVel1 = new ArrayList<>(vel1);
        ArrayList<Pair<Integer, Integer>> newVel2 = new ArrayList<>(vel2);
//        for (int idx2 = vel1.size(), idx1 = idx2 - 1; idx2 < newVel.size() && idx1 >= 0; idx1--, idx2++) {
//            if (newVel.get(idx1).equals(newVel.get(idx2))) {
//                newVel.remove(idx2);
//                newVel.remove(idx1);
//                idx2 = idx1 - 1;
//            }
//            else break;
//        }
        for (int i = 0; i < vel1.size(); i++) {
            for (int j = 0; j < vel2.size(); j++) {
                if (vel1.get(i).getKey().equals(vel2.get(j).getKey())) {
                    newVel1.set(i, vel2.get(j));
                    newVel2.remove(newVel1.get(i));
                }
            }
        }
        if (!newVel2.isEmpty()) {
            newVel1.addAll(newVel2);
        }

        return newVel1;
    }

    private ArrayList<Pair<Integer, Integer>> subtract(ArrayList<Integer> pos1,
                                                       ArrayList<Integer> pos2) {
        ArrayList<Pair<Integer, Integer>> vel = new ArrayList<>();
//        ArrayList<Integer> pos1Copy = new ArrayList<>(pos1);
//        while (!pos1.equals(pos2)) {
            for (int i = 0; i < pos1.size(); i++) {
                if (!pos1.get(i).equals(pos2.get(i))) {
//                    int j = pos2.indexOf(pos1Copy.get(i));
//                    int temp = pos1Copy.get(i);
//                    pos1Copy.set(i, pos1Copy.get(j));
//                    pos1Copy.set(j, temp);
//                    vel.add(new Pair<>(pos1Copy.get(j), pos1Copy.get(i)));
                    vel.add(new Pair<>(i, pos2.get(i)));
                }
            }
//        }
        return vel;
    }

    private ArrayList<Pair<Integer, Integer>> multiply(double num,
                                                       ArrayList<Pair<Integer, Integer>> vel) {
        if (num == 0) return new ArrayList<>();

        double decPart = num % 1;
        int intPart = (int) (num - decPart);
        int newLength = (int) (decPart * vel.size());
        ArrayList<Pair<Integer, Integer>> newList = new ArrayList<>(vel.subList(0, newLength));
        return newList;
    }

    public ArrayList<Integer> getPosition() {
        return position;
    }

    public void setFlag(Behavior flag) {
        this.flag = flag;
    }
}

