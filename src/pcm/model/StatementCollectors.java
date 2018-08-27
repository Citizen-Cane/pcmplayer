package pcm.model;

import java.util.EnumMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

import pcm.model.AbstractAction.Statement;

public class StatementCollectors implements Iterable<StatementCollector> {
    static final class Factory {
        private final Map<Statement, Supplier<StatementCollector>> collectors = new EnumMap<>(Statement.class);

        void add(Statement statement, Supplier<StatementCollector> supplier) {
            collectors.put(statement, supplier);
        }

        Supplier<StatementCollector> get(Statement stateent) {
            return collectors.get(stateent);
        }
    }

    private final Map<Statement, StatementCollector> entries = new EnumMap<>(Statement.class);
    private final StatementCollectors.Factory factory;

    public StatementCollectors(StatementCollectors.Factory factory) {
        this.factory = factory;
    }

    public boolean contains(Statement statement) {
        return factory.get(statement) != null;
    }

    public StatementCollector get(Statement statement) {
        return entries.computeIfAbsent(statement, this::newCollector);
    }

    private StatementCollector newCollector(Statement statement) {
        StatementCollector collector = factory.get(statement).get();
        collector.init();
        return collector;
    }

    @Override
    public Iterator<StatementCollector> iterator() {
        return entries.values().iterator();
    }

    public Stream<StatementCollector> stream() {
        return entries.values().stream();
    }
}