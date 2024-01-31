public class 1a{
    public static void main(String[]args){
        ArrayList<Integer> puzzle1=new ArrayList<>();
        for(int i=1;i<65;i++){
            puzzle1.add(i);
        }
        ArrayList<Integer> puzzle2=puzzle1;
        ArrayList<Integer> puzzle3=puzzle1;
        ArrayList<Integer> puzzle4=puzzle1;
        ArrayList<Integer> hashsum=new ArrayList<>();
        for(int i=0;i<257;i++){
            hashsum.add(0);
        }
        int total=0;
        for(int i:puzzle1){
            for(int j:puzzle2){
                for(int k:puzzle3){
                    for(int l:puzzle4){
                        total=i+j+k+l;
                        int a=hashsum.get(total-1);
                        hashsum.set(total-1,a+1);
                    }
                }
            }
        }
        for(int i=0;i<hashsum.size();i++){
            System.out.println(hashsum.get(i));
        }
    }
}