package gemini_lite;

import java.net.*;

import engine.*;

/**
 * This class handles the Client
 */
public class Client {
    // variable holding reference to the engine
    private static Engine engine;
    private static String proxyEnv;

    public static void main(String[] args) throws Throwable {

        if (args.length < 1) {
            System.err.println("Invalid Request, no URI present");
            System.exit(1);
        }

        var uri = new URI(args[0]);
        proxyEnv = System.getenv("GEMINI_LITE_PROXY");

        if (proxyEnv == null || proxyEnv.isEmpty()) {
            engine = new ClientEngine(uri);
        } else {
            String[] proxyParts = proxyEnv.split(":", 2);
            String host = proxyParts[0];
            int port = Integer.parseInt(proxyParts[1]);

            if (proxyParts.length != 2) {
                System.err.println("Invalid proxy");
                System.exit(1);
                return;
            }

            try(var proxySocket = new Socket(host, port)) {
                engine = new ClientEngine( proxySocket,uri);
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