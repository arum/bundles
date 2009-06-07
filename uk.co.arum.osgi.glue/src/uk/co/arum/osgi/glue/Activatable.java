package uk.co.arum.osgi.glue;


/**
 * Use this interface when you want your component to be activated.
 * 
 * @author brindy
 */
public interface Activatable {

	/** Called when the component is activated. */
	void activate() throws Exception;

	/** Called when the component is deactivated. */
	void deactivate() throws Exception;

}
