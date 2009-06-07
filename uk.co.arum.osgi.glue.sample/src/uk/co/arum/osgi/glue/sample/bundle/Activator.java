package uk.co.arum.osgi.glue.sample.bundle;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

	public void start(BundleContext context) throws Exception {

		context.registerService(GlueableSample.class.getName(),
				new GlueableSample(), null);

	}

	public void stop(BundleContext context) throws Exception {
	}

}
