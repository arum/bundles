package uk.co.arum.osgi.glue;

/**
 * Minimum interface required on a component that you want to be automatically
 * created and bound with services from the container.
 * <p/>
 * If you want your component to receive an activation message implement
 * {@link Activatable}.
 * </p>
 * 
 * Glueable objects declare their dependencies by specifying matching bind
 * unbind methods. Glue supports the concept of optional and multiplicity. Once
 * all dependencies are satisfied the component is then considered activated.
 * 
 * <p/>
 * optional (0..1) - services are bound using optionalBind (and unbound
 * using optionalUnbind), otherwise bind must be used. e.g.
 * <p/>
 * <code><pre>
 * // activation will not occur until this has been called
 * public void bind(HttpService service) {
 * ...
 * }
 * 
 * // activation will still occur even if this hasn't been called
 * public void optionalUnbind(LogService service) {
 * ...
 * }
 * </pre></code>
 * 
 * multiplicity (1..n) - a single service should be specified as an instance,
 * multiple services using an array, e.g.
 * <p/>
 * <code><pre>
 * public void bind(HttpService service) {
 * ... 
 * }
 * 
 * public void bind(HttpService[] services) {
 * ...
 * }
 * </pre></code>
 * 
 * Of course it is also possible to have an optional, but multiple dependency:
 * <p/>
 * <code><pre>
 * public void optionalBind(LogService[] services) {
 * }
 * </pre></code>
 * 
 * Remember, matching unbind/optionalUnbind methods must be present for the
 * dependency to be detected.
 * </p>
 * 
 * If a service that is required goes away, Glue will try to bind an alternative
 * if available. If no alternatives are available then the component is
 * deactivated and the unbind method is called..
 * <p/>
 * 
 * In the case of services coming or going for bind methods with multiplicity of
 * <em>n</em> the unbind method will be called with the existing array and then
 * a new array of matching services will be passed to the bind method.
 * <p/>
 * 
 * Finally, in order to have the same type of service injected multiple
 * times but with a different service filter Glue will take any text after the
 * bind or set part of the method name. Matching unbind/optionalUnbind methods
 * must still be specified, e.g.
 * <p/>
 * 
 * <code><pre>
 * public void bind(SomeService service) {
 * }
 * 
 * public void unbind(SomeService service) {
 * }
 * 
 * public void bindSpecial(SomeService service) {
 * }
 * 
 * public void unbindSpecial(SomeService service) {
 * }
 * </pre></code>
 * 
 * In the above example {@link #getServiceFilter(String, String)} will be called
 * twice with the following parameters:
 * <p/>
 * <ul>
 * <li>"fully.qualified.SomeService", null</li>
 * <li>"fully.qualified.SomeService", "Special"</li>
 * </ul>
 * 
 * {@link #getServiceFilter(String, String)} should respond with an appropriate
 * service filter. If both services have the same service filter the behaviour
 * is not specified.
 * <p/>
 * 
 * @author brindy
 * 
 */
public interface Glueable {

	/**
	 * Called to get a service filter for a given service name that is being
	 * bound to this component.</p>
	 * 
	 * @param serviceName
	 *            the name of the OSGi service to lookup
	 * @param name
	 *            any text after 'bind' or 'optionalBind' for a given service,
	 *            blank if none, but always non-null
	 * @return the filter to use
	 */
	String getServiceFilter(String serviceName, String name);

}
