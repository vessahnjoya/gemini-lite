package engine;

import java.io.*;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;

import protocol.Reply;
import protocol.Request;

public class ProxyEngine implements Engine {

    private final Socket clientSocket;
    private final int DEFAULT_PORT = 1958;
    private final int PROXY_ERROR_CODE = 43;
    private final int CLIENT_ERROR_CODE = 59;
    private final int MAX_REDIRECTS = 5;
    private static final String URI_SCHEME = "gemini-lite";

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
                sendErrorOnBadRequest(clientOut, "Invalid request");
                clientOut.flush();
                return;
            }

            var target = request.getUri();
            String host = target.getHost();

            if (host == null || host.isEmpty()) {
                sendProxyError(clientOut, "Proxy error: missing host");
                clientOut.flush();
                return;
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
                    sendProxyError(clientOut, "proxy error: unknown host");
                    clientOut.flush();
                    return;
                } catch (ConnectException e) {
                    sendProxyError(clientOut, "Proxy error: connection refused");
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

                    if (reply.getStatusCode() == 44) {
                        slowDownHandler(host, port, request, reply, clientOut);
                        return;
                    } else if (reply.getStatusCode() < 10 || reply.getStatusCode() > 59) {
                        var rep = new Reply(PROXY_ERROR_CODE, reply.getMeta());
                        rep.format(clientOut);
                        replySent = true;
                        clientOut.flush();
                        return;
                    } else if (reply.getStatusCode() >= 30 && reply.getStatusCode() < 40) {
                        var handled = redirectHandler(request, reply, clientOut);
                        if (!handled) {
                            sendProxyError(clientOut, "Proxy error: too many redirects");
                        }

                        clientOut.flush();
                        return;

                    }

                    reply.format(clientOut);
                    if (reply.getStatusCode() >= 20 && reply.getStatusCode() < 30) {
                        in.transferTo(clientOut);
                    }
                    clientOut.flush();
                    return;

                } catch (Exception e) {
                    if (!replySent) {
                        sendProxyError(clientOut, "Proxy error: " + e.getMessage());
                        clientOut.flush();
                    }

                }

            }
            clientSocket.close();
        }
    }

    private boolean redirectHandler(Request originalRequest, Reply initialReply, BufferedOutputStream clientOut)
            throws URISyntaxException, IOException {
        var reply = initialReply;
        int count = 0;

        while (reply.getStatusCode() >= 30 && reply.getStatusCode() < 40 && count < MAX_REDIRECTS) {
            count++;
            String redirectString;
            if (reply.getMeta().startsWith(URI_SCHEME)) {
                redirectString = reply.getMeta();
            } else {
                redirectString = originalRequest.getUri().resolve(reply.getMeta()).toString();
            }
            URI redirectUri;
            try {
                redirectUri = new URI(redirectString);

            } catch (Exception e) {
                sendProxyError(clientOut, "Proxy error: invalid request URI");
                clientOut.flush();
                return true;
            }

            try (var redirectSocket = new Socket()) {
                redirectSocket.connect(new InetSocketAddress(redirectUri.getHost(), DEFAULT_PORT));

                try (var rin = new BufferedInputStream(redirectSocket.getInputStream());
                        var rout = new BufferedOutputStream(redirectSocket.getOutputStream())) {

                    var redirectRequest = new Request(redirectString);
                    redirectRequest.format(rout);

                    reply = Reply.parse(rin);
                    reply.format(clientOut);
                    if (reply.getStatusCode() >= 20 && reply.getStatusCode() < 30) {
                        rin.transferTo(clientOut);
                        return true;
                    }
                }

            }

        }

        return reply.getStatusCode() < 30 || reply.getStatusCode() >= 40;
    }

    private void slowDownHandler(String host, int port, Request request, Reply slowDownReply,
            BufferedOutputStream clientOut) throws IOException {
        int seconds;
        try {
            seconds = Integer.parseInt(slowDownReply.getMeta().trim());
        } catch (NumberFormatException e) {
            slowDownReply.format(clientOut);
            clientOut.flush();
            return;
        }

        try {
            Thread.sleep(seconds * 1000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            sendProxyError(clientOut, "Proxy error: Interrupted slow down");
            clientOut.flush();
            return;
        }

        try (var retrySocket = new Socket()) {
            retrySocket.connect(new InetSocketAddress(host, port));

            try (var retryInput = new BufferedInputStream(retrySocket.getInputStream());
                    var retryOuput = new BufferedOutputStream(retrySocket.getOutputStream())) {
                request.format(retryOuput);
                var retryReply = Reply.parse(retryInput);
                retryReply.format(clientOut);

                if (retryReply.getStatusCode() >= 20 && retryReply.getStatusCode() < 30) {
                    retryInput.transferTo(clientOut);
                }
                clientOut.flush();
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

    private void sendErrorOnBadRequest(BufferedOutputStream out, String meta) throws IOException {
        try {
            new Reply(CLIENT_ERROR_CODE, meta).format(out);
        } catch (Exception e) {
            System.err.println("Failed to send client Error");
            System.exit(1);
        }
    }

}