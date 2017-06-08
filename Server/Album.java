import java.util.ArrayList;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import java.io.EOFException;
import java.io.File;
import java.io.IOException; // new
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths; // new

import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JLabel;

public class Album extends JFrame implements ComponentListener, ItemListener {
    
	private ArrayList<AlbumImage> images;
	private JComboBox<String> imagesComboBox;
	private JLabel iconLabel;
	private JLabel textFieldLabel;
	private JTextField nameTextField;
	private JButton saveButton;
	private JButton nextButton;
	private JTextArea captionTextArea;
	private int currentImageIndex = -1; 
	private Font font;
	
	public Album() {
    	super("Photo Album");

    	setLayout(new FlowLayout());
    	setSize(1200,600);
    	setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    	font = new Font("Sans-serif", Font.PLAIN, 20);
    	setupArrayList(); // moved down so new functionality can use the font
    	setupMenu();
    	setupTextField();
    	setupSaveButton();
    	setupNextButton();

        clear();
    	setupComboBox();
    	setupImageLabel();
    	setupTextArea();


    	this.addComponentListener(this);
    	setVisible(true);		
    	// This code was added to enable album load from file
    	// at startup:
    	if (images.size() > 0) {
    		// Enable all components & make them visible
    		nameTextField.setEnabled(true);
    		saveButton.setEnabled(true);  
    		if (images.size() > 1) {
    			nextButton.setEnabled(true);
    		}
    		iconLabel.setVisible(true);
			captionTextArea.setVisible(true);
			captionTextArea.setEnabled(true);
    		// Set the current image
    		setImage(currentImageIndex);
    	}
    	// This code starts the Album server
    	new Thread(new AlbumServer(this)).start(); 
	}
	
	private void setupArrayList() {
		images = new ArrayList<AlbumImage>();
		// Step 1: Open an ObjectInputStream for the file
		ObjectInputStream input = null;
		try {
    		input = new ObjectInputStream(Files.newInputStream(Paths.get("album.bin")));
		}
		catch (IOException ioExc1) {
			// File doesn't exist or isn't readable 
			// - nothing to load here
			return;
		}
		// Step 2: File is open, now read object by object until 
		// we get an EOFException
		try {
     		while (true) {
     			AlbumImage image = (AlbumImage) input.readObject();
			    images.add(image);
	     		currentImageIndex = 0;
     		}
     	}
        catch (EOFException eofExc) {
        	// Nothing to do here
        }
		catch (ClassNotFoundException cnfExc) {
			System.err.println("Class AlbumImage not found!");
		}
        catch (IOException ioExc) {
			System.err.println("Unable to read from album.bin!");
			System.exit(1);
        }
		// Step 3: Close the stream
		try {
    		input.close();
		}
		catch (IOException ioExc2) {
			System.err.println("Unable to close album.bin!");
			System.exit(1);
		}
	}
	
