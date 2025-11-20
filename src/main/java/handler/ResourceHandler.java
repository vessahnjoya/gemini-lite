package handler;

import java.io.*;
import java.nio.file.Path;

import protocol.*;

public interface ResourceHandler {

    static ResourceHandler fileSystem(Path path) {
        return new FileSystemRequestHandler(path);
    }

    Reply handle(Request request);

    InputStream getData(Request request) throws IOException;
}
