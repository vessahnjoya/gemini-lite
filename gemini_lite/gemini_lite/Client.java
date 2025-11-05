package gemini_lite;

import java.net.*;

import engine.ClientEngine;
import engine.Engine;

/**
 * This class handles the Client
 */
public class Client {
    // variable holding reference to the engine
    private static Engine engine;
    // TODO: update to implememt the engine instead

    public static void main(String[] args) throws Throwable {

        if (args.length < 1) {
            System.err.println("Invalid Request");
            System.exit(1);
        }
        var uri = new URI(args[0]);
        engine = new ClientEngine(uri);

        engine.run();
    }
}