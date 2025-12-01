package handler;

import java.io.InputStream;

import protocol.Reply;

public record ReplyAndBody(Reply reply, InputStream maybeBody) {

}