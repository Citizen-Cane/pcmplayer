/**
 * 
 */
package pcm.model;

/**
 * @author someone
 *
 */
public interface ConditionRange<T> {
    public boolean contains(T condition);
}
