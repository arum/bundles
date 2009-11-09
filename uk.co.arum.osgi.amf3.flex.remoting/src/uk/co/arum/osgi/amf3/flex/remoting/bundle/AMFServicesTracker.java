package uk.co.arum.osgi.amf3.flex.remoting.bundle;

import java.util.Arrays;

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
		String[] serviceNames = (String[]) reference
				.getProperty(Constants.OBJECTCLASS);
		log.log(LogService.LOG_INFO, "Tracking AMF service: "
				+ Arrays.asList(serviceNames));

		config.addOSGiService(reference);
		return super.addingService(reference);
	}

	@Override
	public void removedService(ServiceReference reference, Object service) {
		String[] serviceNames = (String[]) reference
				.getProperty(Constants.OBJECTCLASS);
		log.log(LogService.LOG_INFO, "Stopped tracking AMF service: "
				+ Arrays.asList(serviceNames));
		config.removeOSGiService(reference);
		super.removedService(reference, service);
	}

}