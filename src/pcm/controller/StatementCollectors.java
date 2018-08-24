package pcm.controller;

import java.util.EnumMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Supplier;

import pcm.model.AbstractAction.Statement;

final class StatementCollectors implements Iterable<StatementCollector> {
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
        StatementCollector collector = entries.get(statement);
        if (collector == null) {
            collector = factory.get(statement).get();
            collector.init();
        }
        return collector;
    }

    @Override
    public Iterator<StatementCollector> iterator() {
        return entries.values().iterator();
    }

}