package Caohao;

/**
 * created by xdCao on 2018/4/26
 */

public class Constants {

    public static final double VAR_THRESHOLD=0.0025;

    static final int SCHEDULE_INTERVAL = 1;

    static final int HOSTS = 10;
    static final int VMS = 50;

    static final int    HOST_MIPS = 1000; //for each PE

    static final int    HOST_INITIAL_PES = 32;

    static final long   HOST_BW = 1600000L; //Mb/s


    static final int    VM_MIPS = 1000; //for each PE
    static final long   VM_SIZE = 1000; //image size (MB)
    static final int    VM_RAM = 10000; //VM memory (MB)
    static final double VM_BW = HOST_BW/10;
    static final int    VM_PES = 7;
    static final int   CLOUDLET_LENGHT = 10000;

    static final long   CLOUDLET_FILESIZE = 300;
    static final long   CLOUDLET_OUTPUTSIZE = 300;


    static final long   HOST_RAM = 5000000; //host memory (MB)
    static final long   HOST_STORAGE = 10000000; //host storage




    public static int[] vmPes={ 7,3,4,7,6,
                                4,5,3,2,5,
                                6,1,9,6,4,
                                5,1,7,8,5,
                                6,7,4,5,3,
                                7,3,4,7,6,
                                4,5,3,2,5,
                                6,1,9,6,4,
                                5,1,7,8,5,
                                6,7,4,5,3,
                                7,3,4,7,6,
                                4,5,3,2,5,
                                6,1,9,6,4,
                                5,1,7,8,5,
                                6,7,4,5,3,
                                7,3,4,7,6,
                                4,5,3,2,5,
                                6,1,9,6,4,
                                5,1,7,8,5,
                                6,7,4,5,3};//25个vm的pes

    public static int[] taskLength={12000,10000,11000,18000,16000,
                                    15000,19000,11000,12000,16000,
                                    17000,15000,10000,17000,12000,
                                    18000,14000,12000,13000,15000,
                                    18000,19000,10000,16000,18000,
                                    12000,10000,11000,18000,16000,
                                    15000,19000,11000,12000,16000,
                                    17000,15000,10000,17000,12000,
                                    18000,14000,12000,13000,15000,
                                    18000,19000,10000,16000,18000,
                                    12000,10000,11000,18000,16000,
                                    15000,19000,11000,12000,16000,
                                    17000,15000,10000,17000,12000,
                                    18000,14000,12000,13000,15000,
                                    18000,19000,10000,16000,18000,
                                    12000,10000,11000,18000,16000,
                                    15000,19000,11000,12000,16000,
                                    17000,15000,10000,17000,12000,
                                    18000,14000,12000,13000,15000,
                                    18000,19000,10000,16000,18000};//25个task的length


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
}
