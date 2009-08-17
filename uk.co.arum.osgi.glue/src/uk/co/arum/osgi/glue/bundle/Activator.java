package uk.co.arum.osgi.glue.bundle;

import java.util.HashMap;
import java.util.Map;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.SynchronousBundleListener;
import org.osgi.service.log.LogService;
import org.osgi.util.tracker.ServiceTracker;

import uk.co.arum.osgi.glue.Glueable;

/**
 * This looks at the Glue-Component property in bundles that are being
 * registered.
 * <p/>
 * 
 * The classname can be an instance of {@link Glueable} but that dependency is
 * optional.
 * 
 * @author brindy
 */
public class Activator implements BundleActivator, SynchronousBundleListener {

	private Map<String, GlueManager> managers;

	private LogService logService;

	// passed to manager objects so that they can always call in to a
	// log service with a valid reference
	private LogServiceDelegate logServiceDelegate = new LogServiceDelegate();

	public void start(BundleContext context) throws Exception {

		LogServiceTracker tracker = new LogServiceTracker(context);
		logService = tracker;
		tracker.open();

		managers = new HashMap<String, GlueManager>();

		// check bundles already started
		for (Bundle bundle : context.getBundles()) {
			if (bundle.getState() == Bundle.ACTIVE) {
				String[] components = extractComponentNames(bundle);
				if (null != components) {
					for (String component : components) {
						registerComponent(component, bundle);
					}
				}
			}
		}

		// listen for bundles starting
		context.addBundleListener(this);
	}

	public void stop(BundleContext context) throws Exception {
		context.removeBundleListener(this);

		for (GlueManager mgr : managers.values()) {
			mgr.dispose();
		}

		managers = null;
	}

	public void bundleChanged(BundleEvent event) {
		String[] components = extractComponentNames(event.getBundle());

		if (null != components) {

			for (String component : components) {
				switch (event.getType()) {
				case BundleEvent.STARTED:
					logService.log(LogService.LOG_INFO, "Registering "
							+ event.getBundle().getSymbolicName() + "#"
							+ component);
					registerComponent(component, event.getBundle());
					logService.log(LogService.LOG_INFO, "Finished registering "
							+ event.getBundle().getSymbolicName() + "#"
							+ component);
					break;

				case BundleEvent.STOPPING:
					logService.log(LogService.LOG_INFO, "Unregistering "
							+ event.getBundle().getSymbolicName() + "#"
							+ component);
					unregisterComponent(component, event.getBundle());
					break;
				}
			}
		}
	}

	private String[] extractComponentNames(Bundle bundle) {
		Object o = bundle.getHeaders().get("Glueable-Component");
		String[] components = null;
		if (o instanceof String[]) {
			components = (String[]) o;
		} else if (o instanceof String) {
			components = ((String) o).split(",");
		}

		if (null != components) {
			for (int i = 0; i < components.length; i++) {
				components[i] = components[i].trim();
			}
		}

		return components;
	}

	private void registerComponent(String className, Bundle bundle) {
		try {
			Class<?> c = bundle.loadClass(className);
			Object o = c.newInstance();
			Glueable g = (Glueable) o;

			managers.put(className, new GlueManager(logServiceDelegate, g,
					bundle.getBundleContext()));
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	private void unregisterComponent(String className, Bundle bundle) {
		GlueManager mgr = managers.remove(className);
		if (null != mgr) {
			mgr.dispose();
		}
	}

	class LogServiceDelegate implements LogService {

		public void log(int level, String message) {
			log(level, message, null);
		}

		public void log(int level, String message, Throwable exception) {
			log(null, level, message, exception);
		}

		public void log(ServiceReference sr, int level, String message) {
			log(sr, level, message, null);
		}

		public void log(ServiceReference sr, int level, String message,
				Throwable exception) {
			logService.log(sr, level, message);
		}

	}

	class LogServiceTracker extends ServiceTracker implements LogService {

		public LogServiceTracker(BundleContext context) {
			super(context, LogService.class.getName(), null);
		}

		@Override
		public Object addingService(ServiceReference reference) {
			logService = (LogService) context.getService(reference);
			return logService;
		}

		@Override
		public void remove(ServiceReference reference) {
			logService = this;
		}

		public void log(int level, String message) {
		}

		public void log(int level, String message, Throwable exception) {
		}

		public void log(ServiceReference sr, int level, String message) {
		}

		public void log(ServiceReference sr, int level, String message,
				Throwable exception) {
		}
	}

}
