package handler;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import protocol.*;

public class FileSystemRequestHandler implements ResourceHandler {
    private final Path path;
    private static final String URI_SCHEME = "gemini-lite";

    public FileSystemRequestHandler(Path path) {
        if (path == null) {
            throw new IllegalArgumentException("Path cannot be Null!");
        }
        this.path = path.toAbsolutePath().normalize();
    }

    @Override
    public ReplyAndBody handle(Request request) {
        try {
            URI uri = request.getUri();
            String pathString = uri.getPath();

            if (!URI_SCHEME.equalsIgnoreCase(uri.getScheme())) {
                return new Reply(59, "Invalid URI, does not contain expected scheme").withoutBody();
            }

            if (pathString == null || pathString.isEmpty()) {
                pathString = "/";
            }

            var requestedPath = resolvePath(pathString);

            if (requestedPath == null) {
                return new Reply(59, "bad request").withoutBody();
            }

            if (Files.isDirectory(requestedPath)) {
                String mime = "text/gemini";
                InputStream body = listDirectoryElements(requestedPath);
                
                return new Reply(20, mime).withBody(body);
            }

            if (Files.exists(requestedPath) && Files.isRegularFile(requestedPath) && Files.isReadable(requestedPath)) {
                String mime = getMimeType(requestedPath);
                InputStream body = Files.newInputStream(requestedPath);
                return new Reply(20, mime).withBody(body);
            }

            return new Reply(51, "Not Found").withoutBody();

        } catch (Exception e) {
            return new Reply(50, "Server crashed").withoutBody();
        }
    }

    private String getMimeType(Path filePath) {
        String fileName = filePath.getFileName().toString();

        if (fileName.toLowerCase().endsWith(".gmi")) {
            return "text/gemini";
        } else if (fileName.toLowerCase().endsWith(".txt")) {
            return "text/plain";
        } else if (fileName.toLowerCase().endsWith(".gif")) {
            return "image/gif";
        } else if (fileName.toLowerCase().endsWith(".html") || fileName.endsWith(".htm")) {
            return "text/html";
        } else if (fileName.toLowerCase().endsWith(".png")) {
            return "image/png";
        } else if (fileName.toLowerCase().endsWith(".jpg") || fileName.endsWith(".jpeg")) {
            return "image/jpeg";
        }

        return "application/octet-stream";
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

        if (Files.isDirectory(requestedPath)) {
            return listDirectoryElements(requestedPath);
        }

        if (!Files.isReadable(requestedPath)) {
            throw new ProtocolSyntaxException("File is not readable");
        }

        return Files.newInputStream(requestedPath);
    }

    private InputStream listDirectoryElements(Path dir) throws IOException {
        StringBuilder list = new StringBuilder();
        String folderPath = path.relativize(dir).toString().replace('\\', '/');

        if (folderPath.isEmpty()) {
            folderPath = "/";
        }
        list.append("Directory Listing for ").append(folderPath).append("\r\n\r\n");

        List<Path> items = Files.list(dir).sorted().collect(Collectors.toList());

        for (Path item : items) {
            String fileName = item.getFileName().toString();
            String link = "/" + path.relativize(item).toString().replace("\\", "/");

            if (Files.isDirectory(item)) {
                link += "/";
                fileName += "/";
            }

            list.append("=> ").append(link).append(" ").append(fileName).append("\r\n");
        }

        return new ByteArrayInputStream(list.toString().getBytes(StandardCharsets.UTF_8));
    }
}
