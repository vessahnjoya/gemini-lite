package handler;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

import protocol.*;

public class FileSystemRequestHandler implements ResourceHandler {
    private final Path path;

    public FileSystemRequestHandler(Path path) {
        if (path == null) {
            throw new IllegalArgumentException("Path cannot be Null!");
        }
        this.path = path.toAbsolutePath().normalize();
    }

    @Override
    public Reply handle(Request request) {
        try {
            URI uri = request.getUri();

            if (!"gemini-lite://".equals(uri.getScheme())) {
                return new Reply(59, "Invalid URI, does not contain expected scheme");
            }
            String path = uri.getPath();
            if (path == null || path.isEmpty()) {
                path = "/";
            }
            Path requestedPath = resolvePath(path);

            if (requestedPath == null) {
                return new Reply(52, "Not Found");
            }

            if (!Files.isDirectory(requestedPath)) {
                return new Reply(51, "path  is a directory");
            }

            if (!Files.exists(requestedPath) || !Files.isReadable(requestedPath)) {
                return new Reply(50, "Not Found");
            }

            String mimeType = getMimeType(requestedPath);

            return new Reply(20, mimeType);

        } catch (Exception e) {
            return new Reply(50, "Server crashed");
        }
    }

    private String getMimeType(Path filePath) {
        String fileName = filePath.getFileName().toString();

        if (fileName.endsWith(".gmi")) {
            return "text/gemini";
        } else if (fileName.endsWith(".txt")) {
            return "text/plain";
        } else if (fileName.endsWith(".gif")) {
            return "image/gif";
        } else if (fileName.endsWith(".html") || fileName.endsWith(".htm")) {
            return "text/html";
        } else if (fileName.endsWith(".png")) {
            return "image/png";
        } else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
            return "image/jpeg";
        }

        return "application-octet/stream";
    }

    private Path resolvePath(String p) {
        try {
            if (p.startsWith("/")) {
                p = p.substring(1);
            }

            Path resolvedPath = path.resolve(p).normalize();

            if (!resolvedPath.startsWith(path)) {
                return null;
            }

            return resolvedPath;
        } catch (InvalidPathException e) {
            return null;
        }
    }

    @Override
    public InputStream getData(Request request) throws IOException {
        var uri = request.getUri();
        String str = uri.getPath();

        if (str == null || str.isEmpty()) {
            str = "/";
        }

        var requestedPath = resolvePath(str);

        if (requestedPath == null || !Files.exists(requestedPath)) {
            throw new ProtocolSyntaxException("File Not found");
        }

        if (!Files.isReadable(requestedPath)) {
            throw new ProtocolSyntaxException("File is not readable");
        }

        return Files.newInputStream(requestedPath);
    }
}
