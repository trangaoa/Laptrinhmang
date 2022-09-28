import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Random;

public class Server {
    private final static int PORT = 9999;

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

    private boolean check(ArrayList<Integer> a, ArrayList<Integer> b) {
        // System.out.println("a: " + a.toString());
        // System.out.println("b: " + b.toString());
        return a.equals(b);
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

    private String createFlag(String msv) {
        int leftLimit = 48; // numeral '0'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 50; // limit
        Random random = new Random();
        String hashMsv = Base64.getEncoder().encodeToString(msv.getBytes());
        String flag = random.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
        return flag + hashMsv;
    }

    private void addPayload(ArrayList<Byte> sendPayload, String value) {
        sendPayload.addAll(Arrays.asList(toObjects(value.getBytes(StandardCharsets.UTF_8))));
    }

    public void write(Socket socket, int type, int len) throws IOException {
        write(socket, type, len, "");
    }

    public void write(Socket socket, int type, int len, ArrayList<Integer> data) throws IOException {
        // System.out.println(type);
        ArrayList<Byte> sendPayload = new ArrayList<>();
        addPayload(sendPayload, type);
        addPayload(sendPayload, len);
        for (int i = 0; i < len; i++) {
            addPayload(sendPayload, data.get(i));
        }
        write(socket.getOutputStream(), sendPayload);
    }

    public void write(Socket socket, int type, int len, String data) throws IOException {
        // System.out.println(type);
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

    public String PKT_HELLO(Socket socket) throws IOException {
        byte[] payload = new byte[30];
        int type = -1, len = 0;
        String msv = "";
        byte[] typeBytes;
        byte[] lenBytes;
        byte[] msvBytes;

        socket.getInputStream().read(payload);
        typeBytes = Arrays.copyOfRange(payload, 0, 4);
        type = fromByteArray(typeBytes);

        if (type == 0) {
            lenBytes = Arrays.copyOfRange(payload, 4, 8);
            len = fromByteArray(lenBytes);
            msvBytes = Arrays.copyOfRange(payload, 8, 8 + len);
            msv = new String(msvBytes, StandardCharsets.UTF_8);
            System.out.println("MSV: " + msv);
        } else {
            write(socket, 3, 0);
        }
        return msv;
    }

    public void PKT_CALC(Socket socket, ArrayList<Integer> data) throws IOException {
        // System.out.println(data.size());
        write(socket, 1, data.size(), data);
    }

    public void PKT_RES(Socket socket, String msv, ArrayList<Integer> res) throws IOException {
        byte[] payload = new byte[100050];
        byte[] typeByte = new byte[4];
        byte[] lenByte = new byte[4];

        socket.getInputStream().read(payload);
        typeByte = Arrays.copyOfRange(payload, 0, 4);
        lenByte = Arrays.copyOfRange(payload, 4, 8);
        int type = fromByteArray(typeByte);
        if (type != 2) {
            write(socket, 3, 0);
            socket.close();

        } else {
            int len = fromByteArray(lenByte);
            ArrayList<Integer> payloadData = new ArrayList<>();
            for (int i = 8; i < len * 4 + 8; i += 4) {
                byte[] tg = Arrays.copyOfRange(payload, i, i + 4);
                payloadData.add(fromByteArray(tg));
            }

            boolean bool = check(payloadData, res);
            // System.out.println(bool);
            if (bool) {
                String flag = createFlag(msv);
                write(socket, 4, flag.length(), flag);
                System.out.println(flag);
            } else {
                write(socket, 3, 0);
            }
        }

    }

    public void process() {
        ServerSocket sever = null;
        Socket socket = null;
        QuickSort quickSort = null;
        try {
            sever = new ServerSocket(Server.PORT);

            System.out.println("Waiting for new connection....");

            socket = sever.accept();

            String seed = PKT_HELLO(socket);

            ArrayList<Integer> data = new ArrayList<>();
            ArrayList<Integer> res = new ArrayList<>();
            quickSort = new QuickSort();

            for (int i : quickSort.getMyArr())
                data.add(i);
            quickSort.sort();
            for (int i : quickSort.getMyArr())
                res.add(i);

            seed += data.size();

            PKT_CALC(socket, data);

            PKT_RES(socket, seed, res);

        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void processFibonacci() {
        ServerSocket sever = null;
        Socket socket = null;
        CheckFibonacci checkFibonacci = null;
        try {
            sever = new ServerSocket(Server.PORT);

            System.out.println("Waiting for new connection....");

            socket = sever.accept();

            String seed = PKT_HELLO(socket);

            ArrayList<Integer> data = new ArrayList<>();
            ArrayList<Integer> res = new ArrayList<>();
            checkFibonacci = new CheckFibonacci();

            for (int i : checkFibonacci.getMyArr())
                data.add(i);
                checkFibonacci.count();
            for (int i : checkFibonacci.getMyArr())
                res.add(i);

            seed += data.size();

            PKT_CALC(socket, data);

            PKT_RES(socket, seed, res);

        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.process();
    }
}
