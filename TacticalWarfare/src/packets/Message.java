package packets;

public class Message {
	
	String text;
	int id;

	public Message(){
		
	}
	
	public Message(String newtext){
		text = newtext;
	}
	
	public Message(String newtext, int newid){
		text = newtext;
		id = newid;
	}
	
}
