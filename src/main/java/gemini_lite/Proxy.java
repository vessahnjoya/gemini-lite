package gemini_lite;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.Executors;

import engine.*;

public class Proxy {
    private static Engine engine;

    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.err.println("Invalid input");
            System.exit(1);
        }

        int port = Integer.parseInt(args[0]);

        var executor = Executors.newCachedThreadPool();

        try (var serverSocket = new ServerSocket(port)) {
            System.out.print("listening on port " + port + "\r\n");
            while (true) {
                var clientSocket = serverSocket.accept();

                executor.execute(() -> {
                    try {
                        engine = new ProxyEngine(clientSocket);
                        engine.run();
                    } catch (Exception e) {
                        System.err.println("Error handling connection: " + e.getMessage());
                        try {
                            var out = clientSocket.getOutputStream();
                            out.write("43 proxy error".getBytes());
                            out.flush();
                        } catch (IOException e2) {

                        }
                    } finally {
                        try {
                            clientSocket.close();
                        } catch (Exception e) {
                        }
                    }
                });
            }
        }
    }
}
