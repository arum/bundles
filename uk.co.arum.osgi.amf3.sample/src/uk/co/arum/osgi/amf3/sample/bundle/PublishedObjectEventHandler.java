package uk.co.arum.osgi.amf3.sample.bundle;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventHandler;

import uk.co.arum.osgi.amf3.flex.remoting.events.PublishedObjectEvent;

public class PublishedObjectEventHandler implements EventHandler {

	private EventAdmin eventAdmin;

	public PublishedObjectEventHandler() {
	}

	public void bindEventAdmin(EventAdmin admin) {
		this.eventAdmin = admin;
	}

	public void unbindEventAdmin(EventAdmin admin) {
		if (this.eventAdmin == admin) {
			this.eventAdmin = null;
		}
	}

	public void handleEvent(Event event) {
		if (event instanceof PublishedObjectEvent) {
			PublishedObjectEvent poe = (PublishedObjectEvent) event;

			if (poe.getSourceID() == null) {
				return;
			}

			String replyText = "Reply to published object " + new Date()
					+ " : " + poe.getObject();

			Map<String, String> theReply = new HashMap<String, String>();
			theReply.put("Say", replyText);

			eventAdmin.postEvent(poe.createReply(theReply));
		}
	}

}
