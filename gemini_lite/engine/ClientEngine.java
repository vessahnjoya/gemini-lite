package engine;

import java.io.IOException;
import java.net.*;

import protocol.Request;
/**
 * Thic class implements the Engine interface, and contains the running logic of the client.
 */
public class ClientEngine implements Engine {
    // Variable holding reference to the URI
    private final URI uri;

    /**
     * Constructor to initialize URI
     * 
     * @param uri
     */
    public ClientEngine(URI uri) {
        this.uri = uri;

        if (!validateURI()) {
            System.err.println(" Invalid URI, URI should not contain UserInfo!");
        }

    }

    /**
     * Helpher method to check whether or not the URI contains userinfo, to complu
     * with gemini specification
     * 
     * @return a boolean
     */
    private boolean validateURI() {
        if (uri.getUserInfo().isEmpty()) {
            return true;
        }
        return false;
    }

    /**
     * Helper method to get port. If not stated, returns te default port 1965
     * 
     * @return port number
     */
    private int getPort() {
        if (uri.getPort() == -1) {
            return 1965;
        }
        return uri.getPort();
    }

    /**
     * Helpher mehod to get host name
     * 
     * @return host name
     */
    private String getHost() {
        return uri.getHost();
    }

    @Override
    public void run() {
        // TODO: IMPLEMENT THE CLIENT LOGIC (LAZY BUT HAVE TO PUSH AS TONY SAID)

        try (var socket = new Socket(getHost(), getPort())) {
            // out.println(uri.toString());x
            final var i = socket.getInputStream();
            final var o = socket.getOutputStream();

            var request = new Request(uri.toString());
            request.format(o);
            System.out.println("works");
        } catch (UnknownHostException e) {
            System.err.println("Hostname does not EXIST: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Invalid input: " + e.getMessage());
        }
    }
}
