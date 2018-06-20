package Caohao;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SimulationProcess {



    public static void main(String[] args) throws IOException {

        firstFitLambda();
        firstFitMigLambda();
        greedyLambda();
        uniformLambda();


    }




    public static void firstFitLambda() throws IOException {
        File wFile=new File("/Users/caohao/firstFitLambda.txt");
        FileWriter fw=new FileWriter(wFile,false);
        BufferedWriter bw=new BufferedWriter(fw);
        PrintWriter pw=new PrintWriter(bw);

        for (int i = 0; i < 6; i++) {
            double index=0.5+i*0.5;
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

        for (int i = 0; i < 6; i++) {
            double index=0.5+i*0.5;
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

        for (int i = 0; i < 6; i++) {
            double index=0.5+i*0.5;
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

        for (int i = 0; i < 6; i++) {
            double index=0.5+i*0.5;
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


}
