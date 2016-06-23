import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
/**
 * This class is for each node present in 
 * Chord
 * 
 * @author Swati Bhartiya (sxb4298)
 *
 */
class ChordNode implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	ChordNode predeccesor;
	ChordNode successor;
	// Hashtable<Integer, ArrayList<String>> filesInRange = new Hashtable<>();
	ConcurrentHashMap<Integer, ArrayList<String>> filesInRange = new ConcurrentHashMap<>();
	int upperBound;
	int lowerBound;
	String selfName;
	String selfIP;
}
/**
 * This class is used as the individual 
 * chord node which is used to perform 
 * join, leave, view, file insert and 
 * file search operations 
 * 
 * @author Swati Bhartiya (sxb4298)
 *
 */
public class Chord extends UnicastRemoteObject implements CommonInterface,
		Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	ChordNode cNode;
	String selfName;
	String selfIP;

	int portNo = 4798;

	String bootStrapIP = "129.21.22.196";

	int identifier;

	/**
	 * This is the constructor of the class for Chord
	 * 
	 * @throws UnknownHostException
	 * @throws RemoteException
	 */
	public Chord() throws UnknownHostException, RemoteException {
		super();
		Registry reg = LocateRegistry.createRegistry(portNo);
		reg.rebind("chord", this);
		System.out.println("Chord bound to registry");
		selfName = InetAddress.getLocalHost().getHostName();
		selfIP = InetAddress.getLocalHost().getHostAddress();
	}

	/**
	 * This is the main method that runs the 
	 * Chord program
	 * 
	 * @param args
	 * @throws NotBoundException
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 */
	public static void main(String[] args) throws NotBoundException,
			NoSuchAlgorithmException, IOException {
		Chord c = new Chord();
		c.identifier = c.computeHash(c.selfIP);
		Scanner sc = new Scanner(System.in);
		String command = "";
		while (true) {
			c.display();
			command = sc.nextLine();
			c.action(command);
		}
	}

	/**
	 * This is the method that is used to display
	 * the user of the node with the set of 
	 * available options.
	 */
	public void display() {
		System.out.println("You have the following options to choose from: ");
		System.out.println("Enter insert <file name> for file insert: ");
		System.out.println("Enter search <file name> for file search: ");
		System.out.println("Enter view for viewing information about "
				+ "the node: ");
		System.out.println("Enter join for node join: ");
		System.out.println("Enter leave for node leave: ");
		System.out.println("-------------------------------------------"
				+ "---------------");

	}

	/**
	 * This is the method which is used to take 
	 * an action based on the user input
	 * 
	 * @param command
	 * @throws NotBoundException
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 */
	public void action(String command) throws NotBoundException,
			NoSuchAlgorithmException, IOException {
		String[] commandArray = command.split(" ");
		switch (commandArray[0]) {
		case "insert":
			System.out.println("User entered Insert and wants to insert file: "
					+ commandArray[1]);
			File f = new File(commandArray[1]);
			insert(commandArray[1], f, "", selfIP);
			// view();
			break;
		case "view":
			view();
			break;
		case "search":
			System.out.println("User entered Search and wants to search for: "
					+ commandArray[1]);
			search(commandArray[1], "", selfIP);
			break;
		case "join":
			join();
			break;
		case "leave":
			leave();
			break;
		default:
			System.out.println("User entered wrong option");
			break;
		}
	}

	/**
	 * This method is used to join each node
	 * into the ID space at its appropriate
	 * location
	 * 
	 * @throws NotBoundException
	 * @throws IOException
	 */
	private void join() throws NotBoundException, IOException {
		Registry reg = LocateRegistry.getRegistry(bootStrapIP, portNo);
		CommonInterface bootstrapObj = (CommonInterface) reg
				.lookup("bootstrap");
		bootstrapObj.connected(selfName);

		String reply = bootstrapObj.connectNodeToChord(selfName, selfIP);
		if (reply.equals("First Node")) {
			// allocate the ID space
			cNode = new ChordNode();
			cNode.predeccesor = cNode;
			cNode.successor = cNode;

			// cNode.lowerBound = identifier;
			// cNode.upperBound = cNode.lowerBound + 1;

			cNode.upperBound = identifier;
			cNode.lowerBound = cNode.predeccesor.upperBound + 1;

			cNode.selfName = selfName;
			cNode.selfIP = selfIP;

		} else if (reply.equals("Already exists")) {
			System.out.println("Node join failure");
			return;
		} else {
			// System.out.println("selfName = " + selfName);
			reg = LocateRegistry.getRegistry(reply, portNo);
			CommonInterface chordObjToLookIn = (CommonInterface) reg
					.lookup("chord");
			chordObjToLookIn.look(identifier, selfName, selfIP);
		}
	}

	/**
	 * This method is called when a new node other than 
	 * the first node wants to join the network and gets 
	 * placed at its appropriate location
	 * 
	 */
	@Override
	public void look(int identifier, String selfName, String selfIP)
			throws NotBoundException, IOException {
		ChordNode newNode;
		// System.out.println("Identifier = " + identifier + " selfName = "
			// 	+ selfName);

		if (inRange(identifier)) {
			// System.out.println("2nd node issssssss, hence in my range");
			newNode = new ChordNode();
			// System.out.println("selfName = " + selfName);

			newNode.predeccesor = cNode.predeccesor;
			newNode.successor = cNode;

			newNode.upperBound = identifier;

			newNode.lowerBound = (newNode.predeccesor.upperBound + 1) % 1000;

			newNode.selfName = selfName;
			newNode.selfIP = selfIP;

			// cNode.predeccesor.successor = newNode;
			// cNode.predeccesor = newNode;

			newNode.successor.predeccesor = newNode;
			// newNode.predeccesor.successor = newNode;

			String predIP = newNode.predeccesor.selfIP;
			Registry reg = LocateRegistry.getRegistry(predIP, portNo);
			CommonInterface cI = (CommonInterface) reg.lookup("chord");

			cI.setSuccessorNewNode(newNode);

			newNode.successor.lowerBound = (newNode.upperBound + 1) % 1000;

			// System.out.println("cNode.upperbound = " + cNode.upperBound);
			// System.out.println("cNode.lowerbound = " + cNode.lowerBound);

			// cNode's bounds have changed
			// allocating files

			
			ArrayList<Integer> al = new ArrayList<Integer>();
			
			for (Map.Entry<Integer, ArrayList<String>> entry : cNode.filesInRange
					.entrySet()) {
				Integer key = entry.getKey();
				
				System.out.println("cNode files in range, key: " +key );
				if (!inRange(key)) {	// made a change here - reversed the sign
					al.add(key);
					allocateFiles(newNode, key);
				}
			}
			
			for(Integer key : al){
				cNode.filesInRange.remove(key);
			}

			// view();
			Registry reg2 = LocateRegistry.getRegistry(selfIP, portNo);
			CommonInterface chordNodeToAssign = (CommonInterface) reg2
					.lookup("chord");

			chordNodeToAssign.connected(cNode.selfName);
			chordNodeToAssign.assignNewObject(newNode);
		} else {
			// need not take optimal path!!!
			// System.out.println("\nNot in my range\nConnecting to successor");
			String succName = cNode.successor.selfName;
			// System.out.println("successor's name : " + succName);
			String succIP = cNode.successor.selfIP;
			// System.out.println("succ IP" + succIP);
			Registry reg = LocateRegistry.getRegistry(succIP, portNo);
			CommonInterface succObj = (CommonInterface) reg.lookup("chord");
			succObj.connected(cNode.selfName);
			succObj.look(identifier, selfName, selfIP);
		}
	}

	/**
	 * This method is called when the files
	 * need to be re-distributed after a new
	 * node joins the network
	 * 
	 */
	public void allocateFiles(ChordNode newNode, int key) throws NotBoundException,
			IOException {
		Integer id;
		ArrayList<String> al;
		ArrayList<Integer> hashes = new ArrayList<Integer>();

		File directory = new File(newNode.selfName);
		if (!directory.exists()) {
			directory.mkdir();
		}

		
		al = cNode.filesInRange.get(key);
		
		for (String name : al) {

			File f = new File(cNode.selfName + "/" + name);
			FileInputStream fis = new FileInputStream(f);
			InputStream in = fis;
			BufferedReader br = new BufferedReader(
					new InputStreamReader(in));
			String content = "";
			String line;
			while ((line = br.readLine()) != null) {
				content = content + line;
			}

			// not working!!
			if (!f.isDirectory()) {
				f.delete();
				// System.out.println("deleted");
			}

			File fileReceived = new File(newNode.selfName + "/" + name);
			if (!fileReceived.exists()) {
				fileReceived.createNewFile();
			}
			FileOutputStream fop = new FileOutputStream(fileReceived);
			byte[] contentBytes = content.getBytes();
			fop.write(contentBytes);
			fop.flush();
			fop.close();
			br.close();
		}
		
		newNode.filesInRange.put(key, al);
		
	}

	/**
	 * This method is used to check whether the ID is 
	 * in the range of the current chord node or not
	 * 
	 * @param id
	 * @return
	 */
	public boolean inRange(int id) {
		if (cNode.lowerBound > cNode.upperBound) {
			if (id < cNode.upperBound) {
				return true;
			} else if (id >= cNode.lowerBound) {
				return true;
			}
		} else if (id < cNode.upperBound && id >= cNode.lowerBound) {
			return true;
		}
		return false;
	}

	/**
	 * This method is used to assign the newly
	 * created object to the appropriate node 
	 * 
	 */
	@Override
	public void assignNewObject(ChordNode newNode) throws RemoteException {
		cNode = newNode;
	}

	/**
	 * This method is called whenever a node wants 
	 * to leave the chord network and redistributes 
	 * the files to its successor. After successful 
	 * distribution, the node exits the system.
	 *   
	 * @throws NotBoundException
	 * @throws IOException
	 */
	public void leave() throws NotBoundException, IOException {

		String predName = cNode.predeccesor.selfName;
		// System.out.println("Predecessor name is: " + predName);
		String predIP = cNode.predeccesor.selfIP;
		// System.out.println("Predecessor IP is: " + predIP);

		Registry reg = LocateRegistry.getRegistry(predIP, portNo);
		CommonInterface predObj = (CommonInterface) reg.lookup("chord");
		predObj.connected(cNode.selfName);
		
		

		String succName = cNode.successor.selfName;
		// System.out.println("Successor name is: " + succName);
		String succIP = cNode.successor.selfIP;
		// System.out.println("Successor IP is: " + succIP);

		
		// System.out.println("cNode.lower = " + cNode.lowerBound);
		
		predObj.setSuccessor(cNode.successor);

		
		Registry reg2 = LocateRegistry.getRegistry(succIP, portNo);
		CommonInterface succObj = (CommonInterface) reg2.lookup("chord");
		succObj.connected(cNode.selfName);
		succObj.setPredecessor(cNode.predeccesor, cNode.lowerBound);
		
		if (!cNode.filesInRange.isEmpty()) {
			// populate successor's fileInRange
			
			for(Map.Entry<Integer, ArrayList<String>> entry : cNode.filesInRange.entrySet()){
				Integer key = entry.getKey();
				// System.out.println("Key is: " + key);
				ArrayList<String> al = entry.getValue();
				// System.out.println("Value is: " + al);
				for(String name : al){
					File f = new File(cNode.selfName + "/" + name);
					FileInputStream fis = new FileInputStream(f);
					InputStream in = fis;
					BufferedReader br = new BufferedReader(
							new InputStreamReader(in));
					String content = "";
					String line;
					while ((line = br.readLine()) != null) {
						content = content + line;
					}
					// System.out.println("Content: " + content);
					
					if (!f.isDirectory()) {
						f.delete();
						// System.out.println("deleted");
					}
					br.close();
					succObj.populateFilesTable(cNode.filesInRange, content, name);
				}
			}
			// predObj.populateFilesTable(cNode.filesInRange, content);
		}
	
		cNode.successor = null;
		cNode.predeccesor = null;
		
		Registry reg3 = LocateRegistry.getRegistry(bootStrapIP, portNo);
		CommonInterface bootObj = (CommonInterface) reg3.lookup("bootstrap");
		bootObj.connected(selfName);
		bootObj.removeFromMap(selfName);
		System.exit(0);
	}

	/**
	 * This method is called whenever a file 
	 * search is requested and the optimal
	 * path to the node having the path is 
	 * chosen 
	 * 
	 */
	public void search(String fileName, String path, String selfIP)
			throws NoSuchAlgorithmException, RemoteException, NotBoundException {
		int identifier = computeHash(fileName);
		// System.out.println("File hash: " + identifier);
		if (path.equals("")) {
			path = path + cNode.selfName;
		} else {
			path = path + "\t" + cNode.selfName;
		}
		if (inRange(identifier)) {
			if (cNode.filesInRange.containsKey(identifier)) {
				ArrayList<String> al = cNode.filesInRange.get(identifier);
				if (al.contains(fileName)) {

					Registry reg = LocateRegistry.getRegistry(selfIP, portNo);
					CommonInterface initObj = (CommonInterface) reg
							.lookup("chord");
					initObj.printPath(path, cNode.selfName, "search");
				} else {
					// System.out.println("File not present.\nFailure 1	");
					System.out.println("al does not have filename");
					Registry reg = LocateRegistry.getRegistry(selfIP, portNo);
					CommonInterface initObj = (CommonInterface) reg
							.lookup("chord");
					initObj.printPath(path, cNode.selfName, "failure");
				}
			} else {
				// System.out.println("Path taken: " + path);
				// System.out.println("File not present.\nFailure 2");
				System.out.println("cNode does not have identifier");
				Registry reg = LocateRegistry.getRegistry(selfIP, portNo);
				CommonInterface initObj = (CommonInterface) reg
						.lookup("chord");
				initObj.printPath(path, cNode.selfName, "failure");
			}
		} else {

			// choose optimal path!!!!
		
			String IP = "";
			

			int diff = identifier - cNode.upperBound;
			int temp = 1000 - Math.abs(diff);
			
			if(temp < Math.abs(diff)){
				if(diff < 0){
					IP = cNode.successor.selfIP;
				}else{
					IP = cNode.predeccesor.selfIP;
				}
			}else{
				if(diff < 0){
					IP = cNode.predeccesor.selfIP;
				}else{
					IP = cNode.successor.selfIP;
				}
			}
			
			System.out.println("IP = " + IP);

			// String succName = cNode.successor.selfName;
			// System.out.println("successor's name : " + succName);
			// String succIP = cNode.successor.selfIP;
			// System.out.println("succ IP" + succIP);
			Registry reg = LocateRegistry.getRegistry(IP, portNo);
			CommonInterface succObj = (CommonInterface) reg.lookup("chord");
			succObj.search(fileName, path, selfIP);
		}
	}

	/**
	 * This method is called whenever the current
	 * status of the node is requested
	 * 
	 */
	private void view() {
		System.out.println("----------------------------------------------");
		System.out.println("IP Address: " + this.selfIP);
		System.out.println("ID: " + this.identifier);
		System.out.println("Name: " + selfName);
		System.out.println("Successor: " + cNode.successor.selfName);
		System.out.println("Predessdor: " + cNode.predeccesor.selfName);
		System.out.println("Files present:");
		for (Map.Entry<Integer, ArrayList<String>> entry : cNode.filesInRange
				.entrySet()) {
			System.out.println(entry.getValue());
		}
		System.out.println("Upper Bound: " + this.cNode.upperBound);
		System.out.println("Lower Bound: " + this.cNode.lowerBound);
		System.out.println("----------------------------------------------");
	}

	/**
	 * This method is called whenever a file is 
	 * supposed to be inserted at a node. The 
	 * optimal path is chosen
	 * 
	 */
	@Override
	public void insert(String fileName, File f, String path, String selfIP)
			throws NoSuchAlgorithmException, NotBoundException, IOException {
		int identifier = computeHash(fileName);
		
		System.out.println("File hash: " + identifier);
		
		if (path.equals("")) {
			path = path + cNode.selfName;
		} else {
			path = path + "\t" + cNode.selfName;
		}

		if (inRange(identifier)) {
			Registry reg = LocateRegistry.getRegistry(selfIP, portNo);
			CommonInterface initObj = (CommonInterface) reg.lookup("chord");
			initObj.printPath(path, selfName, "insert");
			File directory = new File(cNode.selfName);
			if (!directory.exists()) {
				directory.mkdir();
			}
			FileInputStream fis = new FileInputStream(f);
			InputStream in = fis;
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String content = "";
			String line;
			while ((line = br.readLine()) != null) {
				content = content + line;
			}
			File fileReceived = new File(selfName + "/" + fileName);
			if (!fileReceived.exists()) {
				fileReceived.createNewFile();
			}
			FileOutputStream fop = new FileOutputStream(fileReceived);
			byte[] contentBytes = content.getBytes();
			fop.write(contentBytes);
			fop.flush();
			fop.close();
			br.close();
			if (cNode.filesInRange.containsKey(identifier)) {
				ArrayList<String> al = cNode.filesInRange.get(identifier);
				al.add(fileName);
				cNode.filesInRange.put(identifier, al);
			} else {
				ArrayList<String> al = new ArrayList<String>();
				al.add(fileName);
				cNode.filesInRange.put(identifier, al);
			}
			// view();
		} else {

			String IP = "";
			
			
			int diff = identifier - cNode.upperBound;
			int temp = 1000 - Math.abs(diff);
			
			if(temp < Math.abs(diff)){
				if(diff < 0){
					IP = cNode.successor.selfIP;
				}else{
					IP = cNode.predeccesor.selfIP;
				}
			}else{
				if(diff < 0){
					IP = cNode.predeccesor.selfIP;
				}else{
					IP = cNode.successor.selfIP;
				}
			}	
			
			
			Registry reg = LocateRegistry.getRegistry(IP, portNo);
			CommonInterface obj = (CommonInterface) reg.lookup("chord");
			obj.connected(cNode.selfName);
			obj.insert(fileName, f, path, selfIP);
		}
	}
	/**
	 * This method is used to print the path
	 * of the file insertion & search along with
	 * appropriate messages
	 * 
	 */

	@Override
	public void connected(String name) throws RemoteException {
		System.out.println("Connected to: " + name);
	}

	@Override
	public String connectNodeToChord(String selfName, String selfIP)
			throws RemoteException {
		return null;
	}

	public int computeHash(String name) throws NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance("SHA-1");
		byte[] dataArray = name.getBytes();
		md.update(dataArray, 0, dataArray.length);
		BigInteger i = new BigInteger(1, md.digest());
		int x = i.intValue();
		x = Math.abs(x % 1000);
		return x;
	}

	@Override
	public void printPath(String path, String selfName, String type) throws RemoteException {
		if(type.equals("insert")){
			System.out.println("File inserted at: " + selfName);			
		}else if(type.equals("search")){
			System.out.println("File found at: " + selfName);
		}else if(type.equals("failure")){
			System.out.println("File not found");
		}
		System.out.println("Path taken: " + path);
	}
	/**
	 * This method is used to set the successor
	 * of a node
	 * 
	 */

	@Override
	public void setSuccessor(ChordNode newNode) throws RemoteException {
		// cNode.lowerBound = lowerBound;
		// System.out.println("successor's name issssss : " + cNode.selfName + " IP = " + cNode.selfIP);
		cNode.successor = newNode;
	}
	
	/**
	 * This method is used to set the predecessor 
	 * of a node along with its new lowerBound 
	 */


	@Override
	public void setPredecessor(ChordNode predeccesor, int lowerBound)
			throws RemoteException {
		cNode.predeccesor = predeccesor;
		cNode.lowerBound = lowerBound;
	}

	@Override
	public void removeFromMap(String selfName) throws RemoteException {

	}

	
	@Override
	public void populateFilesTable(ConcurrentHashMap<Integer, ArrayList<String>> filesInRange, String content, String fileName) throws RemoteException, IOException {
		
		File directory = new File(cNode.selfName);
		if (!directory.exists()) {
			directory.mkdir();
		}
		
		File fileReceived = new File(cNode.selfName + "/" + fileName);
		if (!fileReceived.exists()) {
			fileReceived.createNewFile();
		}
		FileOutputStream fop = new FileOutputStream(fileReceived);
		byte[] contentBytes = content.getBytes();
		fop.write(contentBytes);
		fop.flush();
		fop.close();

		for(Map.Entry<Integer, ArrayList<String>> entry : filesInRange.entrySet()){
			Integer key = entry.getKey();
			ArrayList<String> value = entry.getValue();
			cNode.filesInRange.put(key, value);
		}
	}

	/**
	 * This method is used to set the successor
	 * of a new node with is supposed to inserted
	 * 
	 */
	@Override
	public void setSuccessorNewNode(ChordNode newNode) throws RemoteException {
		cNode.successor = newNode;
	}

	/*@Override
	public void allocateFiles(ChordNode newNode) throws RemoteException,
			NotBoundException, FileNotFoundException, IOException {
		// TODO Auto-generated method stub
		
	}*/
}