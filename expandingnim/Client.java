public class Client {

  public static void main(String[] args) {

    String hostName = args[0];
    int portNumber = Integer.parseInt(args[1]);
    
    try (
        Socket nimSocket = new Socket(hostName, portNumber);
        PrintWriter out = new PrintWriter(nimSocket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(nimSocket.getInputStream()));
    ) {
      String userInput;
      while ((userInput = stdIn.readLine()) != null) {
        out.println(userInput);
        System.out.println("echo: " + in.readLine());
      }
    } catch (Exception e) {
      System.out.println("Error connecting to client");
      System.exit(1);
    } finally {
      out.close();
    }
  }


}