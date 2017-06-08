import javax.swing.SwingUtilities;

public class RunAlbumClient implements Runnable {

	public void run() {
		AlbumClient c = new AlbumClient();
	}
	
	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new RunAlbumClient());
	}

}
