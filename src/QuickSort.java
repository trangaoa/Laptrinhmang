import java.util.Arrays;
import java.util.Random;

public class QuickSort {

    int[] myArr = new int[1];

    public QuickSort() {
        RandomArr();
    }

    void RandomArr() {
        Random generator = new Random();
        int value1 = generator.nextInt(100) + 10;
        myArr = new int[value1];
        for (int i = 0; i < value1; i++) {
            myArr[i] = generator.nextInt(100000);
        }
    }

    public static void main(String[] args) {
        QuickSort quickSort = new QuickSort();
        System.out.println(Arrays.toString(quickSort.getMyArr()));
        quickSort.sort();
        System.out.println("======================================");
        System.out.println(Arrays.toString(quickSort.getMyArr()));

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

    private int partition(int arr[], int low, int high) {
        int pivot = arr[high];
        int i = low - 1;
        for (int j = low; j < high; j++) {
            if (arr[j] < pivot) {
                i++;
                int temp = arr[i];
                arr[i] = arr[j];
                arr[j] = temp;
            }
        }
        int temp = arr[i + 1];
        arr[i + 1] = arr[high];
        arr[high] = temp;

        return i + 1;
    }

    void sort(int arr[], int low, int high) {
        if (low < high) {

            int pi = partition(arr, low, high);
            sort(arr, low, pi - 1);
            sort(arr, pi + 1, high);
        }
    }

    public void sort() {
        sort(myArr, 0, myArr.length - 1);
    }
}