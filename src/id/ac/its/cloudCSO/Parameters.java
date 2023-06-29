package id.ac.its.cloudCSO;

/**
 * This is a wrapper class for the parameters used in CSO Algorithm. As the parameters for an algorithm instance does
 * not change, wrapping all parameter values into a class would reduce the number of parameters passed in <code>CSOAlgorithm</code>
 * and <code>Cat</code>. This is done to improve code readability.
 *
 * @author shidqi
 */
public class Parameters {
    public int seekingMemPool;
    public double countDimensionChg;
    public double mixtureRatio;
    public double w;
    public double c;

    public Parameters(int smp, double cdc, double mr, double w, double c) {
        this.seekingMemPool = smp;
        this.countDimensionChg = cdc;
        this.mixtureRatio = mr;
        this.w = w;
        this.c = c;
    }
}
