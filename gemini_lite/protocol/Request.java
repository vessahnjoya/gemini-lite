package protocol;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * This class implimnets the factory interface and provides implimentation for
 * parsing a request and its output
 * format.
 */
public class Request {
    // variable holding reference to the uri
    private final String uri;

    /**
     * Comstructor to initialiZe the URI
     * 
     * @param uri
     */
    public Request(String uri) {
        this.uri = uri;
    }

    /**
     * Helper method to get the uri
     * 
     * @return URI
     */
    public String getUri() {
        return uri;
    }

    /**
     * This method parses the request from inputStream following the Gemini
     * specification
     * 
     * @param in
     * @return request
     * @throws ProtocolSyntaxException Syntax errors
     * @throws IOException             I/O errors realted to the reader
     */
    public static Request parser(InputStream in) throws ProtocolSyntaxException, IOException {

        var reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        String line = reader.readLine();

        if (line == null) {
            throw new ProtocolSyntaxException("Null request");
        }

        if (line.trim().isEmpty()) {
            throw new ProtocolSyntaxException("Empty request");
        }

        if (!line.startsWith("gemini-lite://")) {
            throw new ProtocolSyntaxException("Invalid URI in request: " + line);
        }

        return new Request(line);
    }

    /**
     * This method formats the request into the outputstream
     * 
     * @param requestOutput
     * @throws IOException
     */
    public void format(OutputStream requestOutput) throws IOException {
        String request = uri + "\r\n";
        requestOutput.write(request.getBytes(StandardCharsets.UTF_8));
        requestOutput.flush();
    }
}
