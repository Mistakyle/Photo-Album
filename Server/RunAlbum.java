import javax.swing.SwingUtilities;

public class RunAlbum implements Runnable {

	public void run() {
		Album c = new Album();
	}
	
	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new RunAlbum());
	}

}
