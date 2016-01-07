package parser;

public class InvalidArgumentException extends Exception {
	public InvalidArgumentException(){}
	
	public InvalidArgumentException(String message){
		super(message);
	}

}
