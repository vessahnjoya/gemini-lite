package protocol;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * This class implimnets the factory interface and provides implimentation for
 * parsing a reply and its output
 * format.
 */
public class Reply {
    // varaibles holding reference to status code, and meta respectively
    private final int statusCode;
    private final String meta;

    // constant to account for meta length
    private static final int MAX_META_BYTE_SIZE = 1024;

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

    /**
     * This method parses the reply from inputStream following the Gemini
     * specification
     * 
     * @param in
     *           the input stream
     * @return Reply
     *         the reply object
     * @throws ProtocolSyntaxException
     *                                 syntax errors in the reply line
     * @throws IOException
     *                                 I/o errors
     */
    public static Reply parser(InputStream in) throws ProtocolSyntaxException, IOException {
        var buffer = new ByteArrayOutputStream();
        int count = 0;
        while (true) {
            int reader = in.read();
            boolean flag = false;
            if (reader == -1) {
                if (count == 0) {
                    throw new ProtocolSyntaxException("End of stream before reply line");
                }
                throw new ProtocolSyntaxException("End of stream, Missing CRLF");
            }

            if (reader == '\r') {
                flag = true;
                continue;
            }

            if (reader == '\n') {
                if (!flag) {
                    throw new ProtocolSyntaxException("LF found without CR");
                }
                break;
            }

            if (flag) {
                throw new ProtocolSyntaxException("Found CR without LF");
            }

            count++;
            if (count > MAX_META_BYTE_SIZE) {
                throw new ProtocolSyntaxException("Reply line exceeds max length");
            }

            buffer.write(reader);
        }
        String line = buffer.toString(StandardCharsets.UTF_8.name());

        if (!Character.isDigit(line.charAt(0)) || !Character.isDigit(line.charAt(1)) || line.charAt(2) != ' ') {
            throw new ProtocolSyntaxException("Reply format is invalid: " + line);
        }

        int statusCode = 0;
        try {
            statusCode = Integer.parseInt(line.substring(0, 2));
        } catch (NumberFormatException e) {
            throw new ProtocolSyntaxException("Invalid status code: " + line.substring(0, 2));
        }
        String meta = line.substring(3);

        return new Reply(statusCode, meta);
    }

    /**
     * This method formats the reply into the outputstream
     * 
     * @param requestOutput
     * @throws IOException
     * 
     */
    public void format(OutputStream replyOutput) throws IOException {
        String reply = String.format("%02d %s\r\n", statusCode, meta);
        replyOutput.write(reply.getBytes(StandardCharsets.UTF_8));
        replyOutput.flush();
    }

}