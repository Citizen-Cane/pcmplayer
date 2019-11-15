package pcm.model;

import java.util.Optional;

public class LoadSbdRange extends ActionRange {
    public final String script;

    public LoadSbdRange(String script, ActionRange actionRange) {
        super(actionRange.start, actionRange.end);
        this.script = script;
    }

    @Override
    public Optional<String> script() {
        return Optional.of(script);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((script == null) ? 0 : script.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        LoadSbdRange other = (LoadSbdRange) obj;
        if (script == null) {
            if (other.script != null)
                return false;
        } else if (!script.equals(other.script))
            return false;
        return true;
    }

}
