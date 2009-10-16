package uk.co.arum.osgi.amf3.flex.remoting;

import flex.messaging.messages.RemotingMessage;

public class RemotingContext {

	private static ThreadLocal<RemotingContext> threadLocal = new ThreadLocal<RemotingContext>();

	private final RemotingMessage message;

	public RemotingContext(RemotingMessage message) {
		this.message = message;
		threadLocal.set(this);
	}

	public static RemotingContext getCurrent() {
		return threadLocal.get();
	}

	public RemotingMessage getMessage() {
		return message;
	}

	public Object getMessageHeader(String name) {
		return message.getHeader(name);
	}

}
