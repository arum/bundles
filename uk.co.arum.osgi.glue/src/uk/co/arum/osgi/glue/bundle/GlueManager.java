package uk.co.arum.osgi.glue.bundle;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.log.LogService;

import uk.co.arum.osgi.glue.Activatable;
import uk.co.arum.osgi.glue.Contextual;
import uk.co.arum.osgi.glue.Glueable;
import uk.co.arum.osgi.glue.GlueableService;

/**
 * @author brindy
 */
public class GlueManager {

	private boolean active;

	private final BundleContext glueableContext;

	private final Map<String, Bindings> requiredBindingMethods = new HashMap<String, Bindings>();

	private final Map<String, Bindings> optionalBindingMethods = new HashMap<String, Bindings>();

	private final Set<ServiceRegistration> registrations = new HashSet<ServiceRegistration>();

	private Glueable glueable;

	private LogService logService;

	public GlueManager(LogService logService, Glueable glueable,
			BundleContext glueableContext) throws NoSuchMethodException {
		this.logService = logService;
		this.glueable = glueable;
		this.glueableContext = glueableContext;

		logService.log(LogService.LOG_DEBUG, "Managing : "
				+ glueable.getClass().getName());

		extractRequiredBindings(glueable);
		extractOptionalBindings(glueable);

		// try and satisfy all the bindings
		Set<Bindings> allBindings = new HashSet<Bindings>();
		allBindings.addAll(requiredBindingMethods.values());
		allBindings.addAll(optionalBindingMethods.values());

		for (Bindings bindings : allBindings) {

			logService.log(LogService.LOG_DEBUG, "Searching for existing "
					+ bindings.serviceName + "[" + bindings.serviceFilter
					+ "] for " + glueable);
			try {
				ServiceReference[] refs = glueableContext.getServiceReferences(
						bindings.serviceName, bindings.serviceFilter);
				if (null != refs) {
					for (ServiceReference ref : refs) {
						bindings.serviceRegistered(glueableContext
								.getService(ref));
					}
				}
			} catch (InvalidSyntaxException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		// might be that this class doesn't require any bindings,
		// so check to be sure
		if (!active && allBindings.size() == 0) {
			check();
		}

	}

	private void extractBindings(Glueable glueable, String bindMethodPrefix,
			String unbindMethodPrefix, Map<String, Bindings> bindingMethods) {

		Method[] methods = glueable.getClass().getMethods();
		for (Method bindMethod : methods) {
			if (bindMethod.getName().startsWith(bindMethodPrefix)
					&& !bindMethod.getName().equals("bindContext")
					&& bindMethod.getParameterTypes().length == 1) {

				Bindings bindings;
				Class<?> paramType = bindMethod.getParameterTypes()[0];
				if (paramType.isArray()) {
					bindings = new Bindings(paramType.getComponentType()
							.getName());
					bindings.multiple = true;
				} else {
					bindings = new Bindings(paramType.getName());
				}
				bindings.bindMethod = bindMethod;

				// look for the extra part to the binding name
				String extra = "";
				if (!bindMethod.getName().equals(bindMethodPrefix)) {
					extra = bindMethod.getName().substring(
							bindMethodPrefix.length());
				}

				String actualUnbindMethodName = unbindMethodPrefix + extra;
				try {
					bindings.unbindMethod = glueable.getClass().getMethod(
							actualUnbindMethodName, paramType);
				} catch (NoSuchMethodException e) {
					// ignore
				}

				// we don't care if there's no unbind method - that's up to the
				// developer to decide
				if (null == bindings.unbindMethod) {
					logService.log(LogService.LOG_INFO, "No matching "
							+ unbindMethodPrefix + " method found #"
							+ actualUnbindMethodName + "("
							+ paramType.getName() + ")");
				}

				bindings.serviceFilter = glueable.getServiceFilter(
						bindings.serviceName, extra);

				bindingMethods.put(bindings.serviceName, bindings);

				try {
					glueableContext.addServiceListener(bindings,
							bindings.serviceFilter);
				} catch (Exception e) {
					e.printStackTrace();
					throw new RuntimeException(e);
				}
			}
		}
	}

	private void extractOptionalBindings(Glueable glueable) {
		extractBindings(glueable, "optionalBind", "optionalUnbind",
				optionalBindingMethods);
	}

	private void extractRequiredBindings(Glueable glueable)
			throws NoSuchMethodException {
		extractBindings(glueable, "bind", "unbind", requiredBindingMethods);
	}

	@SuppressWarnings("unchecked")
	private void doActivate() {
		logService.log(LogService.LOG_DEBUG, "Activating " + glueable);
		try {

			if (glueable instanceof Contextual) {
				Contextual c = (Contextual) glueable;
				c.bindContext(glueableContext);
			}

			if (glueable instanceof Activatable) {
				Activatable act = (Activatable) glueable;
				act.activate();
			}

			if (glueable instanceof GlueableService) {
				GlueableService service = (GlueableService) glueable;
				for (String serviceName : service.getServiceNames()) {
					Dictionary properties = service.getProperties(serviceName);

					logService.log(LogService.LOG_DEBUG, "Registering "
							+ glueable + " as " + serviceName);
					registrations.add(glueableContext.registerService(
							serviceName, service, properties));
				}
			}

			active = true;

		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} finally {
			if (!active) {
				// something must have gone wrong, so deactivate
				doDeactivate();
			}
		}
	}

	private void doDeactivate() {
		if (!active) {
			return;
		}

		logService.log(LogService.LOG_DEBUG, "Deactivating " + glueable);
		try {
			if (glueable instanceof GlueableService) {
				for (ServiceRegistration reg : registrations) {
					reg.unregister();
				}
				registrations.clear();
			}

			if (glueable instanceof Activatable) {
				try {
					Activatable act = (Activatable) glueable;
					act.deactivate();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			if (glueable instanceof Contextual) {
				Contextual c = (Contextual) glueable;
				c.unbindContext(glueableContext);
			}

		} finally {
			active = false;
		}

	}

	private void check() {
		int satisfied = 0;
		for (Bindings methods : requiredBindingMethods.values()) {
			if (methods.bound == null) {
				logService.log(LogService.LOG_DEBUG, glueable
						+ " not satisfied, missing (" + methods.serviceName
						+ ")");
			} else {
				satisfied++;
				logService.log(LogService.LOG_DEBUG, glueable + " found "
						+ methods.serviceName + " (" + satisfied + "/"
						+ requiredBindingMethods.size() + ")");
			}
		}

		if (!active && satisfied == requiredBindingMethods.size()) {
			doActivate();
		}
	}

	void dispose() {
		if (!active) {
			return;
		}

		doDeactivate();

		// clean up everything else

		for (ServiceRegistration reg : registrations) {
			reg.unregister();
		}
		registrations.clear();

		Set<Bindings> all = new HashSet<Bindings>();
		all.addAll(requiredBindingMethods.values());
		all.addAll(optionalBindingMethods.values());

		for (Bindings methods : all) {
			glueableContext.removeServiceListener(methods);
			try {
				if (null != methods.unbindMethod) {
					methods.unbindMethod.invoke(glueable, methods.bound);
				}
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}

	}

	class Bindings implements ServiceListener {

		private final String uid;

		public Method bindMethod;

		public Method unbindMethod;

		public Object bound;

		public String serviceName;

		public String serviceFilter;

		public boolean multiple;

		private Set<Object> services = new HashSet<Object>();

		public Bindings(String serviceName) {
			this.serviceName = serviceName;
			uid = UUID.randomUUID().toString();
			logService.log(LogService.LOG_DEBUG, uid + " bindings created");
		}

		public void serviceChanged(ServiceEvent event) {

			String[] serviceNames = (String[]) event.getServiceReference()
					.getProperty(Constants.OBJECTCLASS);

			Collection<String> services = Arrays.asList(serviceNames);
			logService.log(LogService.LOG_DEBUG, uid + " " + services);
			if (!services.contains(serviceName)) {
				logService.log(LogService.LOG_DEBUG, uid + " ignoring");
				return;
			}

			try {
				switch (event.getType()) {

				case ServiceEvent.MODIFIED:
					serviceUnregistered(glueableContext.getService(event
							.getServiceReference()));
					serviceRegistered(glueableContext.getService(event
							.getServiceReference()));
					break;

				case ServiceEvent.REGISTERED:
					logService.log(LogService.LOG_DEBUG, uid
							+ " handling REGISTERED");
					serviceRegistered(glueableContext.getService(event
							.getServiceReference()));
					break;

				case ServiceEvent.UNREGISTERING:
					logService.log(LogService.LOG_DEBUG, uid
							+ " handling UNREGISTERING");
					serviceUnregistered(glueableContext.getService(event
							.getServiceReference()));
					break;
				}
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}

		}

		void serviceRegistered(Object o) throws Exception {

			if (multiple) {
				if (active) {
					// deactivate
					doDeactivate();

					// unbind
					if (null != unbindMethod) {
						logService.log(LogService.LOG_DEBUG, "Unbinding "
								+ unbindMethod + services);
						unbindMethod.invoke(glueable, services.toArray());
					}
				}

				// add
				services.add(o);

				// rebind
				Object bound = services.toArray();

				bindMethod.invoke(glueable, new Object[] { bound });
				this.bound = bound;

				// check
				check();

			} else if (bound == null) {
				// bind
				bindMethod.invoke(glueable, o);
				bound = o;

				// add
				services.add(o);

				// check
				check();

			} else {
				// just store it for future reference
				services.add(o);

			}

		}

		void serviceUnregistered(Object o) throws Exception {

			if (multiple) {
				if (active) {
					// deactivate
					doDeactivate();

					// unbind
					if (null != unbindMethod) {
						logService.log(LogService.LOG_DEBUG, "Unbinding "
								+ unbindMethod + services);
						unbindMethod.invoke(glueable, bound);
					}
				}

				// remove
				services.remove(o);

				// rebind
				if (services.size() > 0) {
					Object bound = services.toArray();
					bindMethod.invoke(glueable, bound);
					this.bound = bound;

					// check
					check();
				} else {
					this.bound = null;
				}

			} else if (bound == o) {
				if (active) {
					// deactivate
					doDeactivate();

					// unbind
					if (null != unbindMethod) {
						logService.log(LogService.LOG_DEBUG, "Unbinding "
								+ unbindMethod + services);
						unbindMethod.invoke(glueable, o);
					}
				}

				// remove
				services.remove(o);

				// rebind
				if (services.size() > 0) {
					Object bound = services.iterator().next();
					bindMethod.invoke(glueable, bound);
					this.bound = bound;

					// check
					check();
				} else {
					this.bound = null;
				}
			} else {
				// remove
				services.remove(o);
			}

		}

	}

}
