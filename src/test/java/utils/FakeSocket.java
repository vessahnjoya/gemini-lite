package utils;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class FakeSocket extends Socket {
    private final InputStream in;
    private final OutputStream out;

    public FakeSocket(InputStream in, OutputStream out) {
        this.in = in;
        this.out = out;
    }

    public InputStream getInputStream() {
        return in;
    }

    public OutputStream getoOutputStream() {
        return out;
    }
}