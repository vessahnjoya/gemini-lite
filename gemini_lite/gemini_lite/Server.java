package gemini_lite;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

import protocol.*;

public class Server {
    // TODO

    private final int port;

    public Server(int port) {
        this.port = port;
    }

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

            // var request = Request.parser(i);
            o.flush();
        } finally {
            socket.close();
        }
    }
}
