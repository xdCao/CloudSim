package Caohao;

import java.io.*;
import java.sql.Time;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SimulationProcess {

    private static int num=15;

    private static double start=0.1;

    public static void main(String[] args) throws IOException {

//        firstFitLambda();
//        firstFitMigLambda();
//        greedyLambda();
//        uniformLambda();


        time("firstFit");
        time("firstFitMig");
        time("greedy");
        time("uniform");

//        pm("firstFit");
//        pm("firstFitMig");
//        pm("greedy");
//        pm("uniform");


    }

    public static void pm(String name) throws IOException{

        File wFile=new File("/Users/caohao/"+name+"PM.txt");
        FileWriter fw=new FileWriter(wFile,false);
        BufferedWriter bw=new BufferedWriter(fw);
        PrintWriter pw=new PrintWriter(bw);

        for (int i = 0; i < 10; i++) {
            int index=20+20*i;
            List<Double> pm=new ArrayList<>();
            for (int j = 1; j < 101; j++) {
                File file=new File("/Users/caohao/vm1000pm"+index+"lambda1.0/"+name+j+"-vm1000pm"+index+"lambda1.0.txt");
                double[] doubles = readLastLine(file);
                pm.add(doubles[1]);
            }
            for(Double num:pm){
                System.out.print(num+", ");
            }
            System.out.println();
            Double collect = pm.stream().collect(Collectors.averagingDouble(Double::doubleValue));
            System.out.println(collect);
            pw.printf("%10.6f",collect);
            pw.printf("\n");
        }

        pw.close();
        bw.close();
        fw.close();

    }


    public static void time(String name) throws IOException{

        File wFile=new File("/Users/caohao/"+name+"Time.txt");
        FileWriter fw=new FileWriter(wFile,false);
        BufferedWriter bw=new BufferedWriter(fw);
        PrintWriter pw=new PrintWriter(bw);


        HashMap<Double,ArrayList<Double>> hashMap=new HashMap<>();

        for (int j = 1; j < 101; j++) {
            File file=new File("/Users/caohao/vm1000pm100lambda0.8/"+name+j+"-vm1000pm100lambda0.8.txt");
            FileReader fr=new FileReader(file);
            BufferedReader br=new BufferedReader(fr);
            String str = null;
            while((str = br.readLine()) != null) {
                double[] doubles = getDoubles(str);
                if (hashMap.containsKey(doubles[0])){
                    hashMap.get(doubles[0]).add(doubles[1]);
                }else {
                    ArrayList<Double> list=new ArrayList<>();
                    list.add(doubles[1]);
                    hashMap.put(doubles[0],list);
                }
            }

            br.close();
            fr.close();

        }

        HashMap<Double,Double> result=new HashMap<>();

        for (Map.Entry entry:hashMap.entrySet()){

            double tmp=0;
            ArrayList<Double> value = (ArrayList<Double>) entry.getValue();
            for (Double ddouble:value){
                tmp+=ddouble;
            }
            tmp=tmp/value.size();
            result.put((Double) entry.getKey(),tmp);

        }

        for (Map.Entry entry:result.entrySet()){

            pw.printf("%10.6f %10.6f",entry.getKey(),entry.getValue());
            pw.printf("\n");

        }



        pw.close();
        bw.close();
        fw.close();

    }






    public static void firstFitLambda() throws IOException {
        File wFile=new File("/Users/caohao/firstFitLambda.txt");
        FileWriter fw=new FileWriter(wFile,false);
        BufferedWriter bw=new BufferedWriter(fw);
        PrintWriter pw=new PrintWriter(bw);

        for (int i = 0; i < num; i++) {
            double index=oneAfterPoint(start+i*0.1);
            List<Double> firstFitLambda=new ArrayList<>();
            for (int j = 1; j < 101; j++) {
                File file=new File("/Users/caohao/vm1000pm100lambda"+index+"/firstFit"+j+"-vm1000pm100lambda"+index+".txt");
                double[] doubles = readLastLine(file);
                firstFitLambda.add(doubles[1]);
            }
            for(Double num:firstFitLambda){
                System.out.print(num+", ");
            }
            System.out.println();
            Double collect = firstFitLambda.stream().collect(Collectors.averagingDouble(Double::doubleValue));
            System.out.println(collect);
            pw.printf("%10.6f",collect);
            pw.printf("\n");
        }

        pw.close();
        bw.close();
        fw.close();
    }





    public static void firstFitMigLambda() throws IOException {
        File wFile=new File("/Users/caohao/firstFitMigLambda.txt");
        FileWriter fw=new FileWriter(wFile,false);
        BufferedWriter bw=new BufferedWriter(fw);
        PrintWriter pw=new PrintWriter(bw);

        for (int i = 0; i < num; i++) {
            double index=oneAfterPoint(start+i*0.1);
            List<Double> firstFitMigLambda=new ArrayList<>();
            for (int j = 1; j < 101; j++) {
                File file=new File("/Users/caohao/vm1000pm100lambda"+index+"/firstFitMig"+j+"-vm1000pm100lambda"+index+".txt");
                double[] doubles = readLastLine(file);
                firstFitMigLambda.add(doubles[1]);
            }
            for(Double num:firstFitMigLambda){
                System.out.print(num+", ");
            }
            System.out.println();
            Double collect = firstFitMigLambda.stream().collect(Collectors.averagingDouble(Double::doubleValue));
            System.out.println(collect);
            pw.printf("%10.6f",collect);
            pw.printf("\n");
        }

        pw.close();
        bw.close();
        fw.close();
    }

    public static void greedyLambda() throws IOException {
        File wFile=new File("/Users/caohao/greedyLambda.txt");
        FileWriter fw=new FileWriter(wFile,false);
        BufferedWriter bw=new BufferedWriter(fw);
        PrintWriter pw=new PrintWriter(bw);

        for (int i = 0; i < num; i++) {
            double index=oneAfterPoint(start+i*0.1);
            List<Double> greedyLambda=new ArrayList<>();
            for (int j = 1; j < 101; j++) {
                File file=new File("/Users/caohao/vm1000pm100lambda"+index+"/greedy"+j+"-vm1000pm100lambda"+index+".txt");
                double[] doubles = readLastLine(file);
                greedyLambda.add(doubles[1]);
            }
            for(Double num:greedyLambda){
                System.out.print(num+", ");
            }
            System.out.println();
            Double collect = greedyLambda.stream().collect(Collectors.averagingDouble(Double::doubleValue));
            System.out.println(collect);
            pw.printf("%10.6f",collect);
            pw.printf("\n");
        }

        pw.close();
        bw.close();
        fw.close();
    }


    public static void uniformLambda() throws IOException {
        File wFile=new File("/Users/caohao/uniformLambda.txt");
        FileWriter fw=new FileWriter(wFile,false);
        BufferedWriter bw=new BufferedWriter(fw);
        PrintWriter pw=new PrintWriter(bw);

        for (int i = 0; i < num; i++) {
            double index=oneAfterPoint(start+i*0.1);
            List<Double> uniformLambda=new ArrayList<>();
            for (int j = 1; j < 101; j++) {
                File file=new File("/Users/caohao/vm1000pm100lambda"+index+"/uniform"+j+"-vm1000pm100lambda"+index+".txt");
                double[] doubles = readLastLine(file);
                uniformLambda.add(doubles[1]);
            }
            for(Double num:uniformLambda){
                System.out.print(num+", ");
            }
            System.out.println();
            Double collect = uniformLambda.stream().collect(Collectors.averagingDouble(Double::doubleValue));
            System.out.println(collect);
            pw.printf("%10.6f",collect);
            pw.printf("\n");
        }

        pw.close();
        bw.close();
        fw.close();
    }





    public static double[] readLastLine(File file) throws IOException {
        // 使用RandomAccessFile , 从后找最后一行数据
        RandomAccessFile raf = new RandomAccessFile(file, "r");
        long len = raf.length();
        String lastLine = "";
        if (len != 0L) {
            long pos = len - 1;
            while (pos > 0) {
                pos--;
                raf.seek(pos);
                if (raf.readByte() == '\n') {
                    lastLine = raf.readLine();
                    break;
                }
            }
        }
        raf.close();


        double[] doubles = getDoubles(lastLine);
//        Double time=doubles[0];
//        Double energy=doubles[1];
//        System.out.println(time);
//        System.out.println(energy);
        return doubles;
    }


    public static double[] getDoubles(String str){

        double[] doubles=new double[2];
        String regex =  "\\d*[.]\\d*";
        Pattern p = Pattern.compile(regex);

        Matcher m = p.matcher(str);

        int i=0;
        while (m.find()) {
            if (!"".equals(m.group())&&i<2){
                doubles[i]= Double.parseDouble(m.group());
                i++;
            }
        }
        return doubles;
    }


    public static double oneAfterPoint(double d){

        String strD = String.valueOf(d*10);
        String[] strArr = strD.split("\\.");

        return Double.parseDouble(strArr[0])/10;
    }


}
