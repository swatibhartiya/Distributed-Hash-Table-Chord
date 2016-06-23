import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class is for the bootstrap which is used to 
 * serve as an entry point for each node who wishes
 * to join the network. 
 * 
 * @author Swati Bhartiya (sxb4298)
 *
 */
public class BootStrap extends UnicastRemoteObject implements CommonInterface, Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	int portNo = 4798;
	HashMap<String, String> nodesPresent = new HashMap<>();
	
	/**
	 * This is the constructor of the BootStrap node
	 * 
	 * @throws RemoteException
	 */
	protected BootStrap() throws RemoteException {
		super();
		Registry reg = LocateRegistry.createRegistry(portNo);
		reg.rebind("bootstrap", this);
		System.out.println("Bootstrap object bound to registry");
	}

	/**
	 * This method is called by a node 
	 * of the Chord class in order to determine
	 */
	@Override
	public void connected(String name){
		System.out.println("Connected: " + name);
	}

	/**
	 * This method is used to connect a new
	 * node to the Chord ID space
	 */
	@Override
	public String connectNodeToChord(String selfName, String selfIP) {
		// TODO Auto-generated method stub
		if(nodesPresent.isEmpty()){
			System.out.println("First node to join chord");
			nodesPresent.put(selfName, selfIP);
			return "First Node";
		}else if (nodesPresent.containsKey(selfName)){
			return "Already exists";
		}
		else{
			Map.Entry<String, String> entry = nodesPresent.entrySet().iterator().next();
			String key = entry.getKey(); 
			String val = entry.getValue();
			System.out.println(selfName+" needs to connect to: " + key + " with IP: " + val);
			nodesPresent.put(selfName, selfIP);
			return val;	// IP address of 1st node
		}
	}

	/**
	 * This is the main method of the BootStrap class
	 * 
	 * @param args
	 * @throws RemoteException
	 */
	public static void main(String[] args) throws RemoteException {
		BootStrap bObj = new BootStrap(); 
	}

	@Override
	public void look(int identifier, String selfName, String selfIP) throws RemoteException, NotBoundException, IOException {
	}
	
	@Override
	public void assignNewObject(ChordNode newNode)throws RemoteException{
		
	}

	@Override
	public void allocateFiles(ChordNode newNode, int key) throws RemoteException,
			NotBoundException ,FileNotFoundException, IOException {
		
	}

	@Override
	public void insert(String fileName, File f, String path, String selfIP) throws NoSuchAlgorithmException, RemoteException, NotBoundException{
		
	}

	@Override
	public void search(String fileName, String path, String IP) throws RemoteException, NoSuchAlgorithmException, NotBoundException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void printPath(String path, String selfName, String type) throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setSuccessor(ChordNode newNode) throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setPredecessor(ChordNode predeccesor, int lowerBound) throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	/**
	 * This method is used to remove a node 
	 * from the Chord ID space whenever 
	 * the node requests to leave
	 * 
	 */
	@Override
	public void removeFromMap(String selfName) throws RemoteException {
		// TODO Auto-generated method stub
		if(nodesPresent.containsKey(selfName)){
			nodesPresent.remove(selfName);
			System.out.println("Node removed");
		}else{
			System.out.println("Not removed!!!!!!!!!!!");
		}
	}

	@Override
	public void populateFilesTable(
			ConcurrentHashMap<Integer, ArrayList<String>> filesInRange, String content, String fileName) throws RemoteException, IOException{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setSuccessorNewNode(ChordNode newNode) throws RemoteException {
		// TODO Auto-generated method stub
		
	}
}
