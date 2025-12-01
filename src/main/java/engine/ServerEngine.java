package engine;

import java.io.IOException;
import java.io.InputStream;
import java.net.*;

import handler.ReplyAndBody;
import handler.ResourceHandler;
import protocol.*;

public class ServerEngine implements Engine {
    // Variable holding reference to the port number, and resource handler
    private final int port;
    private final ResourceHandler resourceHandler;
    private final int DEFAULT_PORT = 1958;

    /**
     * Constructor to initialize URI
     * 
     * @param uri
     */
    public ServerEngine(int port, ResourceHandler resourceHandler) {
        this.port = port;
        this.resourceHandler = resourceHandler;

    }

    /**
     * Helper method to get port. If not stated, returns tHe default port 1958
     * 
     * @return port number
     */
    private int getPort() {
        if (port == -1) {
            return DEFAULT_PORT;
        }
        return port;
    }

    @Override
    public void run() throws IOException {

        try (final var server = new ServerSocket(getPort())) {
            System.err.println("Listening on port " + getPort());
            while (true) {
                Socket socket = null;
                try {
                    socket = server.accept();
                    handleConnection(socket);
                } catch (Exception e) {
                    System.err.println("Error handling connection: " + e.getMessage());
                    if (socket.isClosed()) {
                        sendErrorReply(socket, new Reply(50, "Server Error"));
                    }
                }
            }
        }
    }

    public void handleConnection(Socket socket) throws IOException, URISyntaxException {
        Request request = null;
        ReplyAndBody replyAndBody = null;
        try (var i = socket.getInputStream();
                var o = socket.getOutputStream();) {
            try {

                request = Request.parse(i);
                replyAndBody = resourceHandler.handle(request);

            } catch (ProtocolSyntaxException e) {
                replyAndBody = new Reply(59, "Bad Request").withoutBody();
            } catch ( Exception e){
                new Reply(50, "Server error").format(o);
                o.flush();
                System.err.println("Handle error: " + e.getMessage());
            }

            replyAndBody.reply().format(o);
            if (replyAndBody.maybeBody() != null) {
                replyAndBody.maybeBody().transferTo(o);
            }
            o.flush();
        } catch (Exception e) {
            socket.close();
        }

    }

    private void sendErrorReply(Socket socket, Reply reply) throws IOException {
        try (var out = socket.getOutputStream()) {
            reply.format(out);
            out.flush();
        }
    }
}