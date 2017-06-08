import java.awt.Image;
import java.io.File;
import java.io.Serializable;
import java.io.ObjectStreamField;
import java.io.IOException;
import javax.swing.ImageIcon;

public class AlbumImage implements Serializable {
	private Image image; // Image isn't Serializable
	private ImageIcon icon; // but ImageIcon is (and contains an Image)
    private String caption;
    private String name;
    private String path;
    
    // Identify which fields should be serialized
    private static final ObjectStreamField[] serialPersistentFields =  {
        new ObjectStreamField("icon", ImageIcon.class),
        new ObjectStreamField("caption", String.class),
        new ObjectStreamField("name", String.class),
        new ObjectStreamField("path", String.class)
        };
    
    // This method gets called upon serialization
    private void writeObject(java.io.ObjectOutputStream stream) throws IOException {
    	icon = new ImageIcon();
    	icon.setImage(image);
        stream.defaultWriteObject( );
    }

    // This method gets called upon deserialization
    private void readObject(java.io.ObjectInputStream stream) throws IOException,
        ClassNotFoundException {
        stream.defaultReadObject(); // calls default constructor
        image = icon.getImage(); // additional configuration
    }
    
    // A default constructor is required for deserialization.
    // It needs to initialize all serialized fields.
    public AlbumImage() {
    	path = "";
    	icon = new ImageIcon();
    	name = "";
    	caption = "";
    }
    
    // Overloaded constructor for use in application
    public AlbumImage(File f, String name) {
    	path = f.getAbsolutePath();
    	icon = new ImageIcon(path);
    	image = icon.getImage();  
    	this.name = name;
    	caption = "";
    }
    
	public Image getImage() {
		return image;
	}
	
	public void setImage(Image image) {
		this.image = image;
	}
	
	public ImageIcon scaleIcon(int frameHeight) {
        if (image != null) {
    		Image img = image.getScaledInstance(
				3*frameHeight*image.getWidth(null)/(5*image.getHeight(null)), 
				3*frameHeight/5, 
				Image.SCALE_FAST);
	    	icon.setImage(img);
        }
		return icon;
	}
	
	public ImageIcon getIcon() {
		return icon;
	}

	public void setIcon(ImageIcon icon) {
		this.icon = icon;
	}

	public String getCaption() {
		return caption;
	}
	
	public void setCaption(String caption) {
		this.caption = caption;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getPath() {
		return path;
	}
	
	public void setPath(String path) {
		this.path = path;
	}
}
