package id.ac.its.cloudCSO;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletSchedulerSpaceShared;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.power.PowerHost;
import org.cloudbus.cloudsim.power.PowerDatacenter;
import org.cloudbus.cloudsim.power.PowerHostUtilizationHistory;
import org.cloudbus.cloudsim.power.PowerVmAllocationPolicySimple;
import org.cloudbus.cloudsim.power.models.PowerModelLinear;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.util.*;

public class SimulationCSO {

    private static PowerDatacenter datacenter1, datacenter2, datacenter3, datacenter4, datacenter5, datacenter6;
    private static List<Cloudlet> cloudletList;
    private static List<Vm> vmList;

    private static List<Vm> createVM(int userId, int vms) {

        LinkedList<Vm> list = new LinkedList<Vm>();

        //VM Parameters
        long size = 10000;              // Image size (MB)
        int[] ram = {512, 1024, 2048};  // Memory (MB)
        int[] mips = {400, 500, 600};   // Processing Power (MIPS)
        long bw = 1000;                 // Bandwidth
        int peCount = 1;                // Number of CPUs
        String vmm = "Xen";             // VMM Name

        Vm[] vm = new Vm[vms];

        for (int i = 0; i < vms; i++) {
            vm[i] = new Vm(i, userId, mips[i % 3], peCount, ram[i % 3], bw, size, vmm,
                    new CloudletSchedulerSpaceShared());
            list.add(vm[i]);
        }

        return list;
    }

