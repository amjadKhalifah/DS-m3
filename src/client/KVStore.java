package client;


import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Observable;

import logger.LogSetup;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import common.messages.KVMessage;
import common.messages.Message;

public class KVStore implements KVCommInterface {

	private Logger logger = Logger.getRootLogger();		
	private String address;
	private int port;
	
	private Socket clientSocket;
	private OutputStream output;
 	private InputStream input;
 	
 	private static final int BUFFER_SIZE = 1024;
	private static final int DROP_SIZE = 1024 * BUFFER_SIZE;
		
	/**
	 * Initialize KVStore with address and port of KVServer
	 * @param address the address of the KVServer
	 * @param port the port of the KVServer
	 */
	public KVStore(String address, int port) {
		this.address = address;
		this.port = port;
	}
	
	@Override
	public void connect() throws Exception {
		try {
			clientSocket = new Socket(address, port);								
			output = clientSocket.getOutputStream();
			input = clientSocket.getInputStream();					
			logger.info("Connection established with server");			
		} catch (IOException ioe) {
			logger.error("Connection could not be established!");	
			throw ioe;
		} 
	}

	@Override
	public void disconnect() {			
		// notify the observers that the connection closed
		try {
			tearDownConnection();			
		} catch (IOException ioe) {
			logger.error("Unable to close connection!");
		}
	}
	
	private void tearDownConnection() throws IOException {		
		logger.info("tearing down the connection ...");
		if (clientSocket != null) {
			input.close();
			output.close();
			clientSocket.close();
			clientSocket = null;
			logger.info("connection closed!");
		}
	}
		

	@Override
	public KVMessage put(String key, String value) throws Exception {
		Message msg= new Message();
		KVMessage receivedMsg = null;
		msg.setKey(key);
		msg.setValue(value);
		msg.setStatus(KVMessage.StatusType.PUT);
		try{
			this.sendMessage(msg);
			receivedMsg = this.receiveMessage();
			
		}catch ( IOException e){
			logger.error("");
			throw e;
		}		
		return receivedMsg;
	}

	@Override
	public KVMessage get(String key) throws Exception {		
		Message msg= new Message();
		KVMessage receivedMsg = null;
		msg.setKey(key);		
		msg.setStatus(KVMessage.StatusType.GET);
		try{
			this.sendMessage(msg);
			receivedMsg = this.receiveMessage();
			
		}catch ( IOException e){
			logger.error("");
		}		
		return receivedMsg;
	}
	
	
	
	private KVMessage receiveMessage() throws IOException {			
		int index = 0;
		byte[] msgBytes = null, tmp = null;
		byte[] bufferBytes = new byte[BUFFER_SIZE];
		
		/* read first char from stream */
		byte read = (byte) input.read();	
		boolean reading = true;
		
		while(read != 13 && reading) {/* carriage return */
			/* if buffer filled, copy to msg array */
			if(index == BUFFER_SIZE) {
				if(msgBytes == null){
					tmp = new byte[BUFFER_SIZE];
					System.arraycopy(bufferBytes, 0, tmp, 0, BUFFER_SIZE);
				} else {
					tmp = new byte[msgBytes.length + BUFFER_SIZE];
					System.arraycopy(msgBytes, 0, tmp, 0, msgBytes.length);
					System.arraycopy(bufferBytes, 0, tmp, msgBytes.length,
							BUFFER_SIZE);
				}

				msgBytes = tmp;
				bufferBytes = new byte[BUFFER_SIZE];
				index = 0;
			} 
			
			/* only read valid characters, i.e. letters and numbers */
			if((read > 31 && read < 127)) {
				bufferBytes[index] = read;
				index++;
			}
			
			/* stop reading is DROP_SIZE is reached */
			if(msgBytes != null && msgBytes.length + index >= DROP_SIZE) {
				reading = false;
			}
			
			/* read next char from stream */
			read = (byte) input.read();
		}
		
		if(msgBytes == null){
			tmp = new byte[index];
			System.arraycopy(bufferBytes, 0, tmp, 0, index);
		} else {
			tmp = new byte[msgBytes.length + index];
			System.arraycopy(msgBytes, 0, tmp, 0, msgBytes.length);
			System.arraycopy(bufferBytes, 0, tmp, msgBytes.length, index);
		}
		
		msgBytes = tmp;

			
			/* build final String */
			KVMessage msg = SerializationUtil.toObject(msgBytes);
			logger.info("Receive message:\t '" + msg.getKey() + "'");
			return msg;
	    }
	
	
private void sendMessage(KVMessage msg) throws IOException {
	byte[] msgBytes = SerializationUtil.toByteArray(msg);
	output.write(msgBytes, 0, msgBytes.length);
	output.flush();
	logger.info("Send message :\t '" + msg.getKey() + "'");
}
	
}
