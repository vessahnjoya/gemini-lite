package engine;

import java.io.IOException;
import java.net.UnknownHostException;

/**
 * Engine interface to encapsulate client engine implimentation through the use
 * of a state pattern
 */
public interface Engine {
    /**
     * Run the protocol engine
     * 
     * @throws IOException
     * @throws UnknownHostException
     */
    void run();
}
