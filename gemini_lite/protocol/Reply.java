package protocol;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * This class implimnets the factory interface and provides implimentation for
 * parsing a reply and its output
 * format.
 */
public class Reply{
    // varaibles holding reference to status code, and meta respectively
    private final int statusCode;
    private final String meta;

    /**
     * Constructor to initialize status code and meta
     * 
     * @param statusCode
     * @param meta
     */
    public Reply(int statusCode, String meta) {
        this.statusCode = statusCode;
        this.meta = meta;
    }

    /**
     * Helpert method to get status code
     * 
     * @return status code
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * helper method to get meta
     * 
     * @return meta
     */
    public String getMeta() {
        return meta;
    }

    // TODO: parsing implementation
    // TODO: base components implementation
    /**
     * This method parses the reply from inputStream following the Gemini
     * specification
     * 
     * @param in
     *        the Input stream
     * @return Reply
     *         the reply
     * @throws ProtocolSyntaxException
     */
    public static Reply parser(InputStream in) throws ProtocolSyntaxException, IOException {
        var reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        String line = reader.readLine();
        return new Reply(0, null);
    }

    /**
     * This method formats the reply into the outputstream
     * 
     * @param requestOutput
     * 
     */
    public void format(OutputStream requestOutput) {
        // TODO: After base components implimentation do this
    }
}