package id.ac.its.cloudCSO;

public class Parameters {
    public int seekingMemPool;
    public float countDimensionChg;
    public float mixtureRatio;
    public float w;
    public float c;

    public Parameters(int smp, float cdc, float mr, float w, float c) {
        this.seekingMemPool = smp;
        this.countDimensionChg = cdc;
        this.mixtureRatio = mr;
        this.w = w;
        this.c = c;
    }
}
