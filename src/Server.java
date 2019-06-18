import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import packet.Packet;

public class Server implements Runnable{
	private static final String LOG_TAG = "Server";
	private static Logger Log = Logger.getLogger(LOG_TAG);
	
    public 	static final int DEFAULT_PACKET_SIZE = 8192;
	public 	static final int DEFAULT_PORT = 40406;
	private static int timeout = 60000;
	
	private ServerSocket serverSocket;
	private boolean runnable = false;
	private boolean keepAlive;
	private FileManager fileManager = FileManager.getInstance();
	
	public Server(int port) {
		try{
			if(port <= 0 || port >= 65536) {
				port = DEFAULT_PORT;
			}
			serverSocket = new ServerSocket(port);
			runnable = true;
		}catch(Exception e) {
			Log.log(Level.INFO, "Problem creating server: " + e.toString());
			runnable = false;
			if(serverSocket != null) {
				if(!serverSocket.isClosed()) {
					try {
						serverSocket.close();
					}catch(Exception e2) {
						
					}
				}
			}
		}
	}
	
	public Server() {
		this(DEFAULT_PORT);
	}

	@Override
	public void run() {
		if(this.runnable) {
			while(true) {
				Log.log(Level.INFO, "Server starting");
				runnable = false;
				keepAlive = true;
				try{
					Socket socket = serverSocket.accept();
					socket.setSoTimeout(timeout);
					ObjectInputStream ois;
					ObjectOutputStream oos;
					Object recieved;
					Packet packet;
					while(keepAlive) {
						try{
							ois = new ObjectInputStream(socket.getInputStream());
							Log.log(Level.INFO, "Waiting for client");
							recieved = ois.readObject();
							if(recieved instanceof Packet) {
								packet = (Packet) recieved;
								Log.log(Level.INFO, "Recieved a packet:\n" + packet.toString());
								oos = new ObjectOutputStream(socket.getOutputStream());
								// Here we have to check the flags
								//  	flagConnectionConfig should always be false
								// 		flagConnectionRequest means they are requesting a flagConnectionConfig for packet.fileName
								//			- Check if the file is in FileManager files, and return ammount of pieces, total pieces,...
								//			- Turn flagConnectionConfig to true
								//		All flags false means it is a file piece
								String fileName = packet.fileName;
								File file;
								if(packet.flagConnectionRequest) {
									if((file = this.fileManager.get(fileName)) != null) {
										// Retrieve data from file and send packet
				                        int configPieceNum = (int) Math.ceil(file.length() / DEFAULT_PACKET_SIZE);
				                        oos.writeObject(new Packet(fileName, configPieceNum, DEFAULT_PACKET_SIZE));
									}else{
										// We have not yet recieved this file
										fileManager.create(fileName);
				                        oos.writeObject(new Packet(fileName, 0, DEFAULT_PACKET_SIZE));
									}
								}else{
									//File piece
									if((file = this.fileManager.get(fileName)) == null) {
										// If this happens it is a big problem, recieved file piece without request connection first
										Log.log(Level.SEVERE, "Recieved a piece that should not have been recieved");
										oos.writeObject(new Packet(fileName, false));
									}else{
										int numPiece = packet.numPiece;
										int totalPiece = packet.pieceTotal;
										Log.log(Level.INFO, "Saving data");
										
										oos.writeObject(new Packet(fileName, fileManager.writeToFile(file, numPiece, totalPiece, packet.data)));
									}
								}
							}else{
								Log.log(Level.INFO, "Recieved object but not a packet");
							}
						}catch(SocketTimeoutException e) {
							keepAlive = false;
							Log.log(Level.INFO, "Socked timed out " + e.toString());
						}catch(IOException e) {
							keepAlive = false;
							Log.log(Level.WARNING, "IOException, client closed connection " + e.toString());
							try {
								if(socket != null) {
									socket.close();
								}
							}catch(Exception e2) {
								
							}
						}catch(ClassNotFoundException e) {
							keepAlive = false;
							Log.log(Level.WARNING, "readObject failed " + e.toString());
						}
					}
				}catch(IOException e) {
					Log.log(Level.SEVERE, "Socket couldn't be created" + e.toString());
				}
			}
		}
	}

}
