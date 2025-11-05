package protocol;

import java.io.IOException;
/**
 * This class extendds the IOException class and handles protocol syntax errors
 */
public class ProtocolSyntaxException extends IOException{
    public ProtocolSyntaxException(String message){
        super(message);
    }
}
