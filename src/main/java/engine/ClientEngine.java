package engine;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;

import protocol.ProtocolSyntaxException;
import protocol.Reply;
import protocol.Request;

public class ClientEngine implements Engine {
    private final URI uri;
    private final int MAX_REDIRECTS = 5;
    private final int DEFAULT_PORT = 1958;
    private Socket socket;
    private String userInput;
    private static final String URI_SCHEME = "gemini-lite";

    public ClientEngine(URI uri) {
        this.uri = uri;
    }

    public ClientEngine(URI uri, String userInput) {
        this.uri = uri;
        this.userInput = userInput;
    }

    public ClientEngine(Socket socket, URI uri) throws IOException {
        this.socket = socket;
        this.uri = uri;
    }

    @Override
    public void run() throws IOException {

        if (socket == null) {
            socket = new Socket(uri.getHost(), (uri.getPort() != -1 ? uri.getPort() : DEFAULT_PORT));
        }

        runWithRedirect(uri, 0);
    }

    private void runWithRedirect(URI current, int count) throws IOException {
        if (count > MAX_REDIRECTS) {
            System.err.println("Maximum redirects amount reached");
            System.exit(1);
        }

        if (count == 0 && socket != null) {
            try (var currentSocket = socket;
                    var currentIn = currentSocket.getInputStream();
                    var currentOut = currentSocket.getOutputStream();) {
                processRequest(current, currentIn, currentOut, count);
            }
        } else {
            try (var currentSocket = new Socket(current.getHost(), current.getPort());
                    var currentIn = currentSocket.getInputStream();
                    var currentOut = currentSocket.getOutputStream();) {
                processRequest(current, currentIn, currentOut, count);
            }
        }

    }

    private void handleRedirect(URI current, int count, String meta) throws IOException {
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

        if (!URI_SCHEME.equalsIgnoreCase(target.getScheme())) {
            System.err.println("Non-Gemini redirect not supported: " + target);
            System.exit(1);
            return;
        }

        runWithRedirect(target, count + 1);
    }

    private void processRequest(URI current, InputStream in, OutputStream out, int count) throws IOException {

        try {
            var validateOut = new ByteArrayOutputStream();
            Request tempRequest = new Request(current);
            tempRequest.format(validateOut);

            var validateIn = new ByteArrayInputStream(validateOut.toByteArray());

            Request request = Request.parse(validateIn);

            request.format(out);

        } catch (ProtocolSyntaxException | URISyntaxException e) {
            System.err.println("Invalid request: " + e.getMessage());
            System.exit(1);
        }

        Reply reply;
        try {
            reply = Reply.parse(in);
        } catch (ProtocolSyntaxException e) {
            System.err.println("invalid reply: " + e.getMessage());
            System.out.flush();
            System.exit(1);
            return;
        }

        if (reply.getStatusCode() < 10 || reply.getStatusCode() > 59) {
            System.err.println("Unsupported codes");
            System.out.flush();
            System.exit(1);
            return;
        }

        if (reply.getStatusCode() >= 10 && reply.getStatusCode() < 20) {
            if (userInput != null) {
                URI newUri;
                try {
                    newUri = utils.URIutils.buildNewURI(current, userInput);
                    System.err.println("New uri: " + newUri);
                } catch (URISyntaxException e) {
                    System.err.println("invalid query");
                    System.exit(1);
                    return;
                }
                runWithRedirect(newUri, count + 1);
                return;
            }
        }

        if (reply.getStatusCode() == 20) {
            in.transferTo(System.out);
            System.out.flush();
            System.exit(0);
        }

        if (reply.getStatusCode() != 20 && in.available() > 0) {
            System.err.println("Non success responses should not contain bodies!");
            System.out.flush();
            System.exit(1);
            return;
        }

        if (reply.getStatusCode() >= 30 && reply.getStatusCode() < 40) {
            handleRedirect(current, count, reply.getMeta().trim());
        }
        if (reply.getStatusCode() >= 40 && reply.getStatusCode() < 50) {
            if (in.available() > 0 && reply.getStatusCode() != 44) {
                System.err.println("Temporary failures should not contain bodies");
                System.out.flush();
                System.exit(1);
            }
            if (reply.getStatusCode() == 44) {
                int seconds;
                try {
                    seconds =  Integer.parseInt(reply.getMeta());
                } catch (NumberFormatException e) {
                    seconds = 1;
                }
                try {
                    Thread.sleep(seconds * 1000L);
                } catch (InterruptedException e) {
                }


                runWithRedirect(current, 0);
                return;

            }
            System.out.flush();
            System.exit(reply.getStatusCode());
        }

        if (reply.getStatusCode() >= 50 && reply.getStatusCode() < 60) {
            System.out.flush();
            System.exit(reply.getStatusCode());
        }
        System.out.flush();
        System.exit(1);
    }
}
