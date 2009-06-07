package uk.co.arum.osgi.glue.sample.bundle;

import org.osgi.framework.BundleContext;
import org.osgi.service.log.LogService;

import uk.co.arum.osgi.glue.Activatable;
import uk.co.arum.osgi.glue.Contextual;
import uk.co.arum.osgi.glue.Glueable;

public class GlueableSample implements Glueable, Activatable, Contextual {

	public void bindContext(BundleContext context) {
		System.out
				.println(getClass().getName() + " : bindContext : " + context);
	}

	public void unbindContext(BundleContext context) {
		System.out.println(getClass().getName() + " : unbindContext : "
				+ context);
	}

	public void activate() throws Exception {
		System.out.println(getClass().getName() + " : activate");
	}

	public void deactivate() throws Exception {
		System.out.println(getClass().getName() + " : deactivate");
	}

	public void optionalBind(LogService service) {
		System.out.println(getClass().getName() + " : optionalBind : "
				+ service);
	}

	public void optionalUnbind(LogService service) {
		System.out.println(getClass().getName() + " : optionalUnbind : "
				+ service);
	}

	public String getServiceFilter(String serviceName, String name) {
		System.out.println(getClass().getName() + " : getServiceFilter("
				+ serviceName + ", " + name + ")");
		return null;
	}

}
