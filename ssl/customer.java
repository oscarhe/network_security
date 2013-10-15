import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.security.*;
import javax.crypto.*;
import java.security.spec.*;
import java.nio.ByteBuffer;
import sun.misc.BASE64Encoder;


public class customer {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws UnknownHostException 
	 */
	public static void main(String[] args) throws UnknownHostException, IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException, InvalidKeyException,
												IllegalBlockSizeException, BadPaddingException, SignatureException {

		KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
		keyGen.initialize(1024);
		KeyPair keyPair = keyGen.genKeyPair();
		PrivateKey privateKey = keyPair.getPrivate();
		PublicKey publicKey = keyPair.getPublic();	
		byte[] serverPKeyByte;
		PublicKey serverPKey;

		Socket connection = new Socket("localhost", 10000);
		

		DataInputStream serverIn = new DataInputStream(connection.getInputStream());
		DataOutputStream clientOut = new DataOutputStream(connection.getOutputStream());
		
		// Send server public key and get system public key
		ByteBuffer bb = ByteBuffer.allocate(4);
		bb.putInt(publicKey.getEncoded().length);
		clientOut.write(bb.array());
		clientOut.write(publicKey.getEncoded());
		clientOut.flush();
		
		byte[] lenb = new byte[4];
		serverIn.read(lenb, 0, 4);
		ByteBuffer bb2 = ByteBuffer.wrap(lenb);
		int len = bb2.getInt();
		serverPKeyByte = new byte[len];
		serverIn.read(serverPKeyByte);
		serverPKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(serverPKeyByte));


		String line, userInput;
	
		line = serverIn.readUTF();
		System.out.println(line);
		
		BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
		userInput = stdIn.readLine();
		
		clientOut.writeUTF(userInput);
		clientOut.flush();

		// S5
		line = serverIn.readUTF();
		System.out.println(line);
		userInput = stdIn.readLine();
		clientOut.writeUTF(userInput);
		clientOut.flush();

		
		// S6 & S7
		line = serverIn.readUTF();
		System.out.println(line);
		userInput = stdIn.readLine();
		byte[] cipherText1 = null;
		byte[] cipherText2 = null;
		Cipher cipher  = Cipher.getInstance("RSA");
		// encrypt using public key of purchasing system
		cipher.init(Cipher.ENCRYPT_MODE, serverPKey);
		cipherText1 = cipher.doFinal(userInput.getBytes());
		
		// encrypt using private key with SHA1
		Signature sig = Signature.getInstance("SHA1withRSA");
		sig.initSign(privateKey);
		sig.update((cipherText1));
		cipherText2 = sig.sign();		

		//System.out.println("CC#: " + userInput);
		ByteBuffer bb5 = ByteBuffer.allocate(4);
		ByteBuffer bb6 = ByteBuffer.allocate(4);
		bb5.putInt(cipherText2.length);
		bb6.putInt(cipherText1.length);
		clientOut.write(bb5.array());
		clientOut.write(bb6.array());
		clientOut.write(cipherText2);
		clientOut.write(cipherText1);
		clientOut.flush();

		// Get receipt and decrypt
		byte[] receipt;
		byte[] lenb2 = new byte[4];
		serverIn.read(lenb2, 0, 4);
		ByteBuffer bb3 = ByteBuffer.wrap(lenb2);
		int len2 = bb3.getInt();
		receipt = new byte[len2];
		serverIn.read(receipt);
		cipher.init(Cipher.DECRYPT_MODE, privateKey);
		byte[] decryptedReceipt = cipher.doFinal(receipt);
		String strReceipt = new String(decryptedReceipt);

		System.out.println("The receipt: " + strReceipt);
		
		serverIn.close();
		clientOut.close();
		connection.close();
		System.out.println("Client: connection closed");

	}

}
