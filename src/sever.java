import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.lang.Object;

public class sever {

    public static byte[] reverse(byte[] bytes) {
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

    public static int fromByteArray(byte[] bytes) {
        reverse(bytes);
        return bytes[0] << 24 | (bytes[1] & 0xFF) << 16 | (bytes[2] & 0xFF) << 8 | (bytes[3] & 0xFF);
    }

    public static final byte[] intToByteArray(int value) {
        return new byte[] { (byte) (value >>> 24), (byte) (value >>> 16), (byte) (value >>> 8), (byte) value };
    }

    public static Byte[] toObjects(byte[] bytesPrim) {
        Byte[] bytes = new Byte[bytesPrim.length];
        Arrays.setAll(bytes, n -> bytesPrim[n]);
        return bytes;
    }

    public static byte[] toPrimitives(Object[] objects) {
        byte[] bytes = new byte[objects.length];
        for (int i = 0; i < objects.length; i++)
            bytes[i] = (Byte) objects[i];
        return bytes;
    }

    public static void addPayload(ArrayList<Byte> sendPayload, int value) {
        sendPayload.addAll(Arrays.asList(toObjects(reverse(intToByteArray(value)))));
    }

    public static String createFlag() {
        int leftLimit = 48; // numeral '0'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 50; // limit
        Random random = new Random();

        String generatedString = random.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();

        return generatedString;
    }

    public static void addPayload(ArrayList<Byte> sendPayload, String value) {
        sendPayload.addAll(Arrays.asList(toObjects(value.getBytes(StandardCharsets.UTF_8))));
    }

    public static void write(OutputStream outputStream, ArrayList<Byte> sendPayload) throws IOException {
        outputStream.write(toPrimitives(sendPayload.toArray()));
        outputStream.flush();
    }

    public static void xl() {
        ServerSocket sever = null;
        Socket socket = null;
        byte[] payload;
        String flag = "";
        try {
            sever = new ServerSocket(9999);
        } catch (IOException e) {
            System.out.println(e);
            System.exit(1);
        }

        try {
            int type, len = 0;
            String msv = "";
            System.out.println("Server is waiting to accept user...");
            socket = sever.accept();

            InputStream iStream = socket.getInputStream();
            OutputStream outputStream = socket.getOutputStream();

            // PKT_HELLO
            payload = new byte[30];

            iStream.read(payload);

            byte[] typeBytes = Arrays.copyOfRange(payload, 0, 4);
            byte[] lenBytes;
            byte[] msvBytes;

            type = fromByteArray(typeBytes);

            if (type == 0) {
                lenBytes = Arrays.copyOfRange(payload, 4, 8);
                len = fromByteArray(lenBytes);
                msvBytes = Arrays.copyOfRange(payload, 8, 8 + len);
                msv = new String(msvBytes, StandardCharsets.UTF_8);
            }

            System.out.println("Type: " + type);
            System.out.println("Len: " + len);
            System.out.println("MSV: " + msv);

            // PKT_CALC
            ArrayList<Byte> sendPayload = new ArrayList<>();

            addPayload(sendPayload, 1);
            addPayload(sendPayload, 8);
            addPayload(sendPayload, 8);
            addPayload(sendPayload, 8);

            write(outputStream, sendPayload);

            // PKT_RES
            payload = new byte[30];

            iStream.read(payload);

            typeBytes = Arrays.copyOfRange(payload, 0, 4);
            type = fromByteArray(typeBytes);
            sendPayload.clear();
            flag = createFlag();
            if (type != 2) {
                addPayload(sendPayload, 3);
            } else {
                addPayload(sendPayload, 4);
                addPayload(sendPayload, flag.length());
                addPayload(sendPayload, flag);
                System.out.println(flag);
            }

            write(outputStream, sendPayload);

            socket.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {

    }
}
