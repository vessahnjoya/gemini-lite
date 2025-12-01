import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.*;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import engine.*;
import utils.FakeSocket;

public class ProxyEngineTests {
    private static Engine engine;

    @Test
    public void testSendProxyError() throws IOException {
        String fakeRequest = "invalid request\r\n";
        var fakeInput = new ByteArrayInputStream(fakeRequest.getBytes(StandardCharsets.UTF_8));
        var fakeOutput = new ByteArrayOutputStream();
        var bufferedOutput = new BufferedOutputStream(fakeOutput);

        var fakeSocket = new FakeSocket(fakeInput, bufferedOutput);

        engine = new ProxyEngine(fakeSocket);

        engine.run();

        bufferedOutput.flush();

        String proxyResponse = fakeOutput.toString(StandardCharsets.UTF_8);

        assertTrue(proxyResponse.startsWith("43"), "Expected proxy error code 43");

        assertTrue(proxyResponse.contains("invalid request"), "Expected invalid request error message");
    }

}
