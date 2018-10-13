package ambulance.botty;

import com.google.gson.Gson;

import java.io.IOException;

public class AmbulanceClient {

    static class InitialMsg {
        String name;

        public InitialMsg(String name) {
            this.name = name;
        }
    }

    static class BufferMsg {
        int buffer_size;

        public BufferMsg(int buffer_size) {
            this.buffer_size = buffer_size;
        }
    }

    static class SolutionMsg {

    }

    static SocketClient socketClient;

    public static void main(String[] args) throws IOException {

        if (args.length != 2) {
            System.out.println("Usage: java AmbulanceClient <host> <port>");
            System.exit(0);
        }
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        if (socketClient != null) socketClient.close_socket();

        Gson gson = new Gson();

        // connect to server
        socketClient = new SocketClient(host, port);

        // send initial message
        socketClient.send_json(new InitialMsg("Botty McBotFace"));

        // receive buffer message
        String s = socketClient.receive_data();
        System.out.println(s);

        // receive problem message
//        String s = socketClient.receive_data(b.buffer_size);
//        System.out.println(s);


        // TODO: insert algo here


        // send buffer size
        socketClient.send_json(new BufferMsg(8192));

        // send solution
        socketClient.send_json(new SolutionMsg());

        socketClient.close_socket();
    }

}
