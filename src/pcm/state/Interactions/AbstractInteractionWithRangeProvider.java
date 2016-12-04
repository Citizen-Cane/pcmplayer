package pcm.state.Interactions;

import pcm.state.Interaction;
import pcm.state.Interaction.NeedsRangeProvider;

public abstract class AbstractInteractionWithRangeProvider
        extends AbstractInteraction implements NeedsRangeProvider {

    protected Interaction rangeProvider = null;

    @Override
    public void setRangeProvider(Interaction rangeProvider) {
        if (this.rangeProvider != null) {
            if (this.rangeProvider instanceof NeedsRangeProvider) {
                ((NeedsRangeProvider) this.rangeProvider)
                        .setRangeProvider(rangeProvider);
            } else {
                throw new IllegalStateException(rangeProvider.toString());
            }
        } else {
            this.rangeProvider = rangeProvider;
        }
    }
}
