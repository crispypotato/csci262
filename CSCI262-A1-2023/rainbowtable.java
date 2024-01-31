import java.io.*;
import java.security.*;
import java.math.*;
import java.util.*;
import java.util.stream.Collectors;

class q4{
    static Scanner sc;
    static ArrayList<String> passwords = new ArrayList<>();
    static ArrayList<String> usedPW = new ArrayList<>();
    static int len = 0;
    static HashMap<String,String> rainbowTable = new HashMap<String,String>();
    public static void main(String args[]) throws NoSuchAlgorithmException {
        // first step
        try{
            File f = new File("Passwords.txt");
            sc = new Scanner(f);
            while (sc.hasNext()){
                passwords.add(sc.nextLine());
            }
            len = passwords.size();
        }catch (Exception e){
            e.printStackTrace();
        }
        sc.close();
        System.out.println("Number of words read in: " + len);
        for(int i = 0; i<len; i++){
            if(usedPW.contains(passwords.get(i)) == false){
                int reductionValue = i;
                String hash = "";
                for(int j = 0; j<5; j++){ // reduce 5 times
                    // retrieve the password corresponding to reduction value, then hash the password
                    hash = md5(passwords.get(reductionValue));
                    // reduce the hash, then store new reduction value
                    reductionValue = reduce(hash);
                    // mark current selected password as used (if not used before)
                    if(usedPW.contains(passwords.get(reductionValue)) == false){
                        usedPW.add(passwords.get(reductionValue));
                    }
                }
                rainbowTable.put(passwords.get(i),hash);
            }
        }
        rainbowTable = sort(rainbowTable);
        File rainbow = new File("rainbow.txt");
        try{
            rainbow.createNewFile();
            System.out.println("Rainbow Table file created.");
            FileWriter writer = new FileWriter("rainbow.txt");
            int i = 1;
            for(Map.Entry<String,String> e:rainbowTable.entrySet()){
                writer.write(i + " " + e.getKey() + " " + e.getValue() + "\n");
                i++;
            }
            writer.close();
            System.out.println("Number of lines in rainbow table: " + rainbowTable.size());
        }catch (IOException e) {
            e.printStackTrace();
        }

        // second step
        Scanner input = new Scanner(System.in);
        String hashInput = "";
        String rainbowPW = "";
        String pwHash = "";
        int redval = 0;
        List<String> inputHash = new ArrayList<>(); // possible hashes (out of 5) of hashInput that are in rainbow table
        List<String> collisionPW = new ArrayList<>(); // passwords in rainbow table that has same hash
        while(!hashInput.equals("q")){
            System.out.println("Enter a hash value: (or enter q to quit)");
            hashInput = input.nextLine();
            if(rainbowTable.containsValue(hashInput)){ // table has that hash
                for(Map.Entry<String,String> h:rainbowTable.entrySet()){
                    if(hashInput.equals(h.getValue())){
                        rainbowPW = h.getKey();
                    }
                }
                if(hashInput==md5(rainbowPW)){
                    System.out.println("The corresponding password is "+rainbowPW);
                }else{
                    for(int i=0;i<4;i++){
                        pwHash = md5(rainbowPW);
                        redval = reduce(pwHash);
                        rainbowPW = passwords.get(redval);
                    }
                    System.out.println("The corresponding password is "+rainbowPW);
                }
            }else{ //table does not contain that hash
                int count = 0; //counter for no .of loops
                if(!hashInput.equals("q")){ // to avoid exception of when pwHash = hashInput = "q"
                    pwHash = hashInput;
                    while (count<5) {
                        redval = reduce(pwHash);
                        rainbowPW = passwords.get(redval);
                        pwHash = md5(rainbowPW);
                        if(rainbowTable.containsValue(pwHash)){
                            inputHash.add(pwHash);
                        }
                        count++;
                    }
                    for(int i=0;i<inputHash.size();i++){
                        for(Map.Entry<String,String> h:rainbowTable.entrySet()){
                            if(inputHash.get(i).equals(h.getValue())){
                                rainbowPW = h.getKey();
                                collisionPW.add(rainbowPW);
                            }
                        }
                        for(int j=0;j<collisionPW.size();j++){
                            rainbowPW = collisionPW.get(j);
                            if (hashInput.equals(md5(rainbowPW))) {
                                System.out.println("The corresponding password is " + rainbowPW);
                                break;
                            } else {
                                count = 0;
                                String nextRainbowPW = "";
                                while ((!pwHash.equals(hashInput)) && count < 4) {
                                    if(count!=0){
                                        rainbowPW = nextRainbowPW;
                                    }
                                    pwHash = md5(rainbowPW);
                                    redval = reduce(pwHash);
                                    nextRainbowPW = passwords.get(redval);
                                    count++;
                                }
                                if (pwHash.equals(hashInput)) {
                                    System.out.println("The corresponding password is " + rainbowPW);
                                }
                            }
                        }
                        collisionPW.clear();
                        if (pwHash.equals(hashInput)){
                            break;
                        }
                    }
                }
            }
            if((!pwHash.equals(hashInput)) && (!hashInput.equals("q"))){
                System.out.println("No match found.");
            }
        }
        System.out.println("Exiting application.");
    }
    public static String md5(String s) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] messageDigest = md.digest(s.getBytes());
        BigInteger b = new BigInteger(1, messageDigest);
        String hashtext = b.toString(16);
        while (hashtext.length() < 32) {
            hashtext = "0" + hashtext;
        }
        return hashtext;
    }
    public static int reduce(String md){
        // convert to long
        BigInteger b = new BigInteger(md,16);
        BigInteger l = BigInteger.valueOf(len);
        int redval = (b.mod(l)).intValue();
        return redval;
    }
    public static HashMap<String,String> sort(HashMap<String,String> h){
        HashMap<String,String> sortedMap = h.entrySet().stream().sorted(Map.Entry.comparingByValue()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
        return sortedMap;
    }
}