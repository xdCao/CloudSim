package Caohao;

/**
 * created by xdCao on 2018/4/26
 */

public class Constants {

    static final int SCHEDULE_INTERVAL = 2;
    static final int Req_INTERVAL= 2 ;

    static final int HOSTS = 2;
    static final int VMS = 4;
//    static final int TASKS=5;

    static final int    HOST_MIPS = 1000; //for each PE

    static final int    HOST_INITIAL_PES = 32;

    static final long   HOST_BW = 16000L; //Mb/s


    static final int    VM_MIPS = 1000; //for each PE
    static final long   VM_SIZE = 1000; //image size (MB)
    static final int    VM_RAM = 10000; //VM memory (MB)
    static final double VM_BW = HOST_BW/(double)VMS;
    static final int    VM_PES = 7;
    static final int   CLOUDLET_LENGHT = 10000;

    static final long   CLOUDLET_FILESIZE = 300;
    static final long   CLOUDLET_OUTPUTSIZE = 300;


    static final long   HOST_RAM = 5000000; //host memory (MB)
    static final long   HOST_STORAGE = 10000000; //host storage

    static final double CLOUDLET_INITIAL_CPU_PERCENTAGE = 0.8;

    static final double HOST_UTILIZATION_THRESHOLD_FOR_VM_MIGRATION = 0.7;

    static final double CLOUDLET_CPU_INCREMENT_PER_SECOND = 0.1;

}
