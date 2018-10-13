package ambulance.botty;

import com.google.gson.Gson;

import java.io.*;
import java.net.*;


public class SocketClient {

    public String host;
    public int port;
    public Socket socket;

    public SocketClient(String host, int port) throws IOException {
        this.host = host;
        this.port = port;
        InetAddress inetAddress = InetAddress.getByName(host);
        SocketAddress socketAddress = new InetSocketAddress(inetAddress, port);
        socket = new Socket();
        socket.connect(socketAddress);
    }

    public void send_data(String data) throws IOException {
        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        bufferedWriter.write(data);
        bufferedWriter.flush();
    }

    public String receive_data() throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        while (true) {
            String data = bufferedReader.readLine();
            if (data != null) {
                return data;
            }

        }
//        throw new IOException("Socket is not ready.");
    }

    public void close_socket() throws IOException {
        socket.close();
    }
}

