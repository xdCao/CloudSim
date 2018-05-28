package Caohao.workload;


import Caohao.Constants;
import sun.misc.VMSupport;

import java.io.*;
import java.util.ArrayList;
import java.util.Random;

public class WorkloadProducer {

    private static Random random=new Random();


    private static File vmFile =null;



    public static void initVmWorkLoad() throws IOException {

        vmFile =new File("VmWorkload.txt");



        FileWriter fw=new FileWriter(vmFile,false);
        BufferedWriter bw=new BufferedWriter(fw);
        PrintWriter pw=new PrintWriter(bw);

        for (int i = 0; i < Constants.VMS; i++) {
            pw.printf("%d",1+random.nextInt(Constants.VM_PES));
            pw.printf(" ");
        }
        pw.printf("\n");

        for (int i = 0; i < Constants.VMS; i++) {
            pw.printf("%d",(1+random.nextInt(10))*100);
            pw.printf(" ");
        }
        pw.printf("\n");


        int delay=0;
        for (int i = 0; i < Constants.VMS; i++) {
            int newDelay=random.nextInt(3);
            pw.printf("%d",delay+=newDelay);
            delay=delay+newDelay;
            pw.printf(" ");
        }
        pw.printf("\n");

        for (int i = 0; i < Constants.VMS; i++) {
            pw.printf("%d",(2+random.nextInt(8))/10);
            pw.printf(" ");
        }
        pw.printf("\n");

        pw.close();

    }

    public void readVmWorkLoad() throws IOException {

        FileReader fileReader=new FileReader(vmFile);
        BufferedReader bufferedReader=new BufferedReader(fileReader);
        LineNumberReader reader=new LineNumberReader(bufferedReader);

        String pes = reader.readLine();
        String[] pesSplit = pes.split(" ");

        ArrayList<Integer> pesList=new ArrayList<>();

        for (String pesValue:pesSplit){
            pesList.add(Integer.valueOf(pesValue));
        }

        for (Integer integer:pesList){
            System.out.println(integer);
        }

    }

    public static void main(String[] args) throws IOException {

        initVmWorkLoad();

        WorkloadProducer producer=new WorkloadProducer();
        producer.readVmWorkLoad();

    }




}
