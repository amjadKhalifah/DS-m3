package app_kvClient;

import java.io.IOException;

import logger.LogSetup;

import org.apache.log4j.Level;



public class KVApplication {

    public static void main(String[] args) {
    	
    	try {
			new LogSetup("logs/client.log", Level.ALL);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	new KVClient().startApplication();	
    }
	

}
