package protocol;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * This class handles a reply, and provides implimentation for
 * parsing a reply and its output
 * format.
 */
public class Reply {
    // varaibles holding reference to status code, and meta respectively
    private final int statusCode;
    private final String meta;
    private final InputStream body;

    /**
     * Constructor to initialize status code and meta
     * 
     * @param statusCode
     * @param meta
     */
    public Reply(int statusCode, String meta) {
        this(statusCode,meta,null);
    }

    public Reply(int statusCode, String meta, InputStream body) {
        this.statusCode = statusCode;
        this.meta = meta;
        this.body = body;
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
    public static Reply parse(InputStream in) throws ProtocolSyntaxException, IOException {
        var buffer = new ByteArrayOutputStream();
        boolean flag = false;

        while (true) {
            int reader = in.read();
            if (reader == -1) {
                if (buffer.size() == 0) {
                    throw new ProtocolSyntaxException("End of stream before Reply line");
                }
                throw new ProtocolSyntaxException("End of stream missing line terminator");
            }

            if (flag) {
                if (reader == '\n') {
                    flag = false;
                    break;
                }
                buffer.write(reader);
                flag = false;
            }

            if (reader == '\r') {
                flag = true;
                continue;
            }

            if (reader == '\n') {
                break;
            }

            buffer.write(reader);

        }

        String line = buffer.toString(StandardCharsets.UTF_8);

        if (line.length() < 3 || !Character.isDigit(line.charAt(0)) || !Character.isDigit(line.charAt(1)) || line.charAt(2) != ' ') {
            throw new ProtocolSyntaxException("Invalid reply format: " + line);
        }

        int statusCode = Integer.parseInt(line.substring(0,2));
        String meta = line.substring(3);

        if (meta.getBytes(StandardCharsets.UTF_8).length > 1024) {
            throw new ProtocolSyntaxException("Meta exceeds 1024 bytes");
        }

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

        if (body != null) {
            body.transferTo(replyOutput);
            body.close();
        }
        replyOutput.flush();
    }

    public boolean isInputReply() {
        return statusCode >= 10 && statusCode < 20;
    }

    public void relayBody(OutputStream out) throws IOException {
        if (hasBody()) {
            body.transferTo(out);
            body.close();
        }
    }

    public boolean hasBody() {
        return body != null;
    }

}