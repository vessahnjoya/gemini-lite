package protocol;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

/**
 * This class implimnets the factory interface and provides implimentation for
 * parsing a request and its output
 * format.
 */
public class Request {
    // variable holding reference to the uri
    private final URI uri;

    // constants to account for uri length and the expected scheme for the uri
    // respectively
    private static final int MAX_URI_BYTE_SIZE = 1024;
    private static final String URI_SCHEME = "gemini-lite://";

    /**
     * Comstructor to initialiZe the URI
     * 
     * @param uri
     */
    public Request(URI uri) {
        this.uri = uri;
    }

    /**
     * Helper method to get the uri
     * 
     * @return URI
     */
    public URI getUri() {
        return uri;
    }

    /**
     * This method parses the request from inputStream following the Gemini
     * specification, and reading byte-byte as recommended
     * 
     * @param in
     * @return request
     * @throws ProtocolSyntaxException Syntax errors in the request line
     * @throws IOException             I/O errors realted to the reader
     * @throws URISyntaxException 
     */
    public static Request parse(InputStream in) throws ProtocolSyntaxException, IOException, URISyntaxException {
        var buffer = new ByteArrayOutputStream();
        int count = 0;
        while (true) {
            int reader = in.read();
            boolean flag = false;
            if (reader == -1) {
                if (count == 0) {
                    throw new ProtocolSyntaxException("End of stream before request line");
                }
                throw new ProtocolSyntaxException("End of stream, Missing CRLF");
            }

            if (reader == '\r') {
                flag = true;
                continue;
            }

            if (reader == '\n') {
                if (flag) {
                    throw new ProtocolSyntaxException("LF found without CR");
                }
                break;
            }

            if (flag) {
                throw new ProtocolSyntaxException("Found CR without LF");
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

    /**
     * This method formats the request into the outputstream
     * 
     * @param requestOutput
     * @throws IOException
     */
    public void format(OutputStream requestOutput) throws IOException {
        String request = uri.toString() + "\r\n";
        requestOutput.write(request.getBytes(StandardCharsets.UTF_8));
        requestOutput.flush();
    }
}
