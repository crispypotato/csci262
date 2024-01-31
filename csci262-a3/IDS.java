import java.io.File;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class IDS {
    static Scanner sc;
    public static ArrayList<String[]> eventsArr = new ArrayList<>();
    public static ArrayList<String[]> statsArr = new ArrayList<>();
    public static final DecimalFormat df = new DecimalFormat("0.00");
    public static ArrayList<Integer> weightArr = new ArrayList<>();
    public static ArrayList<Double> meanArr = new ArrayList<>();
    public static ArrayList<Double> stdevArr = new ArrayList<>();
    public static void main(String[] args) {
        try{
            String eventsFile = "";
            String statsFile = "";
            int days = 0;
            if (args.length == 3) { // taking in command-line arguments
                eventsFile = args[0];
                statsFile = args[1];
                days = Integer.parseInt(args[2]);
            }
            //Initial input
            try {
                System.out.println("Reading events file...");
                File events = new File(eventsFile);
                sc = new Scanner(events);
                String s = "";
                while (sc.hasNext()) {
                    s = sc.nextLine();
                    if (s.contains(":")) {
                        String[] split = s.split(":");
                        for (int i = 0; i < split.length; i++) {
                            if ((split[i].trim()).equals("")) {
                                split[i] = "99999";
                            }
                        }
                        eventsArr.add(split);
                    }
                }
                System.out.println("Done!");
                System.out.println("Reading stats file...");
                File stats = new File(statsFile);
                sc = new Scanner(stats);
                while (sc.hasNext()) {
                    s = sc.nextLine();
                    if (s.contains(":")) {
                        String[] split = s.split(":");
                        for (int i = 0; i < split.length; i++) {
                            if ((split[i].trim()).equals("")) {
                                split[i] = "99999";
                            }
                        }
                        statsArr.add(split);
                    }
                }
                System.out.println("Done!");
            } catch (Exception e) {
                e.printStackTrace();
            }

            //detect potential inconsistencies
            if (eventsArr.size() != statsArr.size()) {
                throw new Exception("Number of recorded events and statistics do not match.");
            }
            for(int i=0;i<eventsArr.size();i++){
                if(!eventsArr.get(i)[0].equals(statsArr.get(i)[0])){
                    throw new Exception("Listed events in event and stats file do not match.");
                }
            }
            // activity simulation engine & logs
            System.out.println("Generating activity logs...");
            ArrayList<ArrayList<String>> log = activityEngine("Activity.txt", days);
            System.out.println("Done!");

            // analysis engine
            System.out.println("Generating analysis file...");
            analysisEngine("Analysis.txt", log);
            System.out.println("Done!");

            // alert engine
            String newStats = "";
            while (!newStats.equals("q")) {
                int newDays = 0;
                Scanner input = new Scanner(System.in);
                System.out.println("Enter another statistics file name (or enter q to quit): ");
                newStats = input.nextLine();
                if (newStats.equals("q")) {
                    System.out.println("Exiting application.");
                    break;
                }
                System.out.println("Enter new number of days: ");
                newDays = input.nextInt();
                File newStatsFile = new File(newStats);
                statsArr.clear();
                try {
                    sc = new Scanner(newStatsFile);
                    String s = "";
                    while (sc.hasNext()) {
                        s = sc.nextLine();
                        if (s.contains(":")) {
                            String[] split = s.split(":");
                            for (int i = 0; i < split.length; i++) {
                                if ((split[i].trim()).equals("")) {
                                    split[i] = "99999";
                                }
                            }
                            statsArr.add(split);
                        }
                    }
                    log = activityEngine("Activity2.txt", newDays);
                    analysisEngine("Analysis2.txt", log);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                // calculate anomaly
                ArrayList<Double> anomaly = new ArrayList<>();
                ArrayList<Double> anomalyPerDay = new ArrayList<>();
                int threshold = 0;
                for (String[] a : eventsArr) {
                    threshold += Integer.parseInt(a[4]);
                    weightArr.add(Integer.parseInt(a[4]));
                }
                threshold *= 2;
                for (ArrayList<String> a : log) {
                    double totalN = 0;
                    for (int i = 0; i < a.size(); i++) {
                        double n = (Math.abs(Double.parseDouble(a.get(i)) - meanArr.get(i))) / stdevArr.get(i) * weightArr.get(i);
                        anomaly.add(n);
                        totalN += n;
                    }
                    anomalyPerDay.add(totalN);
                }
                try {
                    FileWriter writer = new FileWriter("Anomaly.txt");
                    writer.write("Threshold = " + threshold + "\n");
                    writer.write("Daily anomaly values: \n");
                    for (int i = 0; i < anomalyPerDay.size(); i++) {
                        if (anomalyPerDay.get(i) > threshold) {
                            System.out.println("Day " + (i + 1) + " exceeded threshold");
                        } else {
                            System.out.println("Day " + (i + 1) + " recorded normal flow");
                        }
                        writer.write("Day " + (i + 1) + ": " + anomalyPerDay.get(i) + "\n");
                    }
                    writer.close();
                    System.out.println("Successfully generated anomaly report.");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<ArrayList<String>> activityEngine(String activityFile, int days){
        int max = 0;
        int min = 0;
        double mean = 0;
        double stdev = 0;
        double n = 0;
        Random r = new Random();
        ArrayList<ArrayList<String>> log = new ArrayList<>(); //for analysis portion
        try{
            FileWriter writer = new FileWriter(activityFile);
            for(int i=0; i<days; i++){
                writer.write("Day "+ (i+1) +"\n");
                ArrayList<String> tmp = new ArrayList<>();
                for(int j=0; j<eventsArr.size(); j++){
                    min = Integer.parseInt(eventsArr.get(j)[2]);
                    max = Integer.parseInt(eventsArr.get(j)[3]);
                    mean = Double.parseDouble(statsArr.get(j)[1]);
                    stdev = Double.parseDouble(statsArr.get(j)[2]);
                    n = r.nextGaussian() * stdev + mean; //since gaussian generates a decimal based on mean=0 and stdev=1,
                    // it can be manipulated to generate random numbers with a specified mean and stdev
                    // this also forms the z-score
                    while (n<min || n>max){
                        n = r.nextGaussian() * stdev + mean;
                    }
                    if(eventsArr.get(j)[1].equals("C")){
                        tmp.add(df.format(n));
                    }else{
                        int discrete = (int) Math.round(n);
                        tmp.add(String.valueOf(discrete));
                    }
                    n = 0;
                }
                for (int j=0; j< tmp.size(); j++){
                    writer.write(eventsArr.get(j)[0]+": "+tmp.get(j)+"\n");
                }
                writer.write("\n");
                log.add(tmp);
            }
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return log;
    }

    public static void analysisEngine(String analysisFile, ArrayList<ArrayList<String>> log){
        ArrayList<ArrayList<String>> activity = new ArrayList<>();
        int dailyTotal = 0;
        meanArr.clear();
        stdevArr.clear();
        try {
            FileWriter writer = new FileWriter(analysisFile);
            // rearrange from by day to by event
            for (int i=0;i<log.size();i++){
                activity.add(new ArrayList<>());
            }
            for(int i=0;i<log.size();i++){
                for (int j=0;j<log.get(i).size();j++){
                    activity.get(j).add(log.get(i).get(j));
                    dailyTotal+=Double.parseDouble(log.get(i).get(j));
                }
                writer.write("Total events on day "+(i+1)+": "+dailyTotal+"\n");
                dailyTotal = 0;
            }
            writer.write("\n");
            for (int i=0;i<eventsArr.size();i++) {
                writer.write("Event "+(i+1)+": "+eventsArr.get(i)[0]+"\n");
                double total = 0;
                for (String s : activity.get(i)) {
                    total += Double.parseDouble(s);
                }
                double generatedMean = total / activity.get(i).size();
                double generatedSD = 0;
                for (String s : activity.get(i)) {
                    generatedSD += Math.pow((Double.parseDouble(s) - generatedMean), 2);
                }
                generatedSD /= activity.get(i).size();
                generatedSD = Math.sqrt(generatedSD);
                writer.write("Total: "+(int)total+"\n");
                writer.write("Mean: "+df.format(generatedMean)+"\n");
                writer.write("Standard deviation: "+df.format(generatedSD)+"\n");
                writer.write("\n");
                meanArr.add(generatedMean);
                stdevArr.add(generatedSD);
            }
            writer.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}