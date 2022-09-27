import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class Client {
    static final String SERVERHOST = "localhost";
    static final int PORT = 9999;

    private byte[] reverse(byte[] bytes) {
        int i;
        byte t;
        int n = bytes.length;
        for (i = 0; i < n / 2; i++) {
            t = bytes[i];
            bytes[i] = bytes[n - i - 1];
            bytes[n - i - 1] = t;
        }
        return bytes;
    }

    private int fromByteArray(byte[] bytes) {
        reverse(bytes);
        return bytes[0] << 24 | (bytes[1] & 0xFF) << 16 | (bytes[2] & 0xFF) << 8 | (bytes[3] & 0xFF);
    }

    private final byte[] intToByteArray(int value) {
        byte[] res;
        res = new byte[] { (byte) (value >>> 24), (byte) (value >>> 16), (byte) (value >>> 8), (byte) value };
        // reverse(res);
        return res;
    }

    private Byte[] toObjects(byte[] bytesPrim) {
        Byte[] bytes = new Byte[bytesPrim.length];
        Arrays.setAll(bytes, n -> bytesPrim[n]);
        return bytes;
    }

    private byte[] toPrimitives(Object[] objects) {
        byte[] bytes = new byte[objects.length];
        for (int i = 0; i < objects.length; i++)
            bytes[i] = (Byte) objects[i];
        return bytes;
    }

    private void addPayload(ArrayList<Byte> sendPayload, int value) {
        sendPayload.addAll(Arrays.asList(toObjects(reverse(intToByteArray(value)))));
    }

    private void addPayload(ArrayList<Byte> sendPayload, String value) {
        sendPayload.addAll(Arrays.asList(toObjects(value.getBytes(StandardCharsets.UTF_8))));
    }

    public void write(Socket socket, int type, int len) throws IOException {
        write(socket, type, len, "");
    }

    public void write(Socket socket, int type, int len, ArrayList<Integer> data) throws IOException {
        ArrayList<Byte> sendPayload = new ArrayList<>();
        addPayload(sendPayload, type);
        addPayload(sendPayload, len);
        for (int i = 0; i < len; i++) {
            addPayload(sendPayload, data.get(i));
        }
        write(socket.getOutputStream(), sendPayload);
    }

    public void write(Socket socket, int type, int len, String data) throws IOException {
        ArrayList<Byte> sendPayload = new ArrayList<>();
        addPayload(sendPayload, type);
        addPayload(sendPayload, len);
        if (len > 0) {
            addPayload(sendPayload, data);
        }
        write(socket.getOutputStream(), sendPayload);
    }

    private void write(OutputStream outputStream, ArrayList<Byte> sendPayload) throws IOException {
        outputStream.write(toPrimitives(sendPayload.toArray()));
        outputStream.flush();
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

    public static void main(String[] args) {
        Client client = new Client();
        Socket socketOfClient;
        try {
            socketOfClient = new Socket(SERVERHOST, PORT);
            String msv = "20020005";
            client.write(socketOfClient, 0, msv.length(), msv);
            byte[] payload = new byte[100100];
            socketOfClient.getInputStream().read(payload);
            byte[] typeByte = new byte[4];
            byte[] lenByte = new byte[4];
            int type, len;
            ArrayList<Integer> dataPayload = new ArrayList<>();
            // System.out.println("Payload: " + Arrays.toString(payload));
            typeByte = Arrays.copyOfRange(payload, 0, 4);
            lenByte = Arrays.copyOfRange(payload, 4, 8);
            type = client.fromByteArray(typeByte);
            len = client.fromByteArray(lenByte);
            System.out.println(len);
            for (int i = 0; i < 8; i++) {
                System.out.print(payload[i] + ", ");
            }
            System.out.println();
            // System.out.println("Payload: " + payload);
            for (int i = 8; i < len * 4 + 8; i += 4) {
                byte[] tg = Arrays.copyOfRange(payload, i, i + 4);
                dataPayload.add(client.fromByteArray(tg));
            }
            System.out.println(dataPayload.size());
            Collections.sort(dataPayload);
            // dataPayload.add(3);
            // System.out.println(dataPayload.toString());
            // ArrayList<Integer> res = new ArrayList<>();
            // res.add(dataPayload.get(0) + dataPayload.get(1));
            client.write(socketOfClient, 2, dataPayload.size(), dataPayload);

            payload = new byte[100];
            socketOfClient.getInputStream().read(payload);
            type = client.fromByteArray(Arrays.copyOfRange(payload, 0, 4));
            if (type == 3) {
                System.out.println("err");
                return;
            }
            len = client.fromByteArray(Arrays.copyOfRange(payload, 4, 8));
            String flag = new String(Arrays.copyOfRange(payload, 8, 8 + len),
                    StandardCharsets.UTF_8);
            System.out.println("Flag: " + flag);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
