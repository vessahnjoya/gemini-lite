package engine;

import java.io.IOException;
import java.net.UnknownHostException;

public interface Engine {
    /**
     * Run the protocol engine
     * 
     * @throws IOException
     * @throws UnknownHostException
     */
    void run() throws UnknownHostException, IOException;
}
