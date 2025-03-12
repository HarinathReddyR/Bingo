package Bin;


import java.io.*;
import java.net.*;
public class MyGameServer {

    public static void main(String[] args) {
        // TODO Auto-generated method stub
        try {
            ServerSocket ss = new ServerSocket(6666);
            
            Socket s[] = new Socket[2];
            DataOutputStream out[] = new DataOutputStream[2]; 
            DataInputStream in[] = new DataInputStream[2];
            for (int i = 0; i < 2; i++) {
                s[i] = ss.accept();
                out[i] = new DataOutputStream(s[i].getOutputStream());
                in[i] = new DataInputStream(s[i].getInputStream());
            }
            boolean alive = true;
            out[0].writeUTF("Start");
            while (alive) {
                   String str = in[0].readUTF();
                out[1].writeUTF(str);
                out[1].flush();
                // MyPlayer.cross(str);
                if ("bye".equals(str)) break;
                
                str = in[1].readUTF();
                out[0].writeUTF(str);
                out[1].flush();
                // MyPlayer.cross(str);
                if ("bye".equals(str)) break;
            }
            s[0].close();
            s[1].close();
            ss.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}