	private void setupMenu() {
    	JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic('F');
		fileMenu.setFont(font);
		menuBar.add(fileMenu);
		JMenuItem fileMenuOpen = new JMenuItem("Add image");
		fileMenuOpen.setFont(font);
		fileMenuOpen.addActionListener(
				new ActionListener()
				{
					public void actionPerformed(ActionEvent e) {
    					JFileChooser fileChooser = new JFileChooser(".");
    					int retval = fileChooser.showOpenDialog(Album.this);
    					if (retval == JFileChooser.APPROVE_OPTION) {
    						File f = fileChooser.getSelectedFile();
    						// Create album image from image file
    						currentImageIndex = images.size();
    						AlbumImage image = new AlbumImage(f, "Image " + currentImageIndex);
    						images.add(image);
    						// Replace icon with scaled version 
    						iconLabel.setIcon(image.scaleIcon(Album.this.getHeight())); 
	        				updateComboBox();
	        				setImage(currentImageIndex);
	        				nameTextField.setEnabled(true);
	        				captionTextArea.setVisible(true);
	        				captionTextArea.setEnabled(true);
	        				saveButton.setEnabled(true);   
	        				nextButton.setEnabled(true);
    					}
					}
				}
        );
		fileMenu.add(fileMenuOpen);
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
	
	private void setupTextField() {
		textFieldLabel = new JLabel("Image name:");
		textFieldLabel.setFont(font);
		add(textFieldLabel);
		nameTextField = new JTextField("",25);
		nameTextField.setFont(font);
		nameTextField.setEnabled(false);
		add(nameTextField);
	}
	
	private void setupSaveButton() {
		saveButton = new JButton("Save");
		saveButton.setFont(font);
		saveButton.setEnabled(false);
		saveButton.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						if (nameTextField.getText().length() > 0) {
							images.get(currentImageIndex).setName(nameTextField.getText());
							images.get(currentImageIndex).setCaption(captionTextArea.getText());
							updateComboBox();
							// The code below here was added to save image info
							// to file.
							// Step 1: Open file for writing with an ObjectOutputStream
							ObjectOutputStream output = null; 
							try {
								output = new ObjectOutputStream(
										    Files.newOutputStream(Paths.get("album.bin")));
							}
							catch (IOException ioExc1) {
								System.err.println("Can't open album.bin for writing!");
								return;
							}
							// Step 2: Write all entries to file (need to rewrite the
							// whole file)
							try {
								for (int i=0; i<images.size(); i++) {
									// Write AlbumImage objects to file:
									output.writeObject(images.get(i));
								}
							}
							catch (IOException ioExc2) {
								System.err.println("Unable to write to album.bin!");
							}	
							// Step 3: Close the file
							try {
    							output.close(); // prevent resource leak
							}
							catch (IOException ioExc3) {
								System.err.println("Unable to close album.bin!");
							}		
						}
					}
				}
	    );
		add(saveButton);
	}
	
	private void setupNextButton() {
		nextButton = new JButton("Next");
		nextButton.setFont(font);
		nextButton.setEnabled(false);
		nextButton.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						currentImageIndex = ++currentImageIndex % images.size();
						setImage(currentImageIndex);
					}
				}
	    );
		add(nextButton);
	}
	
	private void setupComboBox() {
		imagesComboBox = new JComboBox<String>();
		imagesComboBox.setModel( new DefaultComboBoxModel<String>());
		imagesComboBox.setMaximumRowCount(8);
		imagesComboBox.setFont(font);
		imagesComboBox.setMinimumSize(new Dimension(300,25));
		// The following lines replace the line that makes the box
		// initially invisible. The lines ensure that the ComboBox 
		// is updated with the right items for the images in the'
		// album as we load the information from the file.
		if (images.size() > 0) {
			updateComboBox();
			imagesComboBox.setVisible(true);
		}
		else
		{
			imagesComboBox.setVisible(false);
		}
		// The next line has moved down to prevent premature
		// event dispatch to our ItemListener while we update
		// the ComboBox with our image items from file.
		imagesComboBox.addItemListener(this); 
		add(imagesComboBox);
	}
	
	private void setupImageLabel() {
		iconLabel = new JLabel();
		// We need the following line so setImage()
		// has an ImageIcon to replace the image on
		// when we load from file.
		iconLabel.setIcon(new ImageIcon());
		add(iconLabel);
		
	}
	
	private void setupTextArea() {
		captionTextArea = new JTextArea("", 10, 20);
		captionTextArea.setFont(font);
		captionTextArea.setVisible(false);
		captionTextArea.disable(); // alternative to setEnabled(false)
		add(new JScrollPane(captionTextArea));
	}
	
	private void setImage(int imageIndex) {
		AlbumImage img = images.get(imageIndex);
		if (img != null) {
       		iconLabel.setIcon(img.scaleIcon(getHeight()));
       		iconLabel.repaint();
			nameTextField.setText(img.getName());
			captionTextArea.setText(img.getCaption());
			imagesComboBox.setSelectedIndex(imageIndex);
		}
	}
	
	private void updateComboBox() {
		imagesComboBox.removeAllItems();
		for (AlbumImage image : images) {
			imagesComboBox.addItem(image.getName());
		}
		imagesComboBox.setSelectedIndex(currentImageIndex);
		imagesComboBox.setVisible(true);
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
	    	AlbumImage img = images.get(currentImageIndex);
	    	iconLabel.setIcon(img.scaleIcon(getHeight()));
    	}
    	iconLabel.setSize(this.getSize());
    	this.repaint();
    }

    public void componentShown(ComponentEvent e) {
        return;
    }
	
    // ItemListener method
    public void itemStateChanged(ItemEvent e) {
    	if (e.getStateChange() == ItemEvent.SELECTED) {
    		setImage(imagesComboBox.getSelectedIndex());
    	}
    	return;
    }
    
    // getImage() is called by the server
    public AlbumImage getImage(int imageIndex) {
    	if (imageIndex >= 0 && imageIndex < images.size()) {
    		return images.get(imageIndex);
    	}
    	else
    	{
    		return new AlbumImage(); // just return an empty object
    	}
    }
    
    // getImageNumber() is called by the server
    public int getNumberOfImages() {
    	return images.size();
    }

    public void clear()
	{
		images.clear();
	}
}