    private static ArrayList<Double> getSeedValue(int cloudletCount) {

        ArrayList<Double> seed = new ArrayList<>();
        Log.printLine(System.getProperty("user.dir") + "/SDSCDataset.txt");

        try {
            File fobj = new File(System.getProperty("user.dir") + "/SDSCDataset.txt");
            java.util.Scanner readFile = new Scanner(fobj);

            while (readFile.hasNextLine() && cloudletCount > 0) {
                seed.add(readFile.nextDouble());
                cloudletCount--;
            }
            readFile.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return seed;
    }

    private static List<Cloudlet> createCloudlet(int userId, int cloudletCount) {

        ArrayList<Double> randomSeed = getSeedValue(cloudletCount);
        LinkedList<Cloudlet> list = new LinkedList<>();

        //Cloudlet parameters
        long length = 0;
        long fileSize = 300;
        long outputSize = 300;
        int peCount = 1;
        UtilizationModel utilizationModel = new UtilizationModelFull();

        Cloudlet[] cloudlets = new Cloudlet[cloudletCount];

        for (int i = 0; i < cloudletCount; i++) {
            long finalLen = length + Double.valueOf(randomSeed.get(i)).longValue();
            cloudlets[i] = new Cloudlet(i, finalLen, peCount, fileSize, outputSize,
                    utilizationModel, utilizationModel, utilizationModel);
            cloudlets[i].setUserId(userId);
            list.add(cloudlets[i]);
        }

        return list;
    }

    public static void main(String[] args) {

        Log.printLine("Starting Cloud Simulation CSO...");

        try {
            int num_user = 1;
            Calendar calendar = Calendar.getInstance();
            boolean trace_flag = false;
            BufferedWriter outputWriter = null;
            outputWriter = new BufferedWriter(new FileWriter("filename.txt"));
            int vmNumber = 54;
            int cloudletNumber = 7395;

            CloudSim.init(num_user, calendar, trace_flag);

            int hostId = 0;
            datacenter1 = createDatacenter("DataCenter_1", hostId);
            hostId = 3;
            datacenter2 = createDatacenter("DataCenter_2", hostId);
            hostId = 6;
            datacenter3 = createDatacenter("DataCenter_3", hostId);
            hostId = 9;
            datacenter4 = createDatacenter("DataCenter_4", hostId);
            hostId = 12;
            datacenter5 = createDatacenter("DataCenter_5", hostId);
            hostId = 15;
            datacenter6 = createDatacenter("DataCenter_6", hostId);

            DatacenterBroker broker = createBroker();
            int brokerId = broker.getId();

            vmList = createVM(brokerId, vmNumber);
            cloudletList = createCloudlet(brokerId, cloudletNumber);

            broker.submitVmList(vmList);
            broker.submitCloudletList(cloudletList);

            // insert CSO here

            CloudSim.startSimulation();
        }
    }

    private static PowerDatacenter createDatacenter(String name, int hostId) {

        List<PowerHost> hostList = new ArrayList<>();

        List<Pe> peList1 = new ArrayList<>();
        List<Pe> peList2 = new ArrayList<>();
        List<Pe> peList3 = new ArrayList<>();

        int mipsUnused = 300;
        int mips1 = 400;
        int mips2 = 500;
        int mips3 = 600;

        peList1.add(new Pe(0, new PeProvisionerSimple(mips1)));
        peList1.add(new Pe(1, new PeProvisionerSimple(mips1)));
        peList1.add(new Pe(2, new PeProvisionerSimple(mips1)));
        peList1.add(new Pe(3, new PeProvisionerSimple(mipsUnused)));
        peList2.add(new Pe(4, new PeProvisionerSimple(mips2)));
        peList2.add(new Pe(5, new PeProvisionerSimple(mips2)));
        peList2.add(new Pe(6, new PeProvisionerSimple(mips2)));
        peList2.add(new Pe(7, new PeProvisionerSimple(mipsUnused)));
        peList3.add(new Pe(8, new PeProvisionerSimple(mips3)));
        peList3.add(new Pe(9, new PeProvisionerSimple(mips3)));
        peList3.add(new Pe(10, new PeProvisionerSimple(mips3)));
        peList3.add(new Pe(11, new PeProvisionerSimple(mipsUnused)));

        int ram = 12800;
        long storage = 1000000;
        int bw = 10000;
        int maxPower = 117;
        int staticPowerPercentage = 50;

        hostList.add(
                new PowerHostUtilizationHistory(
                        hostId,
                        new RamProvisionerSimple(ram),
                        new BwProvisionerSimple(bw),
                        storage,
                        peList1,
                        new VmSchedulerTimeShared(peList1),
                        new PowerModelLinear(maxPower, staticPowerPercentage)
                )
        );
        hostId++;

        hostList.add(
                new PowerHostUtilizationHistory(
                        hostId,
                        new RamProvisionerSimple(ram),
                        new BwProvisionerSimple(bw),
                        storage,
                        peList2,
                        new VmSchedulerTimeShared(peList2),
                        new PowerModelLinear(maxPower, staticPowerPercentage)
                )
        );
        hostId++;

        hostList.add(
                new PowerHostUtilizationHistory(
                        hostId,
                        new RamProvisionerSimple(ram),
                        new BwProvisionerSimple(bw),
                        storage,
                        peList3,
                        new VmSchedulerTimeShared(peList3),
                        new PowerModelLinear(maxPower, staticPowerPercentage)
                )
        );

        String arch = "x86";
        String os = "Linux";
        String vmm = "Xen";
        double timeZone = 10.0;
        double cost = 3.0;
        double costPerMem = 0.05;
        double costPerStorage = 0.1;
        double costPerBw = 0.1;
        LinkedList<Storage> storageList = new LinkedList<>();

        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
                arch, os, vmm, hostList, timeZone,cost, costPerMem, costPerStorage, costPerBw
        );

        PowerDatacenter datacenter = null;
        try {
            datacenter = new PowerDatacenter(name, characteristics,
                    new PowerVmAllocationPolicySimple(hostList), storageList, 9);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return datacenter;
    }

    private static DatacenterBroker createBroker() {

        DatacenterBroker broker = null;
        try {
            broker = new DatacenterBroker("Broker");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return broker;
    }

}