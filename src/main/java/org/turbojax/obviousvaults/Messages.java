package org.turbojax.obviousvaults;

import org.turbojax.messages.MessageType;

import java.util.Set;

public class Messages {
    public static final MessageType USAGE_LIMIT = new MessageType("usage_limit", Set.of("uses"));
    public static final MessageType FINAL_USE = new MessageType("final_use");
    public static final MessageType HELP_MESSAGE = new MessageType("help_message");
    public static final MessageType NOT_PLAYER = new MessageType("not_player");
    public static final MessageType RELOAD_SUCCESS = new MessageType("reload_success");
}
