import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
	private static final String LOG_TAG = "Main";
	private static Logger Log = Logger.getLogger(LOG_TAG);
	
	public static void main(String[] args) {
		try {
			int port = Integer.parseInt(args[0]);
			if(port > 0 && port < 65536) {
				Log.log(Level.WARNING, "Starting server at port " + port);
				new Thread(new Server(port)).start();
			}else {
				Log.log(Level.WARNING, "Starting server at default 40406 port");
				new Thread(new Server(Server.DEFAULT_PORT)).start();
			}
		}catch(Exception e) {
			Log.log(Level.WARNING, "Starting server at default 40406 port");
			new Thread(new Server(Server.DEFAULT_PORT)).start();
		}
	}
}
