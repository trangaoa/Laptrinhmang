import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;

public class Client {
    static final String severHost = "localhost";
    public static void main(String[] args) {
        Socket socketOfClient;
        DataInputStream ip;
        DataOutputStream op;
        try {
            socketOfClient = new Socket(severHost, 9999);
            ip = new DataInputStream(socketOfClient.getInputStream());
            op = new DataOutputStream(socketOfClient.getOutputStream());

            op.writeInt(0);
            op.writeInt(8);
            op.writeUTF("20020325");
            op.flush();

            int type = ip.readInt();
            int len = ip.readInt();
            ArrayList<Integer> arr = new ArrayList<>();
            int res = 0;
            for(int i = 0;i<len;i++){
                arr.add(ip.readInt());
                res += arr.get(i);
            }
            System.out.println("Type "+type);
            System.out.println("len "+len);
            System.out.println(Arrays.toString(Arrays.stream(arr.toArray()).toArray()));

            op.writeInt(2);
            op.writeInt(1);
            op.writeInt(res);
            op.flush();

            type = ip.readInt();
            if(type == 3) {
                System.out.println("end");
            }
            if(type == 4){
                len = ip.readInt();
                String flag = ip.readUTF();
                System.out.println("Flag: "+flag);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
