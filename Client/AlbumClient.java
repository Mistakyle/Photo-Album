import java.util.ArrayList;
import java.util.Formatter; // new
import java.util.regex.Pattern; // new
import java.util.Scanner; // new

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Image;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException; // new
import java.io.IOException; // new
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.lang.SecurityException; // new
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths; // new

import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import javax.swing.JComboBox;
import javax.swing.MutableComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JLabel;

public class AlbumClient extends JFrame implements ComponentListener {
    
	private Socket socket;
	private PrintStream output;
	private ObjectInputStream input;
	private AlbumImage image = null;
	private JLabel iconLabel;
	private JLabel nameText;
	private JButton nextButton;
	private JLabel captionText;
	private int currentImageIndex = 0; 
	private Font font;
	
	public AlbumClient() {
    	super("Photo Album Client");
    	setLayout(new FlowLayout());
    	setSize(1200,600);
    	setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    	font = new Font("Sans-serif", Font.PLAIN, 20);
    	setupNameLabel();
    	setupImageLabel();
    	setupCaptionLabel();
    	setupNextButton();
    	this.addComponentListener(this);
    	setVisible(true);		
    	// This code was added to enable album load from file
    	// at startup:
		// IP address and port to connect to:
        String serverHost = "localhost";
        int serverPort = 9876;
        socket = null;
        try {
            socket = new Socket(serverHost, serverPort);
            output = new PrintStream(socket.getOutputStream());
            input = new ObjectInputStream(socket.getInputStream());
        } catch (Exception e1) {
        	System.err.print("Unable to connect to " + serverHost);
        	System.err.println(" on " + serverPort);
        	System.err.println(e1.getMessage());
        	System.exit(1);
        }
        System.out.print("Connected to " + serverHost);
        System.out.println(" on port " + serverPort);   
   		setImage(currentImageIndex);
	}
	
	
	private void setupMenu() {
    	JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic('F');
		fileMenu.setFont(font);
		menuBar.add(fileMenu);
		JMenuItem fileMenuQuit = new JMenuItem("Quit");
		fileMenuQuit.setFont(font);
		fileMenu.add(fileMenuQuit);
		fileMenuQuit.addActionListener(
				new ActionListener() 
				{
					public void actionPerformed(ActionEvent e) {
						System.exit(0);
					}
				}
				);
	}
	
	private void setupNameLabel() {
		nameText = new JLabel("Loading image...");
		nameText.setFont(font);
		add(nameText);
	}
	
	
	private void setupNextButton() {
		nextButton = new JButton("Next");
		nextButton.setFont(font);
		nextButton.setEnabled(false);
		nextButton.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						currentImageIndex = ++currentImageIndex;
						setImage(currentImageIndex);
					}
				}
	    );
		add(nextButton);
	}
	
	private void setupImageLabel() {
		iconLabel = new JLabel();
		// We need the following line so setImage()
		// has an ImageIcon to replace the image on
		// when we load from the network.
		iconLabel.setIcon(new ImageIcon());
		add(iconLabel);
		
	}
	
	private void setupCaptionLabel() {
		captionText = new JLabel("");
		captionText.setFont(font);
		captionText.setVisible(true);
		add(captionText);
	}
	
	private void setImage(int imageIndex) {
        try {
            System.out.println("Requesting image " + imageIndex + " from server.");
            output.println(imageIndex);
            System.out.println("Done. Waiting for server response.");
            image = (AlbumImage) input.readObject();
            System.out.println("Server response received.");
        }
        catch (Exception e) {
        	System.err.print("Something went wrong with the socket communication.");
        	System.err.println(e.getMessage());
        	System.exit(1);
        }
		if (!image.getPath().equals("")) {
       		iconLabel.setIcon(image.scaleIcon(getHeight()));
       		iconLabel.repaint();
			nameText.setText(image.getName());
			captionText.setText(
					"<html>" + 
			        image.getCaption().replaceAll("\\n", "<br>\n") +
			        "</html>");
			nextButton.setEnabled(true);
		}
	}
	
	// ComponentListener methods
	
	public void componentHidden(ComponentEvent e) {
        return;
    }

    public void componentMoved(ComponentEvent e) {
        return;
    }

    public void componentResized(ComponentEvent e) {
    	if (currentImageIndex >= 0) {
	    	iconLabel.setIcon(image.scaleIcon(getHeight()));
    	}
    	iconLabel.setSize(this.getSize());
    	this.repaint();
    }

    public void componentShown(ComponentEvent e) {
        return;
    }
	    
}
