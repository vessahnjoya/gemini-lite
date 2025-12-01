package protocol;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

public class Request {
    private final URI uri;
    private static final int MAX_URI_BYTE_SIZE = 1024;
    private static final String URI_SCHEME = "gemini-lite://";

    public Request(URI uri) {
        this.uri = uri;
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
        if (line.isEmpty() || !line.startsWith(URI_SCHEME)) {
            throw new ProtocolSyntaxException("Invalid or empty URI");
        }
        return new Request(new URI(line));
    }

    public void format(OutputStream requestOutput) throws IOException {
        String request = uri.toString() + "\r\n";
        requestOutput.write(request.getBytes(StandardCharsets.UTF_8));
        requestOutput.flush();
    }
}
