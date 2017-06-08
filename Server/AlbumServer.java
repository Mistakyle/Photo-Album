import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Scanner;

public class AlbumServer implements Runnable {
	
	private Album album;
	private Socket socket;
	private boolean isMainServerThread;
	
	public AlbumServer(Album album) {
		this.album = album;
		isMainServerThread = true;

	}

	public AlbumServer(Socket socket, Album album) {
		this.socket = socket;
		this.album = album;
		isMainServerThread = false;
	}

	
	public void run() {
		if (isMainServerThread) {
			// Start the server socket
	        // Some configuration
			int port = 9876; // Just an arbitrary port number between 1024 and 65535
		    int backlog = 1; // allow up to 1 pending connection request
		    // Try to create a server socket for the given port
		    ServerSocket server = null;
		    try {
		    	// Create a server socket and make it listen on the given port.
		    	// Listening means "Incoming client connection requests are welcome"
	            server = new ServerSocket(port, backlog);
		    }
		    catch (IOException ioExc1) {
		    	System.err.println("Unable to create server socket on port " + port);
		    	System.err.println(ioExc1.getMessage());
		    	System.exit(1);
		    }
		    // We'll need the socket variable below to take the connection socket
		    // that gets returned by accept(). This is the one we'll then 
		    // actually use for communication.
		    Socket socket = null; // ...only null for now
	   		while (true) {
	   			try {
	        		// accept() waits for an incoming connection from a client.
		        	// Once the client connection request comes in, the method
				    // returns a Socket object for the connection to the client.
	        		socket = server.accept();
	   			}
	   			catch (IOException ioExc1) {
	   				// Ignore and continue
	   				System.err.println("Unable to accept the connection");
	   		    	System.err.println(ioExc1.getMessage());
	   		    	continue;
	   			}
	  	        // The socket can now tell us who it is connected to: 
	   		    System.out.println("Accepted connection from client " + socket.getRemoteSocketAddress());
	   		    // Handle the connection in its own thread
	   	        new Thread(new AlbumServer(socket, album)).start();    		
			}
		}
        else 
        {
        	// Service the request socket
			String remoteClient = socket.getRemoteSocketAddress().toString();
			try {
	   			String inputString = "";
	   			Scanner input = new Scanner(socket.getInputStream());
	   			// Get ready to send objects
	   			ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
	   			// Now communicate until the client sends "quit":
	   			while (!inputString.trim().equals("quit")) {
	       		    inputString = input.next();
	       		    // Determine the image index that the client wants
	       		    if (inputString.trim().matches("\\d+")) {
		       		    int imageIndex = Integer.parseInt(inputString);
		       		    if (imageIndex > 0) {
		       		    	imageIndex = imageIndex % album.getNumberOfImages();
		       		    }
		       		    // Communication here just means echoing the 
		       		    // client input back:
		       		    System.out.print("Client " + remoteClient);
		       		    System.out.print(" requested image : " + imageIndex);
		       		    AlbumImage responseImage = album.getImage(imageIndex);
		       		    System.out.println(" (\"" + responseImage.getName() + "\")");
		           	    output.writeObject(responseImage);
	       		    }
	   			}
	   			// Closing the Scanner and PrintStream prevents a resource
	   			// leak
	   			input.close();
	   			output.close();
	        }
			catch (IOException ioExc2) {
				// Ignore and continue
				System.err.println("Something went wrong with the connection");
		    	System.err.println(ioExc2.getMessage());
			}
			finally {
				try {
					// Closing the socket prevents a resource leak
					if (socket != null) socket.close();
				}
				catch (IOException ioExc3) {
					System.err.println("Couldn't close socket properly, ignoring.");
	  			    System.err.println(ioExc3.getMessage());
				}
			}
			System.out.println("Disconnected from client " + remoteClient + "!");
		}
	}
}
