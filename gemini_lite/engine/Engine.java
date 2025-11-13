package engine;

import java.io.IOException;

/**
 * Engine interface to encapsulate client engine implimentation through the use
 * of a state pattern
 */
public interface Engine {
    /**
     * Run the protocol engine
     * @throws IOException 
     */
    void run() throws IOException;
}
