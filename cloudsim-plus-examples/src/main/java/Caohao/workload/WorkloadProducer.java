package Caohao.workload;


import Caohao.Constants;
import sun.misc.VMSupport;

import java.io.*;
import java.util.*;

public class WorkloadProducer {

    private static Random random=new Random();


    private static File vmFile  =new File("VmWorkload.txt");



    public static void initVmWorkLoad() throws IOException {

        FileWriter fw=new FileWriter(vmFile,false);
        BufferedWriter bw=new BufferedWriter(fw);
        PrintWriter pw=new PrintWriter(bw);

        //pes
        for (int i = 0; i < Constants.VMS; i++) {
            pw.printf("%d",1+random.nextInt(Constants.VM_PES));
            pw.printf(" ");
        }
        pw.printf("\n");

        //带宽
        for (int i = 0; i < Constants.VMS; i++) {
            pw.printf("%d",(1+random.nextInt(2))*1000);
            pw.printf(" ");
        }
        pw.printf("\n");


        //delay
        double delay=0;
        delay=P_rand(Constants.lamda)+1;
        pw.printf("%f",delay);
        pw.printf(" ");
        for (int i = 1; i < Constants.VMS; i++) {
//            int newDelay=random.nextInt(1);
//            pw.printf("%d",delay+=newDelay);
//            delay=delay+newDelay;
            delay=P_rand(Constants.lamda);
            pw.printf("%f",delay);
            pw.printf(" ");
        }
        pw.printf("\n");

        //qos
        for (int i = 0; i < Constants.VMS; i++) {
            pw.printf("%2.1f",(double)(5+random.nextInt(5))/10);
            pw.printf(" ");
        }
        pw.printf("\n");

        //length
        for (int i = 0; i < Constants.VMS; i++) {
            pw.printf("%d",(2+random.nextInt(15))*20000);
            pw.printf(" ");
        }
        pw.printf("\n");

        pw.close();
        bw.close();
        fw.close();

    }


    public static double P_rand(double Lamda){      // 泊松分布
        double x=0,b=1,c=Math.exp(-Lamda),u;
        do {
            u=Math.random();
            b *=u;
            if(b>=c)
                x++;
        }while(b>=c);
        return x;
    }

    public Map<String,List<?>> readVmWorkLoad() throws IOException {

        FileReader fileReader=new FileReader(vmFile);
        BufferedReader bufferedReader=new BufferedReader(fileReader);
        LineNumberReader reader=new LineNumberReader(bufferedReader);

        String pes = reader.readLine();
        String[] pesSplit = pes.split(" ");

        ArrayList<Integer> pesList=new ArrayList<>();

        for (String pesValue:pesSplit){
            pesList.add(Integer.valueOf(pesValue));
        }

        String bw=reader.readLine();
        String[] bwSplit = bw.split(" ");
        ArrayList<Integer> bwList=new ArrayList<>();
        for (String bwValue:bwSplit){
            bwList.add(Integer.valueOf(bwValue));
        }

        ArrayList<Double> delayList=new ArrayList<>();
        String delay=reader.readLine();
        String[] delaySplit = delay.split(" ");
        for (String delayValue:delaySplit){
            delayList.add(Double.valueOf(delayValue));
        }

        ArrayList<Double> qosList=new ArrayList<>();
        String qos=reader.readLine();
        String[] qosSplit = qos.split(" ");
        for (String qosValue:qosSplit){
            qosList.add(Double.valueOf(qosValue));
        }

        ArrayList<Integer> taskLengthList=new ArrayList<>();
        String taskLength=reader.readLine();
        String[] lengthSplit = taskLength.split(" ");
        for (String lengthValue:lengthSplit){
            taskLengthList.add(Integer.valueOf(lengthValue));
        }

        reader.close();
        bufferedReader.close();
        fileReader.close();

        HashMap<String,List<?>> map=new HashMap<>();
        map.put("pes",pesList);
        map.put("bw",bwList);
        map.put("delay",delayList);
        map.put("qos",qosList);
        map.put("taskLength",taskLengthList);

        return map;

    }

    public static void main(String[] args) throws IOException {

        initVmWorkLoad();

//        WorkloadProducer producer=new WorkloadProducer();
//        producer.readVmWorkLoad();

//        File file=new File("p.txt");
//        FileWriter fw=new FileWriter(file,false);
//        BufferedWriter bw=new BufferedWriter(fw);
//        PrintWriter pw=new PrintWriter(bw);
//
//        double temp=0.0;
//        for (int i = 0; i < 100; i++) {
//            double t=P_rand(2);
//            pw.printf("%6.2f ",temp+t);
//            pw.printf("\n");
//            temp=temp+t;
//        }
//
//        pw.close();
//        bw.close();
//        fw.close();


    }




}
