package uk.co.arum.osgi.glue.bundle;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.SynchronousBundleListener;
import org.osgi.service.log.LogEntry;
import org.osgi.service.log.LogReaderService;
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

	private LogReaderService logReaderService;

	// TODO disable/remove/configure this
	private SimpleLogListener logListener = new SimpleLogListener();

	public void start(BundleContext context) throws Exception {

		LogServiceTracker tracker = new LogServiceTracker(context);
		logService = tracker;
		tracker.open();

		ServiceReference ref = context
				.getServiceReference(LogReaderService.class.getName());
		if (null != ref) {
			logReaderService = (LogReaderService) context.getService(ref);
			logReaderService.addLogListener(logListener);
		}

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
		if (null != logReaderService) {
			logReaderService.removeLogListener(logListener);
		}
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
					System.out.println("Registering "
							+ event.getBundle().getSymbolicName() + "#"
							+ component);
					registerComponent(component, event.getBundle());
					System.out.println("Finished registering "
							+ event.getBundle().getSymbolicName() + "#"
							+ component);
					break;

				case BundleEvent.STOPPING:
					System.out.println("Unregistering "
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

			managers.put(className, new GlueManager(logService, g, bundle
					.getBundleContext()));
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

	class LogServiceTracker extends ServiceTracker implements LogService {

		public LogServiceTracker(BundleContext context) {
			super(context, LogService.class.getName(), null);
		}

		@Override
		public Object addingService(ServiceReference reference) {
			logService = (LogService) context.getService(reference);
			updateManagers();
			return logService;
		}

		@Override
		public void remove(ServiceReference reference) {
			logService = this;
			updateManagers();
		}

		private void updateManagers() {
			if (null == Activator.this.managers) {
				return;
			}

			Set<GlueManager> managers = new HashSet<GlueManager>();
			managers.addAll(Activator.this.managers.values());
			for (GlueManager mgr : managers) {
				mgr.setLogService(logService);
			}
		}

		public void log(int level, String message) {
			log(level, message, null);
		}

		public void log(int level, String message, Throwable exception) {
			log(null, level, message);
		}

		public void log(ServiceReference sr, int level, String message) {
			log(sr, level, message);
		}

		public void log(ServiceReference sr, int level, String message,
				Throwable exception) {
			logListener.logged(new SimpleLogEntry(null, level, message, sr));
		}

	}

	class SimpleLogEntry implements LogEntry {

		private final Throwable ex;
		private final int level;
		private final String message;
		private final ServiceReference ref;
		private final long time;

		public SimpleLogEntry(Throwable ex, int level, String message,
				ServiceReference ref) {
			super();
			this.ex = ex;
			this.level = level;
			this.message = message;
			this.ref = ref;
			this.time = System.currentTimeMillis();
		}

		public Bundle getBundle() {
			return null;
		}

		public Throwable getException() {
			return ex;
		}

		public int getLevel() {
			return level;
		}

		public String getMessage() {
			return message;
		}

		public ServiceReference getServiceReference() {
			return ref;
		}

		public long getTime() {
			return time;
		}

	}

}
