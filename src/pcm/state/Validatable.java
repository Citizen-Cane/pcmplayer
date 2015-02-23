/**
 * 
 */
package pcm.state;

import java.util.List;

import pcm.model.Action;
import pcm.model.Script;
import pcm.model.ValidationError;

/**
 * @author someone
 *
 */
public interface Validatable {
    void validate(Script script, Action action,
            List<ValidationError> validationErrors);
}
