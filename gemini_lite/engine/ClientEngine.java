package engine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.nio.charset.StandardCharsets;

import protocol.Reply;
import protocol.Request;

/**
 * Thic class implements the Engine interface, and contains the running logic of
 * the client.
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

        // if (validateURI()) {
        //     System.err.println(" Invalid URI, URI should not contain UserInfo!");
        // }
    }

    // /**
    //  * Helpher method to check whether or not the URI contains userinfo, to complu
    //  * with gemini specification
    //  * 
    //  * @return a boolean
    //  */
    // private boolean validateURI() {
    //     if (!uri.getUserInfo().isEmpty()) {
    //         return true;
    //     }
    //     return false;
    // }

    /**
     * Helper method to get port. If not stated, returns te default port 1965
     * 
     * @return port number
     */
    private int getPort() {
        if (uri.getPort() == -1) {
            return 1958;
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

            var reply = Reply.parser(i);
            reply.format(o);
            System.out.printf("%d %s%n", reply.getStatusCode(), reply.getMeta());

            if (reply.getStatusCode()>=20) {
                var reader = new BufferedReader(new InputStreamReader(i, StandardCharsets.UTF_8));
                String line = reader.readLine();
                while (line != null) {
                    System.out.println(line);
                }
                System.exit(0);
            }else{
                System.err.println("Request not successful: " + reply.getStatusCode());
            }
        } catch (UnknownHostException e) {
            System.err.println("Hostname does not EXIST: " + e.getMessage());
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Invalid input: " + e.getMessage());
            System.exit(1);
        }
    }
}
