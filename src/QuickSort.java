import java.util.Random;

public class QuickSort {

    static void RandomArr() {
       Random generator = new Random();
        int value1 = generator.nextInt(100000)+10;
        myArr = new int[value1];
        for(int i = 0; i < value1; i++) {
            myArr[i] = 
            generator.nextInt(100000);
        }
    }

    static int[] myArr = new int[1];
    
    public static void main(String[] args) {
        RandomArr();
        int n = myArr.length;
        // printArray(myArr);
        System.out.println(myArr.length);
        QuickSort qs = new QuickSort();
        qs.sort(myArr, 0, n - 1);

        printArray(myArr);

    }
    
    public static int[] getMyArr() {
        return myArr;
    }
    public static void setMyArr(int[] myArr) {
        QuickSort.myArr = myArr;
    }

    static void printArray(int arr[]) {
        int n = arr.length;
        for (int i = 0; i < n; i++) {
            System.out.print(arr[i] + " ");
        }
        System.out.println();
        System.out.println();
        System.out.println();
    }

    public QuickSort() {
    }

    int partition (int arr[], int low, int high) {
        int pivot = arr[high];
        int i = low - 1;
        for (int j = low; j < high; j++) {
            if(arr[j] < pivot) {
                i++;
                int temp = arr[i];
                 arr[i] = arr[j];
                 arr[j] = temp;
            }
        }
        int temp = arr[i+1];
        arr[i+1] = arr[high];
        arr[high] = temp;
        
        return i + 1; 
    }

    void sort(int arr[], int low, int high) {
        if(low < high) {

            int pi = partition(arr, low, high);
            sort(arr, low, pi-1);
            sort(arr, pi+1, high);
        }
    }

}