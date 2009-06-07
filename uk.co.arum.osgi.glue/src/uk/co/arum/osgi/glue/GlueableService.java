package uk.co.arum.osgi.glue;

import java.util.Dictionary;

/**
 * If your component needs to be registered as a service as well implement this
 * interface. It is registered as a service <em>after</em> it has been
 * activated. Likewise, it is unregistered as a service <em>before</em> it is
 * deactivated.
 * 
 * @author brindy
 */
public interface GlueableService extends Glueable {

	/** A list of names to register this service as. */
	String[] getServiceNames();

	/**
	 * Get the properties to use for this object for the given service name.
	 * 
	 * @param serviceName
	 *            the service name as returned from {@link #getServiceNames()}
	 * @return the properties or null
	 */
	@SuppressWarnings("unchecked")
	Dictionary getProperties(String serviceName);

}
