package protocol;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

import utils.URIutils;

public class Request {
    private final URI uri;
    private final String URIstring;
    private static final int MAX_URI_BYTE_SIZE = 1024;
    private static final String URI_SCHEME = "gemini-lite://";

    public Request(URI uri) {
        this.uri = uri;
        this.URIstring = null;
    }

    public Request(String URIstring) throws URISyntaxException {
        this.URIstring = URIstring;
        this.uri = new URI(URIstring);
    }

    public URI getUri() {
        return uri;
    }

    public static Request parse(InputStream in) throws ProtocolSyntaxException, IOException, URISyntaxException {
        var buffer = new ByteArrayOutputStream();
        int count = 0;
        while (true) {
            int reader = in.read();
            if (reader == -1) {
                if (count == 0) {
                    throw new ProtocolSyntaxException("End of stream before request line");
                }
                throw new ProtocolSyntaxException("End of stream, Missing CRLF");
            }

            if (reader == '\r') {
                int next = in.read();
                if (next != '\n') {
                    throw new ProtocolSyntaxException("CR found without LF");
                }
                break;
            } else if (reader == '\n') {
                throw new ProtocolSyntaxException("LF found without CR");
            }

            count++;
            if (count > MAX_URI_BYTE_SIZE) {
                throw new ProtocolSyntaxException("URI exceeds max length");
            }

            buffer.write(reader);
        }
        String line = buffer.toString(StandardCharsets.UTF_8.name());
        if (line.isEmpty() || !line.toLowerCase().startsWith(URI_SCHEME)) {
            throw new ProtocolSyntaxException("Invalid or empty URI");
        }

        var uri = new URI(line);
        if (uri.getUserInfo() != null) {
            throw new ProtocolSyntaxException("user info not allowed");
        }

        if (uri.getFragment()!= null) {
            throw new ProtocolSyntaxException("fragments not allowed");
        }

        return new Request(line);
    }

    public void format(OutputStream requestOutput) throws IOException {
        String requestLine;
        if (URIstring != null) {
            requestLine = URIstring;
        } else{
            StringBuilder builder = new StringBuilder();
        builder.append(uri.getScheme()).append("://").append(uri.getHost());
        
        if (uri.getPort() != -1) {
            builder.append(":").append(uri.getPort());
        }

        if (uri.getPath() != null) {
            builder.append(uri.getPath());
        }

        if (uri.getRawQuery() != null) {
            builder.append("?").append(uri.getRawQuery());
        }
        requestLine = builder.toString();
        }

        String request = requestLine + "\r\n";
        requestOutput.write(request.getBytes(StandardCharsets.UTF_8));
        requestOutput.flush();
    }
}
