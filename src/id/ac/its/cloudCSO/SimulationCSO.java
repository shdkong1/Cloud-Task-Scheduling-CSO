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
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.DoubleStream;

public class SimulationCSO {

    private static PowerDatacenter datacenter1, datacenter2, datacenter3, datacenter4, datacenter5, datacenter6;
    private static List<Cloudlet> cloudletList;
    private static List<Vm> vmList;

    /**
     * Creates a list of VMs to be passed to the broker.
     *
     * @param userId ID of user who owns the VMs
     * @param vms    number of VMs to be created
     * @return       a list of VMs
     */
    private static List<Vm> createVM(int userId, int vms) {

        LinkedList<Vm> list = new LinkedList<Vm>();

                                        // VM Parameters
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

    /**
     * Returns an ArrayList of cloudlet lengths from a specified file.
     *
     * @param cloudletCount size of ArrayList, i.e. number of sizes to be retrieved from dataset.
     * @return              an ArrayList of task sizes.
     */
    private static ArrayList<Double> getSeedValue(int cloudletCount) {

        ArrayList<Double> seed = new ArrayList<>();
        String filepath = System.getProperty("user.dir") + "/SDSCDataset.txt"; // name of dataset file
        Log.printLine(System.getProperty(filepath));

        try {
            File fobj = new File(filepath);
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

    /**
     * Creates a list of cloudlets to be executed.
     *
     * @param userId        ID of user who owns the cloudlets.
     * @param cloudletCount number of cloudlets to be created.
     * @return              a list of cloudlets.
     */
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
            // Initialize simulation parameters
            int num_user = 1;
            Calendar calendar = Calendar.getInstance();
            boolean trace_flag = false;
            BufferedWriter outputWriter = null;
            outputWriter = new BufferedWriter(new FileWriter("filename.txt"));
            int vmNumber = 54;
            int cloudletNumber = 7395;

            // Initialize simulation
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

            // Create broker
            DatacenterBroker broker = createBroker();
            int brokerId = broker.getId();

            // Create VMs and cloudlets
            vmList = createVM(brokerId, vmNumber);
            cloudletList = createCloudlet(brokerId, cloudletNumber);

            // Submit VMs and cloudlets to broker
            broker.submitVmList(vmList);
            broker.submitCloudletList(cloudletList);

            // run CSO
            int cloudletLoopingNumber = cloudletNumber / vmNumber - 1;
            Random random = new Random();

            for (int cloudletIterator = 0; cloudletIterator < cloudletLoopingNumber; cloudletIterator++) {
                System.out.println("Cloudlet Iteration Number " + cloudletIterator);
                for (int datacenterIterator = 1; datacenterIterator <= 6; datacenterIterator++) {
                    CSOAlgorithm cso = new CSOAlgorithm(15, 20, datacenterIterator, cloudletIterator,
                            new Parameters(5, 0.8, 0.3, 0.7, 2.05),
                            new Evaluator(cloudletList, vmList));

                    System.out.println("Datacenter " + datacenterIterator);
//                    ArrayList<Integer> position = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9));

                    ArrayList<Integer> position = new ArrayList<>();
                    for (int i = 0; i < 9 /* || (cloudletIterator * 54 + i) < cloudletNumber */; i++) {
                        position.add(random.nextInt(9));
                    }

                    Cat currentCat = cso.run(position);

                    for (int assigner=0+(datacenterIterator-1)*9 + cloudletIterator*54; assigner<9+(datacenterIterator-1)*9 + cloudletIterator*54; assigner++)
                    {
                        broker.bindCloudletToVm(assigner, currentCat.getPosition().get(assigner%9));
//                        outputWriter.write(Long.toString(cloudletList.get(assigner).getCloudletLength())); // Print Cloudlet Length
//                        outputWriter.write(" ");
                        outputWriter.write(Long.toString(currentCat.getPosition().get(assigner%9)%9)); // Print Assigned VM ID %
                        outputWriter.write(" ");
                        if (assigner%9<8)
                        {
                        	outputWriter.write(",");
                        }
                    }
                    outputWriter.write("\n");
                }
            }

            CloudSim.startSimulation();

            outputWriter.flush();
            outputWriter.close();
            List<Cloudlet> newList = broker.getCloudletReceivedList();

            CloudSim.stopSimulation();
            printCLoudletList(newList);
            Log.printLine("Cloud Simulation Example Finished!");

        } catch (Exception e) {
            e.printStackTrace();
            Log.printLine("The simulation has been terminated due to an error");
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

    private static void printCLoudletList(List<Cloudlet> list) throws FileNotFoundException {

        int size = list.size();
        Cloudlet cloudlet = null;

        String indent = "\t";
        Log.printLine();
        Log.printLine("========== OUTPUT ==========");
        Log.printLine("Cloudlet ID" + indent + "Status" + indent +
                "Datacenter ID" + indent + "Vm ID" + indent + "Time" + indent +
                "Start Time" + indent + "Finish Time" + indent + "Waiting Time");

        double waitTimeSum = 0.0;
        double cpuTimeSum = 0.0;
        int totalValues = 0;
        DecimalFormat dft = new DecimalFormat("###.##");
        double responseTime[] = new double[size];

        for (int i = 0; i < size; i++) {
            cloudlet = list.get(i);
            Log.print(cloudlet.getCloudletId() + indent);

            if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS) {
                Log.print("SUCCESS" + indent);
                cpuTimeSum = cpuTimeSum + cloudlet.getActualCPUTime();
                waitTimeSum = waitTimeSum + cloudlet.getWaitingTime();
                Log.printLine(
                        (cloudlet.getResourceId() - 1) + indent + cloudlet.getVmId() + indent +
                        dft.format(cloudlet.getActualCPUTime()) + indent +
                        dft.format(cloudlet.getExecStartTime()) + indent +
                        dft.format(cloudlet.getFinishTime()) + indent +
                        dft.format(cloudlet.getWaitingTime())
                );
                totalValues++;
                responseTime[i] = cloudlet.getActualCPUTime();
            }
        }

        DoubleSummaryStatistics stats = DoubleStream.of(responseTime).summaryStatistics();
        Log.printLine();
        System.out.println("min: " + stats.getMin());
        System.out.println("Response Time:" + (cpuTimeSum / totalValues));

        Log.printLine();
        Log.printLine("Total CPU Time: " + cpuTimeSum);
        Log.printLine("Total Wait Time: " + waitTimeSum);
        Log.printLine("Total Cloudlets Finished: " + totalValues);
        Log.printLine();
        Log.printLine();

        Log.printLine("Average Cloudlets Finished" + (cpuTimeSum / totalValues));

        double totalStartTime = 0.0;
        for (int i = 0; i < size; i++) {
            totalStartTime = cloudletList.get(i).getExecStartTime();
        }
        double avgStartTime = totalStartTime / size;
        System.out.println("Average Start Time" + avgStartTime);

        double execTime = 0.0;
        for (int i = 0; i < size; i++) {
            execTime = cloudletList.get(i).getActualCPUTime();
        }
        double avgExecTime = execTime / size;
        System.out.println("Average Execution Time: " + avgExecTime);

        double totalTime = 0.0;
        for (int i = 0; i < size; i++) {
            totalTime = cloudletList.get(i).getFinishTime();
        }
        double avgTotalTime = totalTime / size;
        System.out.println("Average Finish Time: " + avgTotalTime);

        double avgWaitTime = cloudlet.getWaitingTime() / size;
        System.out.println("Average Waiting Time: " + avgWaitTime);

        Log.printLine();
        Log.printLine();

        double maxFT = 0.0;
        for (int i = 0; i < size; i++) {
            double currentFT = cloudletList.get(i).getFinishTime();
            if (currentFT > maxFT) {
                maxFT = currentFT;
            }
        }
        double throughput = size / maxFT;
        System.out.println("Throughput: " + throughput);

        double makespan = 0.0;
        double makespan_total = makespan + cloudlet.getFinishTime();
        System.out.println("Makespan: " + makespan_total);

        double degOfImbalance = (stats.getMax() - stats.getMin()) / (cpuTimeSum / totalValues);
        System.out.println("Imbalance Degree: " + degOfImbalance);

        double schedulingLength = waitTimeSum + makespan_total;
        Log.printLine("Total Scheduling Length: " + schedulingLength);

        double resourceUtil = (cpuTimeSum / (makespan_total * 54)) * 100;
        Log.printLine("Resource Utilization: " + resourceUtil);

        Log.printLine(String.format("Total Energy Consumption: %.2f kWh",
                (datacenter1.getPower() + datacenter2.getPower() + datacenter3.getPower() +
                 datacenter4.getPower() + datacenter5.getPower() + datacenter6.getPower()) /
                (3600*1000)
        ));
    }
}