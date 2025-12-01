package engine;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

import handler.ReplyAndBody;
import protocol.*;

public class ClientEngine implements Engine {
    private final URI uri;
    private final int MAX_REDIRECTS = 5;
    private final int DEFAULT_PORT = 1958;
    private Socket socket;
    private String userInput;

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

    private boolean hasUserInfo() {
        return uri.getUserInfo() != null;
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
        if (hasUserInfo()) {
            System.err.println(" Invalid URI, URI should not contain UserInfo!");
            System.exit(1);
        }

        if (socket == null) {
            var host = getHost(uri);
            var port = getPort(uri);
            socket = new Socket(host, port);
            // var in = socket.getInputStream();
            // var out = socket.getOutputStream();
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

        if (!"gemini-lite".equalsIgnoreCase(target.getScheme())) {
            System.err.println("Non-Gemini redirect not supported: " + target);
            System.exit(1);
            return;
        }

        runWithRedirect(target, count + 1);
    }

    private void processRequest(URI current, InputStream in, OutputStream out, int count) throws IOException {
        Request request = new Request(current);
        request.format(out);

        Reply reply = Reply.parse(in);
        ReplyAndBody replyAndBody = reply.withoutBody();
        if (reply.getStatusCode() == 20) {
            in.transferTo(System.out);
            System.out.flush();
            System.exit(0);
        } else if (reply.getStatusCode() >= 30 && reply.getStatusCode() < 40) {
            handleRedirect(current, count, reply.getMeta().trim());
        } else if (reply.getStatusCode() >= 10 && reply.getStatusCode() < 20) {

            if (userInput != null) {
                String encodedInput = URLEncoder.encode(userInput, StandardCharsets.UTF_8.toString()).replace("+",
                        "%20");
                URI newuri;
                try {
                    newuri = utils.URIutils.buildNewURI(current, encodedInput);
                } catch (Exception e) {
                    System.err.println("invalid query");
                    System.exit(1);
                    return;
                }
                runWithRedirect(newuri, count + 1);
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
