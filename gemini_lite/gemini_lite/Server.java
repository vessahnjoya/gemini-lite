package gemini_lite;

import java.io.*;
import java.nio.file.Paths;

import engine.*;
import handler.*;

/**
 * This class represents a Client
 */
public class Server {
    // variable holding reference to the engine
    private static Engine engine;
    private static ResourceHandler resourceHandler;

    public static void main(String[] args) throws Throwable {

        if (args.length < 1 || args.length > 2) {
            System.err.println("Invalid usage, use: <directory> <port>");
            System.exit(1);
        }

        String directoryPath = args[0];
        int port = Integer.parseInt(args[1]);
        try {
            var path = Paths.get(directoryPath);

            if (!path.toFile().exists()) {
                System.err.println("Directory does not exits: " + path);
            }
            if (!path.toFile().isDirectory()) {
                System.err.println("Path is not a directory: " + path);
            }
            resourceHandler = ResourceHandler.fileSystem(null);
            engine = new ServerEngine(port, resourceHandler);
            engine.run();
        } catch (IOException e) {
            System.err.println("Error while starting server: " + e.getMessage());
            System.exit(1);
        }
    }
}
