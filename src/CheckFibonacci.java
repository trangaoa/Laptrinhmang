import java.util.Arrays;
import java.util.Random;

public class CheckFibonacci {
    int[] myArr = new int[1];

    public CheckFibonacci() {
        RandomArr();
    }

    void RandomArr() {
        Random generator = new Random();
        int value1 = generator.nextInt(16000) + 10;
        // int value1 = 16000;
        myArr = new int[value1];
        for (int i = 0; i < value1; i++) {
            int tg = generator.nextInt();
            myArr[i] = tg < 0 ? -tg : tg;
        }
    }

    public static void main(String[] args) {
        CheckFibonacci checkFibonacci = new CheckFibonacci();
        System.out.println(Arrays.toString(checkFibonacci.getMyArr()));
        int total = checkFibonacci.count();
        System.out.println("======================================");
        System.out.println(total);
    }

    public int[] getMyArr() {
        return myArr;
    }

    public void setMyArr(int[] myArr) {
        this.myArr = myArr;
    }

    static void printArray(int arr[]) {
        int n = arr.length;
        for (int i = 0; i < n; i++) {
            System.out.print(arr[i] + " ");
        }
        System.out.println();
    }
    
    public int count() {
        return countFibonacci(myArr);
    }

    int countFibonacci(int arr[]) {
        int n = arr.length;
        int amount = 0;

        for (int i = 0; i < n; i++) {
            if (isFibonacci(arr[i])){
                amount++;
            }
        }

        return amount;
    }

    static  boolean isPerfectSquare(int x)
    {
        int s = (int) Math.sqrt(x);
        return (s*s == x);
    }

    static boolean isFibonacci(int n)
    {
        if (n <= 0) return false;

        return isPerfectSquare(5*n*n + 4) ||
                isPerfectSquare(5*n*n - 4);
    }
}
