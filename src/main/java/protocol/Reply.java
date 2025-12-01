package protocol;

import java.io.*;
import java.nio.charset.StandardCharsets;

import handler.ReplyAndBody;

public class Reply {
    private final int statusCode;
    private final String meta;

    public Reply(int statusCode, String meta) {
        this.statusCode = statusCode;
        this.meta = meta;
    }

    public ReplyAndBody withoutBody() {
        return new ReplyAndBody(this, null);
    }

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
                throw new ProtocolSyntaxException("End of stream missing line terminator");
            }

            if (reader == '\r') {
                flag = true;
                continue;
            }

            if (reader == '\n') {
                throw new ProtocolSyntaxException("End of stream missing line terminator");
            }

            buffer.write(reader);

        }

        String line = buffer.toString(StandardCharsets.UTF_8);

        if (line.length() < 2 || !Character.isDigit(line.charAt(0)) || !Character.isDigit(line.charAt(1))) {
            throw new ProtocolSyntaxException("Invalid reply format: " + line);
        }

        int statusCode = Integer.parseInt(line.substring(0, 2));
        String meta = "";

        if (line.length() == 2) {
            meta = "";
        }else{
            if (line.charAt(2) != ' ') {
                throw new ProtocolSyntaxException("Invalid reply format: " + line);
            }
            if (line.length() == 3) {
                throw new ProtocolSyntaxException("Invalid reply format: " + line);
            }
            if (!line.substring(3).trim().isEmpty()) {
                meta = line.substring(3);
            }else{
                throw new ProtocolSyntaxException("Invalid reply format: " + line);
            }
        }

        if (meta.getBytes(StandardCharsets.UTF_8).length > 1024) {
            throw new ProtocolSyntaxException("Meta exceeds 1024 bytes");
        }

        return new Reply(statusCode, meta);
    }

    public void format(OutputStream replyOutput) throws IOException {
        String reply = String.format("%02d %s\r\n", statusCode, meta);
        replyOutput.write(reply.getBytes(StandardCharsets.UTF_8));
        replyOutput.flush();
    }

    public boolean isInputReply() {
        return statusCode >= 10 && statusCode < 20;
    }

    public ReplyAndBody withBody(InputStream body) {
        return new ReplyAndBody(this, body);
    }

}