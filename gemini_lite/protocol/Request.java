package protocol;

import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;

/**
 * Thic class handles the request operation, hand;es parsing and its output
 * format.
 */
public class Request {
    // variable holding reference to the uri
    private final URI uri;

    /**
     * Comstructor to initialiZe the URI
     * 
     * @param uri
     */
    public Request(URI uri) {
        this.uri = uri;
    }

    /**
     * HELPHER METHOD TO GET THE uri
     * 
     * @return URI
     */
    public URI getUri() {
        return uri;
    }

    // TODO: parsing implementation
    /**
     * This method parses the request from inputStream following the Gemini
     * specification
     * 
     * @param in
     * @return request
     * @throws ProtocolSyntaxException
     */
    public static Request parser(InputStream in) throws ProtocolSyntaxException {
        return null;

    }

    /**
     * This method formats the request into the outputstream
     * 
     * @param requestOutput
     */
    public void format(OutputStream requestOutput) {
        String request = uri + "\r\n";
        try {
            requestOutput.write(request.getBytes(StandardCharsets.UTF_8));
            requestOutput.flush();
        } catch (IOException e) {
            System.err.println("Failed Request, Invalid URI " + e.getMessage());
        }
    }
}
