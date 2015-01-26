import java.util.ArrayList;

import ubco.ai.GameRoom;
import ubco.ai.connection.ServerMessage;
import ubco.ai.games.GameClient;
import ubco.ai.games.GameMessage;
import ubco.ai.games.GamePlayer;

/**
 * 
 * Illustrate how to write a player. A player has to implement the GamePlayer interface.  
 * 
 * @author yonggao
 *
 */

public class SimplePlayer implements GamePlayer {

	GameClient gameClient = null; 
	ArrayList<GameRoom> roomlist;
	
	/*
	 * Constructor 
	 */
	public SimplePlayer(String name, String passwd) {
		
		//A player has to maintain an instance of GameClient, and register itself with the  
		//GameClient. Whenever there is a message from the server, the Gameclient will invoke 
		//the player's handleMessage() method.
		//Three arguments: user name (any), passwd (any), this (delegate)   
		gameClient = new GameClient(name,passwd,this);
    	roomlist = gameClient.getRoomLists();

    	//Print out the room list to user
    	for(GameRoom gr : roomlist){
    		System.out.println(gr.toString());
    		System.out.println("With: " + gr.userCount + " users");
    	}
		
	    
	    
	    
	}
	
	//general message from the server
	public boolean handleMessage(String arg0) throws Exception {
 
		return true;
	}

	//game-specific message from the server
	public boolean handleMessage(GameMessage arg0) throws Exception {
		System.out.println("[SimplePlayer: The server said =]  " + arg0.toString()); 
		return true;
	}

    // You may want to implement a method like this as a central point for sending messages 
	// to the server.  
	public void sendToServer(String msgType, int roomID){
	  // before sending the message to the server, you need to (1) build the text of the message 
	  // as a string,  (2) compile the message by calling 
	  // the static method ServerMessage.compileGameMessage(msgType, roomID, actionMsg),
	  // and (3) call the method gameClient.sendToServer() to send the compiled message.
		
	  // For message types and message format, see the GameMessage API and the project notes	
	}
	
	public static void main(String[] args){
		
		SimplePlayer sp = new SimplePlayer("kk","her");
	}
}
