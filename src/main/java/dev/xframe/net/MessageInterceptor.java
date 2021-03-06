package dev.xframe.net;

import dev.xframe.net.codec.IMessage;
import dev.xframe.net.session.Session;

public interface MessageInterceptor {
	
	public boolean intercept(Session session, IMessage message) throws Exception;

}
