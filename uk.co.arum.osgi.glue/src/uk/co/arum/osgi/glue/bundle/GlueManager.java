package uk.co.arum.osgi.glue.bundle;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
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
 * 
 * 
 * @author brindy
 * 
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

		// check it
		check();
	}

	private void extractBindings(Glueable glueable, String bindMethodName,
			String unbindMethodName, Map<String, Bindings> bindingMethods) {

		Method[] methods = glueable.getClass().getMethods();
		for (Method bindMethod : methods) {
			if (bindMethod.getName().startsWith(bindMethodName)
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
				if (!bindMethod.getName().equals(bindMethodName)) {
					extra = bindMethod.getName().substring(
							bindMethodName.length());
				}

				String actualUnbindMethodName = unbindMethodName + extra;
				try {
					bindings.unbindMethod = glueable.getClass().getMethod(
							actualUnbindMethodName, paramType);
				} catch (NoSuchMethodException e) {
					// ignore
				}
				if (null == bindings.unbindMethod) {
					logService.log(LogService.LOG_DEBUG, "No matching "
							+ unbindMethodName + " method found");
					continue;
				}

				bindings.serviceFilter = glueable.getServiceFilter(
						bindings.serviceName, extra);

				bindingMethods.put(bindings.serviceName, bindings);

				try {
					logService.log(LogService.LOG_DEBUG,
							"Searching for existing " + bindings.serviceName
									+ "[" + bindings.serviceFilter + "] for "
									+ glueable);
					ServiceReference[] refs = glueableContext
							.getServiceReferences(bindings.serviceName,
									bindings.serviceFilter);
					if (null != refs) {
						for (ServiceReference ref : refs) {
							bindings.serviceRegistered(glueableContext
									.getService(ref));
						}
					}

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
		try {
			logService.log(LogService.LOG_DEBUG, "Activating " + glueable);

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
		try {
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
				c.bindContext(glueableContext);
			}
		} finally {
			active = false;
		}

	}

	private void check() {
		boolean satisfied = true;
		for (Bindings methods : requiredBindingMethods.values()) {
			if (methods.bound == null) {
				logService.log(LogService.LOG_DEBUG, glueable
						+ " not satisfied, missing (" + methods.serviceName
						+ ")");
				satisfied = false;
			}
		}

		if (!active && satisfied) {
			doActivate();
		}
	}

	void setLogService(LogService logService) {
		this.logService = logService;
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
				methods.unbindMethod.invoke(glueable, methods.bound);
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}

	}

	class Bindings implements ServiceListener {

		public Method bindMethod;

		public Method unbindMethod;

		public Object bound;

		public String serviceName;

		public String serviceFilter;

		public boolean multiple;

		private Set<Object> services = new HashSet<Object>();

		public Bindings(String serviceName) {
			this.serviceName = serviceName;
		}

		public void serviceChanged(ServiceEvent event) {

			String[] serviceNames = (String[]) event.getServiceReference()
					.getProperty(Constants.OBJECTCLASS);

			Set<String> names = new HashSet<String>();
			Collections.addAll(names, serviceNames);
			if (!names.contains(serviceName)) {
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
					serviceRegistered(glueableContext.getService(event
							.getServiceReference()));
					break;

				case ServiceEvent.UNREGISTERING:
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
					unbindMethod.invoke(glueable, services.toArray());
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
					unbindMethod.invoke(glueable, bound);
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
				}

			} else if (bound == o) {
				if (active) {
					// deactivate
					doDeactivate();

					// unbind
					unbindMethod.invoke(glueable, o);
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
				}
			} else {
				// remove
				services.remove(o);
			}

		}

	}

}
