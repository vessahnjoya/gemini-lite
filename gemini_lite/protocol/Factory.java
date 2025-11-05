package protocol;

import java.io.IOException;
import java.io.*;
/**
 * Interface that governs the main components of reply and request classes
 */
public interface Factory<T> {
/**
 * Input parser 
 * @param <T>
 *        either a request or a reply
 * @param in
 * @return a reply or a request
 * @throws ProtocolSyntaxException
 * @throws IOException
 */
    public static <T> T parser(InputStream in) throws ProtocolSyntaxException, IOException {
     throw new UnsupportedOperationException();
    }
/**
 * Output formatter
 * @param out
 *        the output Stream
 * @throws IOException
 */
    public void format(OutputStream out) throws IOException;
    
}
