import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.lang.Object;

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
        if (a.size() != b.size())
            return false;
        for (int i = 0; i < a.size(); i++) {
            if (a.get(i) != b.get(i)) {
                return false;
            }
        }
        return true;
    }

    private final byte[] intToByteArray(int value) {
        return new byte[] { (byte) (value >>> 24), (byte) (value >>> 16), (byte) (value >>> 8), (byte) value };
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

    private String createFlag() {
        int leftLimit = 48; // numeral '0'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 50; // limit
        Random random = new Random();

        String flag = random.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();

        return flag;
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
        addPayload(sendPayload, len * 4);
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

    public void PKT_HELLO(Socket socket) throws IOException {
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
            System.out.println("Type: " + type);
            System.out.println("Len: " + len);
            System.out.println("MSV: " + msv);
        } else {
            write(socket, 3, 0);
        }
    }

    public void PKT_CALC(Socket socket, ArrayList<Integer> data) throws IOException {
        write(socket, 1, data.size(), data);
    }

    public void PKT_RES(Socket socket, ArrayList<Integer> res) throws IOException {
        byte[] payload = new byte[100010];
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
            for (int i = 0; i < len / 4; i++) {
                byte[] tg = Arrays.copyOfRange(payload, 8 + i, 12 + i);
                payloadData.add(fromByteArray(tg));
            }
            if (check(payloadData, res)) {
                String flag = createFlag();
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

        try {
            sever = new ServerSocket(Server.PORT);

            System.out.println("Waiting for new connection....");

            socket = sever.accept();

            PKT_HELLO(socket);

            ArrayList<Integer> data = new ArrayList<>();
            data.add(1);
            data.add(1);
            PKT_CALC(socket, data);

            data.clear();
            data.add(2);

            PKT_RES(socket, data);

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.process();
    }
}
