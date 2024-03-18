// Relevant Imports
import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class Client {

	// Class Variables
	private Socket serverSocket = null;
	private PrintWriter outputStream = null;
	private BufferedReader inputStream = null;

	public void run(String[] command) {

		try {
			// Setup server socket and I/O streams
			serverSocket = new Socket("localhost", 9900);
			outputStream = new PrintWriter(serverSocket.getOutputStream(), true);
			inputStream = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
		} catch (IOException e) {
			System.err.println("ERROR: Failed to establish I/O for designated port/host");
			System.exit(1);
		}

		// Call relevant command based on arguments
		switch (command[0]) {
			case "list":
				List();
				break;
			case "put":
				Put(command[1]);
				break;
			default:
				// Argument provided is not a valid command
				System.err.println("ERROR: Invalid command provided");
				System.exit(1);
		}

		try {
			// Close all open connections
			inputStream.close();
			outputStream.close();
			serverSocket.close();
		} catch (IOException e) {
			System.err.println("ERROR: Issue closing connection with server");
			System.exit(1);
		}

	}

	// Function for list command
	public void List() {

		// Make request of server
		outputStream.println("list");

		// Read output from server
		String newLine = null;
		System.out.println("Listing File(s):");
		try {
			while ((newLine = inputStream.readLine()) != null) {
				if (newLine.equals("empty")) {
					System.out.println("--No files in folder--");
				} else {
					System.out.println(newLine);
				}
			}
		} catch (IOException e) {
			System.err.println("ERROR: Issue while reading from server");
			System.exit(1);
		}

	}

	// Function for put command
	public void Put(String filename) {

		// Make request of server
		outputStream.println("put " + filename);

		// Read in file contents
		BufferedReader fileReader = null;
		ArrayList<String> fileContents = new ArrayList<>();
		String newLine = null;
		try {
			fileReader = new BufferedReader(new FileReader(filename));
			while((newLine=fileReader.readLine()) != null) {
				fileContents.add(newLine);
			}
		} catch (FileNotFoundException e) {	
			System.err.println("ERROR: File does not exist");
			System.exit(1);
		} catch (IOException e) {
			System.err.println("ERROR: Issue while reading from file");
			System.exit(1);
		}

		// Ensure file doesn't already exist before writing
		try {
			if (inputStream.readLine().equals("exists")) {
				System.out.println("File of that name already exists in the server");
			} else {
				for (int i = 0; i < fileContents.size(); i++) {
					outputStream.println(fileContents.get(i));
				}
			}
		} catch (IOException e) {
			System.err.println("ERROR: Issue while reading from the server");
			System.exit(1);			
		}

	}

	public static void main( String[] args ) {

		// Ensure argument length is suitable for possible commands
		if ((args.length < 1) || (args.length > 2)) {
			System.err.println("ERROR: Invalid number of arguments provided");
			System.exit(1);
		} else if ((!args[0].equals("list")) && (!args[0].equals("put"))) {
			System.err.println(args[0]);
			System.err.println("ERROR: Invalid command provided");
			System.exit(1);
		} else if (((args[0].equals("list")) && (args.length != 1)) || ((args[0].equals("put")) && (args.length != 2))) {
			System.err.println("ERROR: Incorrect command usage");
			System.err.println("EXAMPLE USAGE:");
			System.err.println("list");
			System.err.println("put 'filename'");
			System.exit(1);
		} else if (args.length == 2) {
			File chosenFile = new File(args[1]);
			// check file exists before attempting a request
			if (chosenFile.exists() == false) {
				System.err.println("ERROR: File does not exist");
				System.exit(1);
			}
		}

		Client startClient = new Client();
		startClient.run(args);
	}

}