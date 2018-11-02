package nyu.hps.botty.dancing;

import java.io.IOException;

public class DancingClient {

    private String name;
    private boolean isSpoiler;
    private Spoiler spoiler;
    private Choreographer choreographer;
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
            dancingClient.socketClient.send_data(dancingClient.name + "&");
            dancingClient.spoiler.receiveInput(dancingClient.socketClient.receive_data('&'));
            dancingClient.spoiler.receiveGameInfo(dancingClient.socketClient.receive_data('&'));
            dancingClient.socketClient.send_data(dancingClient.spoiler.getMoveString() + "&");
        } else {
            dancingClient.socketClient.send_data(dancingClient.name + "&");
            dancingClient.choreographer.receiveInput(dancingClient.socketClient.receive_data('&'));
            dancingClient.choreographer.receiveGameInfo(dancingClient.socketClient.receive_data('&'));
            dancingClient.choreographer.receiveStars(dancingClient.socketClient.receive_data('&'));
            dancingClient.socketClient.send_data(dancingClient.choreographer.getMoveString() + "&");
            dancingClient.socketClient.send_data("DONE&");
            dancingClient.socketClient.send_data(dancingClient.choreographer.getLineString() + "&");
        }
        dancingClient.socketClient.close_socket();
    }
}
