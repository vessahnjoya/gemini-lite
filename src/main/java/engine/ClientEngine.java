package engine;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import protocol.*;

/**
 * Thic class implements the Engine interface, and contains the running logic of
 * the client.
 */
public class ClientEngine implements Engine {
    // Variable holding reference to the URI and constants respecrively
    private final URI uri;
    private final int MAX_REDIRECTS = 5;
    private final int DEFAULT_PORT = 1958;
    private Socket socket;
    private InputStream in;
    private OutputStream out;

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

    public ClientEngine(Socket socket, URI uri) throws IOException {
        this.socket = socket;
        this.uri = uri;
        in = socket.getInputStream();
        out = socket.getOutputStream();
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
    private int getPort(URI current) {
        if (current.getPort() == -1) {
            return DEFAULT_PORT;
        }
        return current.getPort();
    }

    /**
     * Helpher mehod to get host name
     * 
     * @return host name
     */
    private String getHost(URI current) {
        return current.getHost();
    }

    @Override
    public void run() throws IOException {
        if (socket == null) {
            var host = getHost(uri);
            var port = getPort(uri);
            socket = new Socket(host, port);
            in = socket.getInputStream();
            out = socket.getOutputStream();
        }

        runWithRedirect(uri, 0);
    }

    private void runWithRedirect(URI current, int count) throws IOException {
        if (count > MAX_REDIRECTS) {
            System.err.println("Maximum redirects amount reached");
            System.exit(1);
        }

        if (count == 0 && socket != null) {
            try ( var currentSocket = socket;
            var currentIn = currentSocket.getInputStream();
            var currentOut = currentSocket.getOutputStream();) {
                processRequest(current, currentIn, currentOut, count);
            }
        } else{
            try (var currentSocket = new Socket(getHost(current), getPort(current));
            var currentIn = currentSocket.getInputStream();
            var currentOut = currentSocket.getOutputStream();) {
                processRequest(current, currentIn, currentOut, count);
            }
        }

    }

    private void HandleRedirect(URI current, int count, String meta) throws IOException {
        URI target;
        try {
            target = new URI(meta);
            if (!target.isAbsolute()) {
                target = current.resolve(meta);
            }
        } catch (URISyntaxException e) {
            System.err.println("Invalid redirecting URL: " + meta);
            System.exit(1);
            return;
        }

        if (!"gemini-lite".equalsIgnoreCase(target.getScheme())) {
            System.err.println("Non-Gemini redirect not supported: " + target);
            System.exit(1);
            return;
        }

        runWithRedirect(target, count + 1);
    }

    private void processRequest(URI current, InputStream in, OutputStream out, int count) throws IOException{
        Request request = new Request(current);
            request.format(out);

            Reply reply = Reply.parse(in);

            if (reply.getStatusCode() == 20) {
                if (reply.hasBody()) {
                    reply.relayBody(System.out);
                } else {
                    byte[] buffer = new byte[1024];
                    int read;
                    while ((read = in.read(buffer)) != -1) {
                        System.out.write(buffer, 0, read);
                    }
                }
                System.out.flush();
                System.exit(0);
            } else if (reply.getStatusCode() >= 30 && reply.getStatusCode() < 40) {
                HandleRedirect(current, count, reply.getMeta().trim());
            } else {
                System.out.flush();
                System.exit(50);
            }
    }
}
