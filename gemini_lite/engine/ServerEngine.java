package engine;

import java.io.IOException;
import java.net.*;

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
            System.err.println("Listening on port " + port);
            while (true) {
                final var socket = server.accept();
                handleConnection(socket);
            }
        }
    }


    public void handleConnection(Socket socket) throws IOException {
        try {
            final var i = socket.getInputStream();
            final var o = socket.getOutputStream();

            var request = Request.parse(i);
            o.flush();
        } finally {
            socket.close();
        }
    }
}