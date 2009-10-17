package uk.co.arum.osgi.amf3.sample.bundle;

import java.util.Date;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

import uk.co.arum.osgi.amf3.flex.remoting.events.PublishedObjectEvent;
import uk.co.arum.osgi.glue.GlueableService;

public class PublishedObjectEventHandler implements EventHandler,
		GlueableService {

	private EventAdmin eventAdmin;

	public PublishedObjectEventHandler() {
	}

	public Dictionary<?, ?> getProperties(String serviceName) {
		if (serviceName.equals(EventHandler.class.getName())) {
			Dictionary<String, String[]> ht = new Hashtable<String, String[]>();
			ht.put(EventConstants.EVENT_TOPIC,
					new String[] { PublishedObjectEvent.TOPIC });
			return ht;
		}
		return null;
	}

	public String getServiceFilter(String serviceName, String name) {
		return null;
	}

	public String[] getServiceNames() {
		return new String[] { EventHandler.class.getName() };
	}

	public void bind(EventAdmin admin) {
		this.eventAdmin = admin;
	}

	public void handleEvent(Event event) {
		if (event instanceof PublishedObjectEvent) {
			PublishedObjectEvent poe = (PublishedObjectEvent) event;

			if (poe.getSourceID() == null) {
				return;
			}

			System.out.println(poe.getChannelID() + " : " + poe.getMessageID()
					+ " : " + poe.getSourceID() + " : " + poe.getObject());

			String replyText = "Reply to published object " + new Date()
					+ " : " + poe.getObject();

			Map<String, String> theReply = new HashMap<String, String>();
			theReply.put("Say", replyText);

			eventAdmin.postEvent(poe.createReply(theReply));
		}
	}

}
