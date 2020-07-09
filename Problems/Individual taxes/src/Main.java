import java.util.*;

public class Main {
    public static void main(String[] args) {
        // write your code here
        Scanner s = new Scanner(System.in);
        int n = Integer.parseInt(s.nextLine());
        String[] company = s.nextLine().split(" ");
        String[] perc = s.nextLine().split(" ");
        double max = 0;
        int count = 0;
        for (int i = 0; i < n; i++){
            int x = Integer.parseInt(company[i]);
            int y = Integer.parseInt(perc[i]);
            double z = (1.0 * y / 100) * x;
            if (max < z) {
                max = z;
                count = i + 1;
            }
        }
        System.out.println(count);
    }
}