package engine;

import java.io.*;
import java.net.*;
import protocol.*;

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

    private int getPort(URI current) {
        if (current.getPort() == -1) {
            return DEFAULT_PORT;
        }
        return current.getPort();
    }

    private String getHost(URI current) {
        return current.getHost();
    }

    @Override
    public void run() throws IOException {

        if (socket == null) {
            var host = getHost(uri);
            var port = getPort(uri);
            socket = new Socket(host, port);
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
            try (var currentSocket = new Socket(getHost(current), getPort(current));
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
            Request  tempRequest = new Request(current);
            tempRequest.format(validateOut);

            var validateIn = new ByteArrayInputStream(validateOut.toByteArray());

            Request request = Request.parse(validateIn);

            request.format(out);

        } catch (ProtocolSyntaxException | URISyntaxException e) {
            System.err.println("Invalid request: " + e.getMessage());
            System.exit(1);
        }

        Reply reply = Reply.parse(in);
        if (reply.getStatusCode() == 20) {
            in.transferTo(System.out);
            System.out.flush();
            System.exit(0);
        } else if (reply.getStatusCode() >= 30 && reply.getStatusCode() < 40) {
            handleRedirect(current, count, reply.getMeta().trim());
        } else if (reply.getStatusCode() >= 10 && reply.getStatusCode() < 20) {
            if (userInput != null) {
                URI newUri;
                try {
                    newUri = utils.URIutils.buildNewURI(current, userInput);
                    System.err.println("New uri: " + newUri);
                } catch (Exception e) {
                    System.err.println("invalid query");
                    System.exit(1);
                    return;
                }
                runWithRedirect(newUri, count + 1);
                return;
            }
        } else if (reply.getStatusCode() == 44) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }

            runWithRedirect(current, count + 1);
            return;

        } else if (reply.getStatusCode() >= 50 && reply.getStatusCode() < 60) {
            System.out.flush();
            System.exit(reply.getStatusCode());
        } else if (reply.getStatusCode() >= 40 && reply.getStatusCode() < 50) {
            System.out.flush();
            System.exit(1);
        }
        System.out.flush();
        System.exit(1);
    }
}
