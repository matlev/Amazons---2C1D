import java.util.ArrayList;
import java.util.Enumeration;

import ubco.ai.GameRoom;
import ubco.ai.connection.ServerMessage;
import ubco.ai.games.GameClient;
import ubco.ai.games.GameMessage;
import ubco.ai.games.GamePlayer;

import net.n3.nanoxml.IXMLElement;

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
	private GameRoom currentRoom = null;
	
	private boolean isPlayerA = false;
	private boolean gameStarted = false;
	
	private int roomID = -1;
	
	public String usrName = null;
	
	public String[] moves = null;
	
	
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
    	
    	//Join room
    	currentRoom = gameClient.roomList.get(0);
    	roomID = currentRoom.roomID;
    	gameClient.joinGameRoom(currentRoom.roomName);  
	}
	
	//general message from the server
	public boolean handleMessage(String msg) throws Exception {
		System.out.println("timeout: " + msg);
		return true;
	}

	//game-specific message from the server
	public boolean handleMessage(GameMessage msg) {
		IXMLElement xml = ServerMessage.parseMessage(msg.msg);
		String type = xml.getAttribute("type","Wrong!"); 
		System.out.println(msg);
		
		if(type.equals(GameMessage.ACTION_ROOM_JOINED)){
			onJoinRoom(xml);
		}
		else if (type.equals(GameMessage.ACTION_GAME_START)){
			Gameboard ai_board = new Gameboard();
			AI ai = null;

			this.gameStarted = true;
			
			IXMLElement usrlist = xml.getFirstChildNamed("usrlist");
			int ucount = usrlist.getAttribute("ucount",-1);
			
			Enumeration ch = usrlist.enumerateChildren();
			while(ch.hasMoreElements()){
				System.out.println("game start");
				
				IXMLElement usr = (IXMLElement) ch.nextElement();
				int id = usr.getAttribute("id", -1);
				String name = usr.getAttribute("name","nnn");
				
				if(!name.equalsIgnoreCase(usrName)){
					continue;
				}
				
				String role = usr.getAttribute("role","W");
				if(role.equalsIgnoreCase("W")){
					isPlayerA = true;
					ai = new AI(ai_board, 1);
					moves = ai.search();

					sendToServer(moves[0] ,moves[1] ,roomID);		
				}
				else{
					isPlayerA = false;
					ai = new AI(ai_board, 2);
				}
			}
			System.out.println("Game Start: " + msg.msg);
		}
		else if(type.equals(GameMessage.ACTION_MOVE)){
			this.handleOpponentMove(xml);
		}
		return true;
	}
	
	private void onJoinRoom(IXMLElement xml){
		IXMLElement usrlist = xml.getFirstChildNamed("usrlist");
		int ucount = usrlist.getAttribute("ucount", -1);
		
		Enumeration ch = usrlist.enumerateChildren();
		while(ch.hasMoreElements()){
			IXMLElement usr = (IXMLElement) ch.nextElement();
			int id = usr.getAttribute("id", -1);
			String name = usr.getAttribute("name","NO!");
		}
	}
	
	private void handleOpponentMove(IXMLElement xml){
		System.out.println("Opp Move");
		if(!gameStarted){
			return;
		}
		
		IXMLElement c1 = xml.getFirstChildNamed("queen");
		String qmove = c1.getAttribute("move", "default");
		
		String[] queenMove = qmove.split("-");
		
		IXMLElement c2 = xml.getFirstChildNamed("arrow");
		String amove = c2.getAttribute("move", "default");
		
		ai.playMove(queenMove[0], queenMove[1], amove);
		moves = ai.search();
		
		sendToServer(moves[0], moves[1],roomID);
	}

    // You may want to implement a method like this as a central point for sending messages 
	// to the server.  
	public void sendToServer(String qMove, String arrow, int roomID){
	  // before sending the message to the server, you need to (1) build the text of the message 
	  // as a string,  (2) compile the message by calling 
	  // the static method ServerMessage.compileGameMessage(msgType, roomID, actionMsg),
	  // and (3) call the method gameClient.sendToServer() to send the compiled message.
		
	  // For message types and message format, see the GameMessage API and the project notes
		
		String actionmessage = "<action type ='move'><queen move='" + qMove + "'></queen>";
		actionmessage = actionmessage + "<arrow move='" + arrow + "'></arrow></action>";
		
		System.out.println(actionmessage);
		String msg = ServerMessage.compileGameMessage(GameMessage.MSG_GAME, roomID, actionmessage);
		gameClient.sendToServer(msg, true);
	}
	
	public static void main(String[] args){
		
		SimplePlayer sp = new SimplePlayer("kk","her");
	}
}
