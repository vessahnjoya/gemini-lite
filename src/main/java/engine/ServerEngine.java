package engine;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URISyntaxException;

import handler.ReplyAndBody;
import handler.ResourceHandler;
import protocol.ProtocolSyntaxException;
import protocol.Reply;
import protocol.Request;

public class ServerEngine implements Engine {
    private final int port;
    private final ResourceHandler resourceHandler;
    private final int DEFAULT_PORT = 1958;

    public ServerEngine(int port, ResourceHandler resourceHandler) {
        this.port = port != -1 ? port : DEFAULT_PORT;
        this.resourceHandler = resourceHandler;

    }

    @Override
    public void run() throws IOException {

        try (final var server = new ServerSocket(port)) {
            System.err.println("Listening on port " + port);
            while (true) {
                Socket socket = null;
                try {
                    socket = server.accept();
                    handleConnection(socket);
                } catch (IOException | URISyntaxException e) {
                    System.err.println("Error handling connection: " + e.getMessage());
                    if (socket.isClosed()) {
                        sendErrorReply(socket, new Reply(50, ""));
                    }
                }
            }
        }
    }

    public void handleConnection(Socket socket) throws IOException, URISyntaxException {
        Request request;
        ReplyAndBody replyAndBody;
        try (var i = socket.getInputStream();
                var o = socket.getOutputStream();) {
            try {

                request = Request.parse(i);
                replyAndBody = resourceHandler.handle(request);

            } catch (ProtocolSyntaxException e) {
                replyAndBody = new Reply(59, "bad request").withoutBody();
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