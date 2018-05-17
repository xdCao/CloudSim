package Caohao;

/**
 * created by xdCao on 2018/4/26
 */

public class Constants {

    public static final double VAR_THRESHOLD=0.01;

    public static final int SCHEDULE_INTERVAL = 1;

    public static final int HOSTS = 50;
    public static final int VMS = 100;

    public static final int    HOST_MIPS = 1000; //for each PE

    public static final int    HOST_INITIAL_PES = 64;

    public static final long   HOST_BW = 1600000L; //Mb/s


    public static final int    VM_MIPS = 1000; //for each PE
    public static final long   VM_SIZE = 1000; //image size (MB)
    public static final int    VM_RAM = 10000; //VM memory (MB)
    public static final double VM_BW = HOST_BW/10;
    public static final int    VM_PES = 7;
    public static final int   CLOUDLET_LENGHT = 10000;

    public static final long   CLOUDLET_FILESIZE = 300;
    public static final long   CLOUDLET_OUTPUTSIZE = 300;


    public static final long   HOST_RAM = 5000000; //host memory (MB)
    public static final long   HOST_STORAGE = 10000000; //host storage




    public static int[] vmPes={ 4,3,4,2,6,
                                4,5,3,2,5,
                                6,1,2,6,4,
                                5,1,1,4,5,
                                6,5,4,5,3,
                                2,3,4,3,6,
                                4,5,3,2,5,
                                6,1,2,6,4,
                                5,1,3,4,5,
                                6,2,4,5,3,
                                5,3,4,4,6,
                                4,5,3,2,5,
                                6,1,5,6,4,
                                5,1,5,2,5,
                                6,4,4,5,3,
                                5,3,4,3,6,
                                4,5,3,2,5,
                                6,1,6,6,4,
                                5,1,3,4,5,
                                6,5,4,5,3};//25个vm的pes

    public static int[] taskLength={2000,10000,11000,18000,16000,
                                    5000,19000,11000,12000,16000,
                                    7000,15000,10000,17000,12000,
                                    8000,14000,12000,13000,15000,
                                    8000,19000,10000,6000,18000,
                                    2000,10000,11000,8000,16000,
                                    5000,19000,11000,2000,16000,
                                    7000,15000,10000,17000,12000,
                                    8000,14000,12000,13000,15000,
                                    8000,19000,10000,16000,8000,
                                    2000,10000,11000,18000,6000,
                                    5000,19000,11000,12000,6000,
                                    7000,15000,10000,7000,12000,
                                    8000,14000,12000,3000,15000,
                                    8000,19000,10000,6000,18000,
                                    2000,10000,11000,8000,16000,
                                    5000,9000,11000,12000,16000,
                                    7000,5000,10000,17000,12000,
                                    8000,4000,12000,13000,15000,
                                    8000,9000,10000,16000,18000};//25个task的length

    public static int taskLengthChange=1;



    public static double[] taskQos={0.6,0.8,0.6,0.7,0.5,
                                    0.9,0.8,0.5,0.6,0.7,
                                    0.6,0.9,0.7,0.8,0.7,
                                    0.6,0.8,0.9,0.8,0.9,
                                    0.7,0.8,0.9,0.6,0.8,
                                    0.8,0.8,0.6,0.7,0.5,
                                    0.7,0.8,0.5,0.6,0.7,
                                    0.6,0.9,0.7,0.8,0.7,
                                    0.6,0.8,0.9,0.8,0.9,
                                    0.7,0.8,0.9,0.6,0.8,
                                    0.8,0.8,0.6,0.7,0.5,
                                    0.7,0.8,0.5,0.6,0.7,
                                    0.6,0.9,0.7,0.8,0.7,
                                    0.6,0.8,0.9,0.8,0.9,
                                    0.7,0.8,0.9,0.6,0.8,
                                    0.8,0.8,0.6,0.7,0.5,
                                    0.7,0.8,0.5,0.6,0.7,
                                    0.6,0.9,0.7,0.8,0.7,
                                    0.6,0.8,0.9,0.8,0.9,
                                    0.7,0.8,0.9,0.6,0.8};

    public static double QOS_CHANGE=0.3;


    public static double[] delay={
        4,5,7,10,12,
        14,18,19,20,25,
        26,31,32,36,44,
        45,51,51,54,55,
        56,57,60,65,63,
        63,63,64,63,66,
        64,65,73,72,75,
        76,71,72,76,74,
        75,81,83,84,85,
        86,82,84,85,83,
        95,93,94,94,96,
        94,95,93,92,95,
        106,101,105,106,104,
        105,12,53,22,54,
        61,42,43,54,35,
        52,33,44,35,66,
        41,52,33,24,55,
        67,18,63,63,42,
        54,12,31,42,52,
        6,5,4,5,3};//25个vm的delay

}
