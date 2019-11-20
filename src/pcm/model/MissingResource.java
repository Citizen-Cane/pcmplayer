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

    public MissingResource(Script script, Action action, String name) {
        super(script, action, name);
    }
}
