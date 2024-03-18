// Relevant Imports
import java.io.*;
import java.net.*;
import java.time.*;

public class ClientHandler extends Thread {
    
    // Class Variables
    private Socket clientSocket = null;
	private PrintWriter outputStream = null;
	private BufferedReader inputStream = null;

    public ClientHandler(Socket socket) {
        super("ClientHandler");
        this.clientSocket = socket;
    }

    public void List() {

        // Check serverFiles for files
        File directory = new File("serverFiles");
        File[] allFiles = directory.listFiles();

        // Check if no files
        if (allFiles.length == 0) {
            outputStream.println("empty");
        } else {
            // Write all file names to stream
            for (int i = 0; i < allFiles.length; i++) {
                outputStream.println(allFiles[i].getName());
            }
        }

    }

    public void Put(String filename) {

        // Create file in serverFiles
        File newFile = new File("serverFiles", filename);
        PrintWriter fileWriter = null;
        String newLine = null;
        // Check if it already exists
        if (newFile.exists()) {
            outputStream.println("exists");
        } else {
            outputStream.println("fine");
            try {
                newFile.createNewFile();
                fileWriter = new PrintWriter(new FileWriter(newFile.getAbsolutePath(), true));
                while ((newLine=inputStream.readLine()) != null) {
                    fileWriter.println(newLine);
                    fileWriter.flush();
                }
            } catch (IOException e) {
                System.err.println("ERROR: Issue while reading from client into file");
			    System.exit(1);
            }
        }

    }

    public void run() {

        try {
            // I/O Streams
            outputStream = new PrintWriter(clientSocket.getOutputStream(), true);
            inputStream = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch (IOException e) {
			System.err.println("ERROR: Failed to establish I/O with client");
			System.exit(1);
        }

        String request = null;
        // Accept client request
        try {
            request = inputStream.readLine();
        } catch (IOException e) {
			System.err.println("ERROR: Failure receving client request");
			System.exit(1);
        }

        // Log client request
        InetAddress clientAddress = clientSocket.getInetAddress();
        String clientIP = clientAddress.getHostAddress();
        String requestType = null;
        String filename = null;
        if (request.startsWith("list")) {
            requestType = "list";
        } else if (request.startsWith("put")) {
            requestType = "put";
            // Extract filename from request
            filename = request.substring(4);
        }
        try {
            PrintWriter logger = new PrintWriter(new FileWriter("log.txt", true));
            logger.println(LocalDate.now() + "|" + LocalTime.now() + "|" + clientIP + "|" + requestType);
            logger.flush();
			logger.close();
        } catch (IOException e) {
            System.err.println("ERROR: Failure logging request");
			System.exit(1);
        }

        switch (requestType) {
            case "list":
                List();
                break;
            case "put":
                Put(filename);
                break;
        }
        
		try {
			// Close all open connections
			inputStream.close();
			outputStream.close();
			clientSocket.close();
		} catch (IOException e) {
			System.err.println("ERROR: Issue closing connection with client");
			System.exit(1);
		}

    }

}
