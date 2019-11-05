package dev.xframe.net.sever;

import dev.xframe.net.LifecycleListener;
import dev.xframe.net.MessageInterceptor;
import dev.xframe.net.NetMessageHandler;
import dev.xframe.net.cmd.CommandContext;
import dev.xframe.net.session.Session;
import io.netty.channel.ChannelHandlerContext;

public class ServerMessageHandler extends NetMessageHandler {

    public ServerMessageHandler(LifecycleListener listener, CommandContext cmds, MessageInterceptor interceptor) {
        super(listener, cmds, interceptor);
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        Session session = new ServerSession(ctx.channel(), listener);
        listener.onSessionRegister(session);
    }
    
}