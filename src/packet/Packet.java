package packet;

import java.io.Serializable;

public class Packet implements Serializable {
	public boolean flagOK;
    // Indicates whether this is a file packet or config packet
    public boolean flagConnectionConfig;

    // Indicates that a config packet is being requested
    public boolean flagConnectionRequest;

    public String fileName;
    public byte[] data;
    // If file has already been fully received, numPiece must be >= pieceTotal
    public int numPiece;
    public int pieceTotal;

    // Means recieved
    public Packet(String fileName, boolean flagOK){
        this.flagOK = flagOK;
    }

    // Normal packet data sending
    public Packet(String fileName, byte[] data, int numPiece, int pieceTotal){
        this.flagConnectionConfig = false;
        this.flagConnectionRequest = false;
        this.fileName = fileName; // Name
        this.data = data; // data
        this.numPiece = numPiece; // Piece X
        this.pieceTotal = pieceTotal; // out of Y
    }

    // Config packet
    public Packet(String fileName, int numPiece, int packetSize){
        // numPiece is the ammount of bytes (numPiece * packetSize) the server already has
        this.flagConnectionConfig = true;
        this.flagConnectionRequest = false;
        this.fileName = fileName;
        this.numPiece = numPiece;
        data = new byte[packetSize];
    }

    // Request packet for config with fileName name
    public Packet(String name){
        this.flagConnectionRequest = true;
        this.flagConnectionConfig = false;
        this.fileName = name;
    }
    
    public String toString() {
    	String retorno = "";
    	
    	retorno += "Flags:\n\t- flagOK = " + this.flagOK + "\n\t- flagConnectionConfig = " + this.flagConnectionConfig + "\n\t- flagConnectionRequest = " + this.flagConnectionRequest;
    	retorno += "\nInfo:\n\t- File name: " + this.fileName + "\n\t- Piece " + this.numPiece + "/" + this.pieceTotal;
    	if(this.data != null) {
    		retorno += "\nData:\n\t- Data: " + this.data + "\n\t- Length: " + this.data.length;
    	}else {
    		retorno += "\nData:\n\t- Data: " + this.data + "\n";
    	}
    	
    	return retorno;
    }
}
