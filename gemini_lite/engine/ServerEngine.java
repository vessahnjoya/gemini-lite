package engine;

import java.io.IOException;
import java.net.*;

import protocol.*;

public class ServerEngine implements Engine {
    // Variable holding reference to the URI
    private final int port;

    /**
     * Constructor to initialize URI
     * 
     * @param uri
     */
    public ServerEngine(int port) {
        this.port = port;

    }

    /**
     * Helper method to get port. If not stated, returns tHe default port 1958
     * 
     * @return port number
     */
    private int getPort() {
        if (port == -1) {
            return 1958;
        }
        return port;
    }

    @Override
    public void run() throws IOException {

         try (final var server = new ServerSocket(port)) {
            System.err.println("Listening on port " + port);
            while (true) {
                final var socket = server.accept();
                handleConnection(socket);
            }
        }
    }


    public void handleConnection(Socket socket) throws IOException {
        try {
            // TODO: read request. This server is so terrible it doesn't even wait for a
            // request before sending a failure reply!
            final var i = socket.getInputStream();
            final var o = socket.getOutputStream();

            var request = Request.parse(i);
            o.flush();
        } finally {
            socket.close();
        }
    }
}