/**
 * 
 */
package pcm.model;

/**
 * @author someone
 *
 */
public class MissingResource extends ValidationIssue {
    private static final long serialVersionUID = 1L;

    public MissingResource(Action action, String resourceName,
            Script script) {
        super(action, resourceName, script);
    }
}
