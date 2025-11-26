package utils;

import java.net.URI;
import java.net.URISyntaxException;

public class URIutils {
    public static URI buildNewURI(URI original, String newQuery) throws URISyntaxException{
        return new URI(
            original.getScheme(),
            original.getAuthority(),
            original.getPath(),
            newQuery,
            null
        );
    }
}
