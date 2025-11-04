package engine;

import java.io.IOException;
import java.net.*;

import protocol.Request;

public class ClientEngine implements Engine {

    private final URI uri;

    public ClientEngine(URI uri) {
        this.uri = uri;
    }

    private int getPort() {
        if (uri.getPort() == -1) {
            return 1958;
        }
        return uri.getPort();
    }

    private String getHost() {
        return uri.getHost();
    }

    @Override
    public void run() throws UnknownHostException, IOException {
        // TODO: IMPLEMENT THE CLIENT LOGIC (LAZY BUT HAVE TO PUSH AS TONY SAID)

        try (var socket = new Socket(getHost(), getPort())) {
            // out.println(uri.toString());x
            final var i = socket.getInputStream();
            final var o = socket.getOutputStream();

            var request =  new Request(uri);
            request.format(o);
            System.out.println("works");
        }
    }
}
