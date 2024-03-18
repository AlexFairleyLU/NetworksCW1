// Relevant Imports
import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class Server {

	// Class variables
	private ServerSocket openServer = null;
	private ExecutorService threadService = null;

	public void startServer() {

		// Initialise port as chosen number
		int chosenPort = 9900;

		try {
			// Open server on chosen port
			openServer = new ServerSocket(chosenPort);
		} catch (IOException e){
			System.err.println("ERROR: Failed to start server on port:" + chosenPort);
			System.exit(1);
		}

		// Create log file
		try {
			File logFile = new File("log.txt");
			logFile.createNewFile();
			PrintWriter logSetup = new PrintWriter("log.txt", "UTF-8");
			logSetup.println("date|time|client IP address|request");
			logSetup.close();
		} catch (IOException e) {
			System.err.println("ERROR: Issue creating log file");
			System.exit(1);
		}

		// Initialise Executor
		threadService = Executors.newFixedThreadPool(20);

		while (true) { 
			try {
				// Accept client connection
				Socket client = openServer.accept();
				threadService.submit(new ClientHandler(client));
			} catch (IOException e) {
				System.err.println("ERROR: Failed to accept connection and start client handler thread");
				System.exit(1);
			}
		}

	}

	public static void main( String[] args ) throws IOException {
		Server newServer = new Server();
		newServer.startServer();
	}

}