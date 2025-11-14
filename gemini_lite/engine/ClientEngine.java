package engine;

import java.io.IOException;
import java.net.*;

import protocol.*;

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

        if (hasUserInfo()) {
            System.err.println(" Invalid URI, URI should not contain UserInfo!");
            System.exit(1);
        }
    }

    /**
     * Helpher method to check whether or not the URI contains userinfo, to complY
     * with gemini specification
     *
     * @return a boolean
     */
    private boolean hasUserInfo() {
        return uri.getUserInfo() != null;
    }

    /**
     * Helper method to get port. If not stated, returns tHe default port 1958
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
    public void run() throws IOException {

        try (var socket = new Socket(getHost(), getPort())) {
            final var i = socket.getInputStream();
            final var o = socket.getOutputStream();

            var request = new Request(uri.toString());
            request.format(o);

            var reply = Reply.parser(i);

            System.out.print(reply.getStatusCode() + " " + reply.getMeta() + "\r\n");

            if (reply.getStatusCode() == 20) {
                byte[] buffer = new byte[1024];
                int read;
                while ((read = i.read(buffer)) != -1) {
                    System.out.write(buffer, 0, read);
                }
                System.out.print("\r\n");

                System.out.flush();
                System.exit(0);

            } else if (reply.getStatusCode() >= 30 && reply.getStatusCode() < 40) {
                System.out.print(reply.getMeta());
                System.out.flush();
                System.exit(0);
            } else {
                System.out.flush();
                System.exit(1);
            }

        } catch (UnknownHostException e) {
            System.err.println("Invalid HostName: " + e.getMessage());
            System.exit(1);
        }
    }
}
