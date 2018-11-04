package nyu.hps.botty.dancing;

import java.io.IOException;

public class DancingClient {

    private String name;
    private boolean isSpoiler;
    private Spoiler spoiler = new HerbertSpoiler();
    private Choreographer choreographer = new HerbertChoreo();
    private SocketClient socketClient;

    private DancingClient(String name, String host, int port, boolean isSpoiler) throws IOException {
        this.name = name;
        this.isSpoiler = isSpoiler;
        socketClient = new SocketClient(host, port);
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 3) {
            System.out.println("Usage: java DancingClient <host> <port> <s|c>");
        }
        DancingClient dancingClient = new DancingClient("botty",
                args[0],
                Integer.parseInt(args[1]),
                args[2].equals("s"));

        if (dancingClient.isSpoiler) {
            Spoiler s = dancingClient.spoiler;

            System.out.println("Sending name: " + dancingClient.name);
            dancingClient.socketClient.send_data(dancingClient.name + "&");

            System.out.println("Waiting for game info...");
            s.receiveGameInfo(dancingClient.socketClient.receive_data('&').trim());
            System.out.println("Received game info.");

            System.out.println("Waiting for dance data...");
            s.receiveInput(dancingClient.socketClient.receive_data('&').trim());
            System.out.println("Received dance data.");

            System.out.println("Placing stars...");
            dancingClient.socketClient.send_data(s.getMoveString(s.getStars()));
            System.out.println("Sent stars.");

        } else {
            Choreographer c = dancingClient.choreographer;

            System.out.println("Sending name: " + dancingClient.name);
            dancingClient.socketClient.send_data(dancingClient.name + "&");

            System.out.println("Waiting for game info...");
            c.receiveGameInfo(dancingClient.socketClient.receive_data('&').trim());
            System.out.println("Received game info.");

            System.out.println("Waiting for dance data...");
            c.receiveInput(dancingClient.socketClient.receive_data('&').trim());
            System.out.println("Received dance data.");

            System.out.println("Waiting for stars...");
            c.receiveStars(dancingClient.socketClient.receive_data('&').trim());
            System.out.println("Received stars.");

            System.out.println("Sending moves...");
            dancingClient.socketClient.send_data(c.getMoveString(c.getPaths()));
            dancingClient.socketClient.send_data("DONE&");
            System.out.println("Sent moves.");

            System.out.println("Sending lines...");
            dancingClient.socketClient.send_data(c.getLineString(c.getLines()));
            System.out.println("Sent lines.");

        }
        dancingClient.socketClient.close_socket();
    }
}
