package protocol;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class Request {

    private final String uri;

    public Request(String uri) {
        this.uri = uri;
    }

    public String getUri() {
        return uri;
    }

    //TODO: parsing implementation

    public void format(OutputStream requestOutput){
        String request = uri + "\r\n";
        try {
            requestOutput.write(request.getBytes(StandardCharsets.UTF_8));
            requestOutput.flush();
        } catch (IOException e) {
            System.err.println("Failed Request, Invalid URI " + e.getMessage());
        }
    }
}
