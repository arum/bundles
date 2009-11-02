package uk.co.arum.osgi.amf3.flex.remoting.bundle;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogService;
import org.osgi.util.tracker.ServiceTracker;

public class AMFServicesTracker extends ServiceTracker {

	private final OSGiAMFConfig config;

	private final LogService log;

	public AMFServicesTracker(BundleContext context, LogService log,
			OSGiAMFConfig config, String propertyName)
			throws InvalidSyntaxException {
		super(context, context.createFilter("(" + propertyName + "=*)"), null);
		this.log = log;
		this.config = config;
	}

	@Override
	public Object addingService(ServiceReference reference) {
		log.log(LogService.LOG_INFO, "Tracking AMF service: "
				+ reference.getProperty(Constants.OBJECTCLASS));
		config.addOSGiService(reference);
		return super.addingService(reference);
	}

	@Override
	public void removedService(ServiceReference reference, Object service) {
		log.log(LogService.LOG_INFO, "No longer tracking AMF service: "
				+ reference.getProperty(Constants.OBJECTCLASS));
		config.removeOSGiService(reference);
		super.removedService(reference, service);
	}

}