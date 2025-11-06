import java.io.*;
import java.net.*;

public class TerribleClient {
    public static void main(String[] args) throws Throwable {

        if (args.length < 1) {
            System.err.println("Invalid Request");
            System.exit(1);
        }
        try {
            var uri = new URI(args[0]);
            String hostName = uri.getHost();
            int port = uri.getPort();

            if (port == -1) {
                port = 1958;

            }

            try (final var s = new Socket(hostName, port)) {
                var out = new PrintWriter(s.getOutputStream(), true);
                out.println(uri.toString());
                final var i = s.getInputStream();
                final var o = s.getOutputStream();
                String request = uri.toString() + "\r\n";
                o.write(request.getBytes());
                o.flush();
                try (final var r = new BufferedReader(new InputStreamReader(i))) {
                    final var rep = r.readLine();
                    if (rep.startsWith("2")) {
                        try (final var w = new PrintWriter(System.out)) {
                            r.transferTo(w);
                        }
                    } else {
                        System.err.println(rep);
                        System.exit(Integer.parseInt(rep.substring(0, 2)));
                    }
                }
            }
        } catch (URISyntaxException e) {
            System.err.println("invalid uri" + e.getMessage());
        }
    }
}