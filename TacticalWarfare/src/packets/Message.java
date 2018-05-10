package packets;

public class Message {
	
	private String text;
	private int value;

	public Message(){
		
	}
	
	public Message(String newtext){
		text = newtext;
	}
	
	public Message(String newtext, int newvalue){
		text = newtext;
		value = newvalue;
	}
	
	public String getText() {
		return text;
	}
	
	public int getValue() {
		return value;
	}
	
}
