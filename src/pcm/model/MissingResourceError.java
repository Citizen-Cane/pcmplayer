/**
 * 
 */
package pcm.model;

/**
 * @author someone
 *
 */
public class MissingResourceError extends ValidationError {
    private static final long serialVersionUID = 1L;

    public MissingResourceError(Action action, String resourceName,
            Script script) {
        super(action, resourceName, script);
    }
}
