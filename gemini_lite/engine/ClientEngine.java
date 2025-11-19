package engine;

import java.io.IOException;
import java.io.InputStream;
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

    private void reader(InputStream i) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = i.read(buffer)) != -1) {
            System.out.write(buffer, 0, read);
        }
        System.out.print("\r\n");
    }

    @Override
    public void run() throws IOException {
        runWithRedirect(uri, 0);
    }

    private void runWithRedirect(URI current, int count) throws IOException {
        if (count > MAX_REDIRECTS) {
            System.err.println("Maximum redirects amount reached");
            System.exit(1);
        }
        try (var socket = new Socket(getHost(current), getPort(current))) {
            final var i = socket.getInputStream();
            final var o = socket.getOutputStream();

            var request = new Request(current.toString());
            request.format(o);

            var reply = Reply.parse(i);

            System.out.print(reply.getStatusCode() + " " + reply.getMeta() + "\r\n");

            if (reply.getStatusCode() == 20) {
                reader(i);
                System.out.flush();
                System.exit(0);
            } else if (reply.getStatusCode() >= 30 && reply.getStatusCode() < 40) {
                HandleRedirect(current, count, reply.getMeta().trim());
            } else {
                System.out.flush();
                System.exit(1);
            }

        } catch (UnknownHostException e) {
            System.err.println("Invalid HostName: " + e.getMessage());
            System.exit(1);
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
}
