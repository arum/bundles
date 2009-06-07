package uk.co.arum.osgi.glue;

import org.osgi.framework.BundleContext;

/**
 * Your component should implement this interface if it needs a reference to
 * it's bundle's context.
 * <p/>
 * 
 * The context will be bound after other dependent services are bound but before
 * any activation.
 * <p/>
 * 
 * Conversely, the context will be unbound immediately after a deactivation.
 * </p>
 * 
 * @author brindy
 */
public interface Contextual {

	void bindContext(BundleContext context);

	void unbindContext(BundleContext context);

}
