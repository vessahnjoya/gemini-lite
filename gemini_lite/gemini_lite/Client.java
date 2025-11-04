package gemini_lite;

import java.io.*;
import java.net.*;

import engine.ClientEngine;
import engine.Engine;

public class Client {
    private static Engine engine;
    // TODO: update to implememt the engine instead

    public static void main(String[] args) throws Throwable {

        if (args.length < 1) {
            System.err.println("Invalid Request");
            System.exit(1);
        }
        try {
            var uri = new URI(args[0]);
            engine = new ClientEngine(uri);

            engine.run();
        } catch (IOException e) {
            System.err.println("invalid uri" + e.getMessage());
        }
    }
}