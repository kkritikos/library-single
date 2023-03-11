package gr.aegean.book.exception;

public class InternalServerErrorException extends Exception{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public InternalServerErrorException() {
		super();
	}
	
	public InternalServerErrorException(String message) {
		super(message);
	}
}
