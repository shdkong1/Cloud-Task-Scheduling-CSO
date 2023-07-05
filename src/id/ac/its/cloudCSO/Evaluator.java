package id.ac.its.cloudCSO;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Vm;

import java.util.ArrayList;
import java.util.List;

/**
 * This class provides the evaluation function(s) used for the CSO Algorithm.
 *
 * @author shidqi
 */
public class Evaluator {

    private List<Cloudlet> cloudletList;
    private List<Vm> vmList;

    public Evaluator(List<Cloudlet> cloudletList, List<Vm>vmList) {
        this.cloudletList = cloudletList;
        this.vmList = vmList;
    }
    public double evaluate(ArrayList<Integer> position, int dataCenterIterator, int cloudletIteration) {

        double totalExecutionTime = 0;
        double mips = 0;
        double failureRate = 0.04847468455;
        int iterator=0;
        dataCenterIterator = dataCenterIterator-1;

        for (int i = dataCenterIterator*9 + cloudletIteration*54; i<9 + dataCenterIterator*9 + cloudletIteration*54; i++)
        {
            int vm = position.get(iterator);
            if (vm%9 == 0 || vm%9 == 3 || vm%9 == 6)
            {
                mips = 400;
            }else if (vm%9 == 1 || vm%9 == 4 || vm%9 == 7)
            {
                mips = 500;
            }else if (vm%9 == 2 || vm%9 == 5 || vm%9 == 8)
            {
                mips = 600;
            }else break;

            totalExecutionTime = totalExecutionTime + cloudletList.get(i).getCloudletLength() / mips;
            iterator++;
        }

//        int random = getRandomPoisson(failureRate);
//        double poisson=(getPoisson(failureRate, random, 9));
//        double poisson = 1;

        // Calculate fitness
        double fitness = totalExecutionTime;
        //Log.printLine("Fitness " + fitness);
//
//        // Store fitness
//        individual.setFitness(fitness);

        return fitness;
    }
}
