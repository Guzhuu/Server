import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * This class' objective is to save and give info to Server about stored files
 * It will save videos and data in a "rendering" directory and put them after
 * they are done in the done directory
 * **/
public class FileManager {
	private static final String LOG_TAG = "FileManager";
	private static Logger Log = Logger.getLogger(LOG_TAG);
	private static final String recievingDirectory = ".in-progress";
	private static final String doneDirectory = "done";
	
	private static FileManager singleton;
	
	private File recievingDirectoryFile;
	private File doneDirectoryFile;
	private List<File> fileList;
	
	
	private FileManager() {
		try {
			recievingDirectoryFile = new File(recievingDirectory);
			doneDirectoryFile = new File(doneDirectory);
			fileList = this.recoverFiles();
			boolean rec = recievingDirectoryFile.mkdirs();
			boolean don = doneDirectoryFile.mkdirs();
			if(!rec || !don) {
				Log.log(Level.SEVERE, "Could not create directory");
			}else {
				Log.log(Level.SEVERE, "Directories created succesfully");
			}
		}catch(Exception e) {
			Log.log(Level.SEVERE, "Could not create directory: " + e.toString());
		}
	}
	
	public static FileManager getInstance() {
		if(singleton == null) {
			singleton = new FileManager();
		}
		return singleton;
	}
	
	private List<File> recoverFiles(){
		List<File> retorno = new LinkedList<File>();
		File[] files;
		if(!recievingDirectoryFile.isDirectory()) {
			return retorno;
		}
		files = recievingDirectoryFile.listFiles();
		for(int i = 0; i < files.length; i++) {
			if(!files[i].isDirectory() && files[i].exists()) {
				retorno.add(files[i]);
			}else {
				files[i].delete();
			}
		}
		return retorno;
	}
	
	private int getNumDoneFiles() {
		if(!doneDirectoryFile.isDirectory()) {
			return 0;
		}
		return doneDirectoryFile.listFiles().length;
	}

	
	/*
	 * Returns a file with name s if it exists in fileList, else it returns null
	 * */
	public File get(String s) {
		for(File f : this.fileList) {
			if(f.getName().equals(s)){
				return f;
			}
		}
		return null;
	}
	
	/*
	 * Adds a new file to fileList and creates it, only if it does not exists
	 * */
	public boolean create(String s) {
		boolean retorno = false;
		try{
			File newFile = new File(recievingDirectoryFile, s);
			if(newFile.exists()) {
				return false;
			}
			retorno = newFile.createNewFile();
			if(retorno) {
				this.fileList.add(newFile);
			}
		}catch(IOException e) {
			Log.log(Level.SEVERE, "Could not create new file: " + e.toString());
			retorno = false;
		}
		return retorno;
	}
	
	public boolean writeToFile(File file, int numPacket, int totalPacket, byte[] data) {
		if(this.fileList.contains(file) && file.exists() && !file.isDirectory()) {
			try{
				RandomAccessFile raf = new RandomAccessFile(file, "rw");
				raf.seek(numPacket * data.length);
				raf.write(data);
				raf.close();
				if(numPacket == totalPacket) {
					this.doFinal(file);
				}
				return true;
			}catch(FileNotFoundException e) {
				Log.log(Level.SEVERE, "File does not exist but it is registered as that: " + e.toString());
			}catch(IOException e) {
				Log.log(Level.SEVERE, "Error writting to file: " + e.toString());
			}
		}else {
			Log.log(Level.WARNING, "File does not exist");
			//this.add(file.getName());
			return false;
		}
		return false;
	}
	
	private void doFinal(File f) {
		try{
			File dir = new File(doneDirectoryFile + File.separator + getFileDate(f));
			if(dir.mkdirs() || dir.exists()) {
				f.renameTo(new File(dir, f.getName()));
				Log.log(Level.INFO, "Saving to " + dir.getAbsolutePath());
			}else {
				f.renameTo(new File(doneDirectoryFile, f.getName()));
				Log.log(Level.INFO, "Saving to " + doneDirectoryFile.getAbsolutePath());
			}
		}catch(Exception e) {
			Log.log(Level.WARNING, e.toString());
		}
	}
	
	/*
	 * name--YYYY-DD-MM_HH-mm-ss.mp4
	 * 		|
	 * 		v
	 * name--YYYY-DD-MM_HH-mm-ss
	 * 		|
	 * 		v
	 * 	YYYY-DD-MM_HH-mm-ss
	 * 		|
	 * 		v
	 * 	YYYY-DD-MM
	 * */
	private String getFileDate(File f) {
		try {
			String[] parts = f.getName().split("\\.");
			String[] parts2 = parts[0].split("--");
			String[] parts3 = parts2[1].split("_");
			return parts3[0];
		}catch(Exception e) {
			return Integer.toString(getNumDoneFiles()+1);
		}
	}
}
