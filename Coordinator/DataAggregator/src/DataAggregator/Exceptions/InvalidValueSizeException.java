package DataAggregator.Exceptions;

/**
 * Created by Zobair on 12-11-2015.
 */
public class InvalidValueSizeException extends Exception {


    /**
     * Initializes a new instance of the InvalidValueSizeException class.
     */
   public InvalidValueSizeException(){}

    /**
     * Initializes a new instance of the InvalidValueSizeException class with a message.
     * @param msg is the message that has to be sent through.
     */
   public InvalidValueSizeException(String msg){
       super(msg);
   }

}
