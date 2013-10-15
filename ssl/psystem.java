import java.util.ArrayList;
import java.io.*;
import java.net.*;
import java.security.*;
import javax.crypto.*;
import java.security.spec.*;
import java.nio.ByteBuffer;
import sun.misc.BASE64Encoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;



public class psystem {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, SignatureException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {

		KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
		keyGen.initialize(1024);
		KeyPair keyPair = keyGen.genKeyPair();
		PrivateKey privateKey = keyPair.getPrivate();
		PublicKey publicKey = keyPair.getPublic();	
		byte[] clientPKeyByte;
		PublicKey clientPKey;

		ServerSocket socket = new ServerSocket(10000);
		Socket connection = socket.accept();
		
		File file = new File(System.getProperty("user.dir") + "/item.txt");
		if (!file.exists()) {
			file.createNewFile();
		}
		
		FileReader fr;
		fr = new FileReader("item.txt");
		BufferedReader br;
		br = new BufferedReader(fr);
		String line;
		ArrayList<String> id = new ArrayList<String>();
		ArrayList<String> name = new ArrayList<String>();
		ArrayList<Integer> quantity = new ArrayList<Integer>();
		ArrayList<String> price = new ArrayList<String>();
		String[] splitString;
		
		while((line = br.readLine()) != null) {
			splitString = line.split("\\s+");
			id.add(splitString[0]);
			name.add(splitString[1]);
			quantity.add(Integer.parseInt(splitString[2]));
			price.add(splitString[3]);
		}
		br.close();
		
		DataOutputStream serverOut = new DataOutputStream(connection.getOutputStream());
		DataInputStream clientIn = new DataInputStream(connection.getInputStream());

		// Send customer public key and get customer public key
		ByteBuffer bb = ByteBuffer.allocate(4);
		bb.putInt(publicKey.getEncoded().length);
		serverOut.write(bb.array());
		serverOut.write(publicKey.getEncoded());
		serverOut.flush();
		
		byte[] lenb = new byte[4];
		clientIn.read(lenb, 0, 4);
		ByteBuffer bb2 = ByteBuffer.wrap(lenb);
		int len = bb2.getInt();
		clientPKeyByte = new byte[len];
		clientIn.read(clientPKeyByte);
		clientPKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(clientPKeyByte));


		// S2
		String toClient = "Item#, Item Name, Price\n------------------\n";
		for (int i = 0; i < id.size(); i++) {
			if (quantity.get(i) > 0) {
				toClient += id.get(i) + ", " + name.get(i) + ", " + price.get(i) + "\n";
			}
		}
		serverOut.writeUTF(toClient + "Please select an item: ");
		serverOut.flush();

		
		String clientID = clientIn.readUTF();
		String itemPrice = null;

		/* Decrement quantity */
		fr = new FileReader("item.txt");
		br = new BufferedReader(fr);
		StringBuilder fileContent = new StringBuilder();
		while ((line = br.readLine()) != null) {
			String tokens[] = line.split(" ");
			if (tokens.length > 0) {
				if (tokens[0].equals(clientID)) {
					tokens[2] = Integer.toString(Integer.parseInt(tokens[2]) - 1);
					itemPrice = tokens[3];
					String newLine = tokens[0] + " " + tokens[1] + " " + tokens[2] + " " + tokens[3];
					fileContent.append(newLine);
					fileContent.append("\n");
				}
				else {
					fileContent.append(line);
					fileContent.append("\n");
				}
			}
		}
		FileWriter fstreamWrite = new FileWriter(System.getProperty("user.dir") + "/item.txt");
		BufferedWriter out = new BufferedWriter(fstreamWrite);
		out.write(fileContent.toString());
		out.close();
		br.close();
				
		// S4
		serverOut.writeUTF("Please enter your name: ");
		serverOut.flush();

		// S5
		String clientName;
		clientName = clientIn.readUTF();

		// S6
		serverOut.writeUTF("Please enter credit card number: ");
		serverOut.flush();

		// S7
		byte[] lenb2 = new byte[4];
		byte[] lenb3 = new byte[4];
		clientIn.read(lenb2, 0, 4);
		clientIn.read(lenb3, 0, 4);
		ByteBuffer bb3 = ByteBuffer.wrap(lenb2);
		ByteBuffer bb4 = ByteBuffer.wrap(lenb3);
		int len2 = bb3.getInt();
		int len3 = bb4.getInt();
		byte[] clientCCNumber = new byte[len2];
		byte[] preCCNumber = new byte[len3];
		clientIn.read(clientCCNumber);
		clientIn.read(preCCNumber);

		// S8
		Signature sig = Signature.getInstance("SHA1withRSA");
		sig.initVerify(clientPKey);
		sig.update(preCCNumber);
		boolean verifies = sig.verify(clientCCNumber);
		System.out.println("Signature verifies: " + verifies);
		if (verifies) {

			// Decrypt encrypted CC number
			Cipher cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.DECRYPT_MODE, privateKey);
			byte[] decryptedText = cipher.doFinal(preCCNumber);
			String creditCard = new String(decryptedText);

			String receipt = clientName + " " + clientID + " " + itemPrice;
			// write to activity.txt
			PrintWriter outStream = new PrintWriter(new BufferedWriter(new FileWriter("activity.txt", true)));
			DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
			outStream.println(clientName + " " + df.format(new Date()) + " " + itemPrice);
			outStream.close();

			// encrypt receipt and send to customer
			cipher.init(Cipher.ENCRYPT_MODE, clientPKey);
			byte[] cipherText = cipher.doFinal(receipt.getBytes());
			ByteBuffer bb5 = ByteBuffer.allocate(4);
			bb5.putInt(cipherText.length);
			serverOut.write(bb5.array());
			serverOut.write(cipherText);
			serverOut.flush();
		 

		}	
		else {
			System.out.println("Signature not verified");
		}
		connection.close();
		socket.close();	
		serverOut.close();
		clientIn.close();
		System.out.println("Server: connection closed");
	}

}