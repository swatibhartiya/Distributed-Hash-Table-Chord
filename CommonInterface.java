import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This is the common interface which is shared by both 
 * the Bootstrap server as well as the Chord Node 
 * 
 * @author Swati Bhartiya (sxb4298)
 *
 */
public interface CommonInterface extends java.rmi.Remote{

	public void connected(String name) throws RemoteException;

	public String connectNodeToChord(String selfName, String selfIP) throws RemoteException;

	public void look(int identifier, String selfName, String selfIP) throws RemoteException, NotBoundException, IOException;
	
	public void assignNewObject(ChordNode newNode)throws RemoteException;

	public void allocateFiles(ChordNode newNode, int key) throws RemoteException, NotBoundException, FileNotFoundException, IOException;

	public void insert(String fileName, File f, String path, String selfIP) throws NoSuchAlgorithmException, RemoteException, NotBoundException, FileNotFoundException, IOException;

	public void search(String fileName, String path, String IP) throws RemoteException, NoSuchAlgorithmException, NotBoundException;

	public void printPath(String path, String selfName, String type) throws RemoteException;

	public void setSuccessor(ChordNode newNode) throws RemoteException;

	public void setPredecessor(ChordNode predeccesor, int lowerBound)throws RemoteException;

	public void removeFromMap(String selfName) throws RemoteException;

	public void populateFilesTable(
			ConcurrentHashMap<Integer, ArrayList<String>> filesInRange, String content, String fileName) throws RemoteException, IOException;

	public void setSuccessorNewNode(ChordNode newNode) throws RemoteException;
}
