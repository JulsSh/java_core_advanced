package java.collections_base;
import java.util.*;

public class Main {
    public static void main(String[] args) {

        System.out.println("Enter you digits: ");
        Scanner sc = new Scanner(System.in);
        List<Integer> numbers = new ArrayList<>();
        
        while (true) {
            String line = sc.nextLine().trim();
            if (line.isEmpty()) break;            // stop on empty line
            numbers.add(Integer.parseInt(line));  // one number per line
        }

        // searching for a numbers from console
        Map<Integer, Integer> allNumberEntered = new HashMap<>();
        for (int n : numbers){
            if(allNumberEntered.containsKey(n)){
                allNumberEntered.put(n, allNumberEntered.get(n) +1);
            }else{
                allNumberEntered.put(n, 1);
            }
        }

        // searching for sum of unique numberes
        int sumOfUnique = 0;
        for (Map.Entry<Integer, Integer> element : allNumberEntered.entrySet()) {
            if (element.getValue() == 1) sumOfUnique += element.getKey();
           
        }
        System.out.println("Sum "+ sumOfUnique);
        StringBuilder dups = new StringBuilder();
        boolean first = true;
        for (int val : new TreeSet<>(allNumberEntered.keySet())) {
            int cnt = allNumberEntered.get(val);
            if (cnt >= 2) {
                if (!first) dups.append(", ");
                dups.append(val).append("×").append(cnt);
                first = false;
            }
        }
        System.out.println("Duplicates: " + (first ? "—" : dups.toString()));

    }
}

