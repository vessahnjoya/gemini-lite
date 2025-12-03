package gemini_lite;

import java.net.*;

import engine.*;
import protocol.ProtocolSyntaxException;
import protocol.Request;

public class Client {
    private static Engine engine;
    private static String proxyEnv;

    public static void main(String[] args) throws Throwable {

        if (args.length < 1) {
            System.err.println("Invalid Request, no URI present");
            System.exit(1);
        }
        String URIstring = args[0];
        try {
            Request.validateUriString(URIstring);
        } catch (URISyntaxException | ProtocolSyntaxException e) {
            System.err.println("invlaid request URI: " + e.getMessage());
            System.exit(1);
        }

        var uri = new URI(URIstring);
        String userInput = null;
        try {
            userInput = args[1];
        } catch (Exception e) {
            
        }
        
        proxyEnv = System.getenv("GEMINI_LITE_PROXY");

        if (proxyEnv == null || proxyEnv.isEmpty()) {
            if (userInput != null) {
                engine = new ClientEngine(uri, userInput);
            } else {
                engine = new ClientEngine(uri);
            }

        } else {
            String[] proxyParts = proxyEnv.split(":", 2);
            String host = proxyParts[0];
            int port = Integer.parseInt(proxyParts[1]);

            if (proxyParts.length != 2) {
                System.err.println("Invalid proxy");
                System.exit(1);
                return;
            }

            try (var proxySocket = new Socket(host, port)) {
                engine = new ClientEngine(proxySocket, uri);
                engine.run();
                return;
            } catch (Exception e) {
                System.err.println("Error connecting to proxy: " + e.getMessage());
                System.exit(1);
                return;
            }

        }

        engine.run();
    }
}