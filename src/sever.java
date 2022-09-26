import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Base64;

import javafx.util.Pair;

public class sever {

    public static Pair<String, Integer> PKT_CALC(String msv, Socket socket, DataInputStream ip, DataOutputStream op)
            throws Exception {
        String inputString = msv + "211";
        String flag = Base64.getEncoder().encodeToString(inputString.getBytes());
        Pair res = new Pair<>(flag, 2);
        try {
            op.writeInt(1);
            op.writeInt(2);
            op.writeInt(1);
            op.writeInt(1);
            op.flush();
        } catch (Exception e) {
            throw new Exception(e);
        }
        return res;
    }

    public static void PKT_BYE(Pair<String, Integer> pair, Socket socket, DataInputStream ip, DataOutputStream op)
            throws Exception {
        int type = ip.readInt();

        if (type == 2) {
            int len = ip.readInt();
            int res = ip.readInt();
            if (res == pair.getValue()) {
                op.writeInt(4);
                op.writeInt(pair.getKey().length());
                op.writeUTF(pair.getKey());
                System.out.println(pair.getKey());
            } else {
                op.writeInt(3);
            }
            op.flush();
            op.close();
        }
    }

    public static void main(String[] args) throws Exception {
        ServerSocket sever = null;
        Socket socket = null;
        DataInputStream ip;
        DataOutputStream op;
        try {
            sever = new ServerSocket(9999);
        } catch (IOException e) {
            System.out.println(e);
            System.exit(1);
        }

        try {
            System.out.println("Server is waiting to accept user...");
            socket = sever.accept();

            op = new DataOutputStream(socket.getOutputStream());
            ip = new DataInputStream(socket.getInputStream());

            String res = "";
            int type, len;
            type = ip.readInt();
            if (type == 0) {
                len = ip.readInt();
                res = ip.readUTF();
                System.out.println("Type " + type);
                System.out.println("Len " + len);
                System.out.println("Accept " + res);
            } else {
                op.writeInt(3);
                op.flush();
                socket.close();
            }

            Pair<String, Integer> resPair = PKT_CALC(res, socket, ip, op);

            PKT_BYE(resPair, socket, ip, op);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
