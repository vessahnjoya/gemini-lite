package engine;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import protocol.Reply;
import protocol.Request;

public class ProxyEngine implements Engine {

    private final Socket clientSocket;
    private final int DEFAULT_PORT = 1958;
    private final int PROXY_ERROR_CODE = 43;

    public ProxyEngine(Socket socket) {
        this.clientSocket = socket;
    }

    @Override
    public void run() throws IOException {
        try (var clientIin = new BufferedInputStream(clientSocket.getInputStream());
                var clientOut = new BufferedOutputStream(clientSocket.getOutputStream())) {
            Request request = null;
            try {
                request = Request.parse(clientIin);
            } catch (Exception e) {
                sendProxyError(clientOut, "Proxy error: invalid request");
            }

            var target = request.getUri();
            String host = target.getHost();

            if (host == null || host.isEmpty()) {
                sendProxyError(clientOut, "Proxy error: missing host");
            }

            int port = target.getPort();

            if (port == -1) {
                port = DEFAULT_PORT;
            }

            boolean replySent = false;

            try (var socket = new Socket()) {
                try {
                    socket.connect(new InetSocketAddress(host, port));
                } catch (UnknownHostException e) {
                    sendProxyError(clientOut, "proxy error: cannot connect");
                    clientOut.flush();
                    return;
                }

                try (var in = new BufferedInputStream(socket.getInputStream());
                        var out = new BufferedOutputStream(socket.getOutputStream())) {
                    request.format(out);
                    Reply reply;
                    try {
                        reply = Reply.parse(in);
                    } catch (Exception e) {
                        sendProxyError(clientOut, "proxy error: Invalid Client");
                        clientOut.flush();
                        return;
                    }

                    reply.format(clientOut);
                    replySent = true;

                    if (reply.isInputReply()) {
                        communicationBetweenSockets(clientIin, clientOut, in, out);
                        return;
                    }

                    if (reply.hasBody()) {
                        reply.relayBody(clientOut);
                        clientOut.flush();
                    }

                } catch (Exception e) {
                    if (!replySent) {
                        sendProxyError(clientOut, "proxy error: cannot connect to client");
                    }
                }
            }

        }
    }

    private void sendProxyError(BufferedOutputStream out, String meta) throws IOException {
        try {
            new Reply(PROXY_ERROR_CODE, meta).format(out);
        } catch (Exception e) {
            System.err.println("Failed to send proxy Error");
            System.exit(1);
        }
    }

    private void communicationBetweenSockets(BufferedInputStream clientIn, BufferedOutputStream clientOut,
            BufferedInputStream input, BufferedOutputStream output) {
        var t1 = new Thread(() -> {
            try {
                byte[] buffer = new byte[1024];
                int read;
                while ((read = clientIn.read(buffer)) != 1) {
                    output.write(buffer, 0, read);
                    output.flush();
                }
            } catch (Exception e) {
            }

            try {
                output.close();
            } catch (Exception e) {
            }
        });

        var t2 = new Thread(() -> {
            try {
                byte[] buffer = new byte[1024];
                int read;
                while ((read = input.read(buffer)) != 1) {
                    clientOut.write(buffer, 0, read);
                    clientOut.flush();
                }
            } catch (Exception e) {
            }

            try {
                clientOut.close();
            } catch (Exception e) {
            }
        });

        t1.start();
        t2.start();

        try {
            t1.join();
            t2.join();
        } catch (Exception e) {
            Thread.currentThread().interrupt();
        }
    }

}
