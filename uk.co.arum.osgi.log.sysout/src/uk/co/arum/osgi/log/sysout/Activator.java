package uk.co.arum.osgi.log.sysout;

import java.util.Date;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogEntry;
import org.osgi.service.log.LogListener;
import org.osgi.service.log.LogReaderService;
import org.osgi.util.tracker.ServiceTracker;

public class Activator implements BundleActivator, LogListener {

	public void start(BundleContext context) throws Exception {
		new ServiceTracker(context, LogReaderService.class.getName(), null) {
			@Override
			public Object addingService(ServiceReference reference) {
				LogReaderService service = (LogReaderService) context
						.getService(reference);
				service.addLogListener(Activator.this);
				return service;
			}

			@Override
			public void removedService(ServiceReference reference,
					Object service) {
				((LogReaderService) service).removeLogListener(Activator.this);
				super.removedService(reference, service);
			}
		}.open();
	}

	public void stop(BundleContext context) throws Exception {
	}

	public void logged(LogEntry entry) {
		String sr = null == entry.getServiceReference() ? "<no service reference>"
				: entry.getServiceReference().toString();
		String ex = null == entry.getException() ? "<no exception>" : entry
				.getException().toString();
		System.out.println(new Date(entry.getTime()) + " : " + entry.getLevel()
				+ " : " + entry.getMessage() + " : " + ex + " : " + sr);
	}
}
