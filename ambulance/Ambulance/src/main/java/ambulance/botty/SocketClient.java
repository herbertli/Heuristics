/**
 * A socket based client for the ``hps.servers.SocketServer`` class
 */

package ambulance.botty;

import java.io.*;
import java.net.*;

public class SocketClient {
    /**
     * A client class for ``hps.servers.SocketServer``
     */
    public String host;
    public int port;
    public Socket socket;
    BufferedWriter bufferedWriter;
    BufferedReader bufferedReader;

    public SocketClient(String host, int port) throws IOException {
        /**
         *   @param host: The hostname of the server
         *   @param port: The port of the server
         */
        this.host = host;
        this.port = port;
        InetAddress inetAddress = InetAddress.getByName(host);
        SocketAddress socketAddress = new InetSocketAddress(inetAddress, port);
        socket = new Socket();
        socket.connect(socketAddress);
        this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    public void send_data(String data) throws IOException {
        /**
         * Send data to the server
         *
         * @param data: The data to send to the server.
         */
        bufferedWriter.write(data);
        bufferedWriter.flush();
    }

    public String receive_data() throws IOException {
        return receive_data(4096);
    }

    public String receive_data(int size) throws IOException {
        /**
         * Receive data from the server
         *
         * @return The data received as a String from server.
         */
        char[] buf = new char[size];
        bufferedReader.read(buf, 0, size);
        return String.valueOf(buf);
    }

    public void close_socket() throws IOException {
        /**
         * Close the connection
         */
        socket.close();
    }
}
