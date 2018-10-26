package edu.nyu.cs.hps.evasion;

import java.io.*;
import java.net.*;

public class SocketClient {

    public String host;
    public int port;
    public Socket socket;
    BufferedWriter bufferedWriter;
    BufferedReader bufferedReader;

    /**
     *   @param host: The hostname of the server
     *   @param port: The port of the server
     */
    public SocketClient(String host, int port) throws IOException {
        this.host = host;
        this.port = port;
        InetAddress inetAddress = InetAddress.getByName(host);
        SocketAddress socketAddress = new InetSocketAddress(inetAddress, port);
        socket = new Socket();
        socket.connect(socketAddress);
        this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    /**
     * Send data to the server
     *
     * @param data: The data to send to the server.
     */
    public void send_data(String data) throws IOException {
        bufferedWriter.write(data);
        bufferedWriter.flush();
    }

    public String receive_data() throws IOException {
        return receive_data(4096);
    }

    /**
     * Receive data from the server
     *
     * @return The data received as a String from server.
     */
    public String receive_data(int size) throws IOException {
        char[] buf = new char[size];
        bufferedReader.read(buf, 0, size);
        return String.valueOf(buf);
    }

    /**
     * Close the connection
     */
    public void close_socket() throws IOException {
        socket.close();
    }
}