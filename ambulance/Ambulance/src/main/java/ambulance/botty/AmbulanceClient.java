package ambulance.botty;

import org.json.*;

import java.io.IOException;
import java.util.ArrayList;

public class AmbulanceClient {

    private static SocketClient socketClient;

    public static void main(String[] args) throws IOException {

        if (args.length != 2) {
            System.out.println("Usage: java AmbulanceClient <host> <port>");
            System.exit(0);
        }
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        if (socketClient != null) socketClient.close_socket();

        // connect to server
        socketClient = new SocketClient(host, port);

        // send initial message
        socketClient.send_data(new JSONObject().put("name", "Botty McBotFace").toString());

        // receive buffer message
        JSONObject bufferMsg = new JSONObject(socketClient.receive_data().trim());
        int bufferSize = bufferMsg.getInt("buffer_size");
        System.out.println("Buffer Size: " + bufferSize);

        // receive problem message
        JSONObject probObj = new JSONObject(socketClient.receive_data(bufferSize).trim());

        // get patients
        ArrayList<Patient> patientArrayList = new ArrayList<>();
        JSONObject patientObj = probObj.getJSONObject("patients");
        for (String pId : patientObj.keySet()) {
            JSONObject p = patientObj.getJSONObject(pId);
            int x = p.getInt("xloc");
            int y = p.getInt("yloc");
            int d = p.getInt("rescuetime");
            patientArrayList.add(new Patient(Integer.parseInt(pId), x, y, d));
        }

        ArrayList<Hospital> hospitalArrayList = new ArrayList<>();
        JSONObject hospitalObj = probObj.getJSONObject("hospitals");
        for (String hId : hospitalObj.keySet()) {
            JSONObject h = hospitalObj.getJSONObject(hId);
            Hospital hos = new Hospital(Integer.parseInt(hId));
            JSONArray ams = h.getJSONArray("ambulances_at_start");
            for (int i = 0; i < ams.length(); i++) {
                hos.ambulancesAtStart.add(ams.getInt(i));
            }
            hospitalArrayList.add(hos);
        }

        ArrayList<Ambulance> ambulanceArrayList = new ArrayList<>();
        JSONObject amObj = probObj.getJSONObject("ambulances");
        for (String aId : amObj.keySet()) {
            JSONObject am = amObj.getJSONObject(aId);
            ambulanceArrayList.add(new Ambulance(Integer.parseInt(aId), am.getInt("starting_hospital")));
        }

        System.out.printf("Read %d patients\n", patientArrayList.size());
        System.out.printf("Read %d hospitals\n", hospitalArrayList.size());
        System.out.printf("Read %d ambulances\n", ambulanceArrayList.size());

        TestLibrary.run(patientArrayList, hospitalArrayList, ambulanceArrayList);

        // send buffer size
        JSONObject solObj = new JSONObject();
        String solString = solObj.toString();
        socketClient.send_data(new JSONObject().put("buffer_size", solString.length()).toString());

        // send solution
        socketClient.send_data(solString);

        // close socket
        socketClient.close_socket();
    }

}
