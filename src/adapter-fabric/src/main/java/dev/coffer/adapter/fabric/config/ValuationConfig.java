package dev.coffer.adapter.fabric.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.OptionalLong;
import java.util.Optional;
import java.util.Set;

/**
 * VALUATION CONFIG
 *
 * Rule-based valuation with priority, type, and currency.
 */
public final class ValuationConfig {

    public enum Type { ITEM, TAG, DEFAULT }

    public static final class Rule {
        private final Type type;
        private final String id;
        private final String currencyId;
        private final long value;
        private final int priority;

        public Rule(Type type, String id, String currencyId, long value, int priority) {
            this.type = Objects.requireNonNull(type, "type");
            this.id = id;
            this.currencyId = Objects.requireNonNull(currencyId, "currencyId");
            if (value <= 0) throw new IllegalArgumentException("value must be > 0");
            this.value = value;
            this.priority = priority;
        }

        public Type type() { return type; }
        public String id() { return id; }
        public String currencyId() { return currencyId; }
        public long value() { return value; }
        public int priority() { return priority; }
    }

    private final List<Rule> rules;

    public ValuationConfig(List<Rule> rules) {
        this.rules = Collections.unmodifiableList(new ArrayList<>(rules));
    }

    public static ValuationConfig empty() {
        return new ValuationConfig(List.of());
    }

    public int entryCount() {
        return rules.size();
    }

    public List<Rule> rules() {
        return rules;
    }

    public OptionalLong resolve(String itemId, Set<String> tags, String currencyId) {
        Objects.requireNonNull(itemId, "itemId");
        Objects.requireNonNull(tags, "tags");
        Objects.requireNonNull(currencyId, "currencyId");

        Rule best = null;
        int bestSpecificity = -1;
        for (Rule rule : rules) {
            if (!currencyId.equalsIgnoreCase(rule.currencyId())) continue;
            boolean matches = switch (rule.type()) {
                case ITEM -> itemId.equals(rule.id());
                case TAG -> tags.contains(rule.id());
                case DEFAULT -> true;
            };
            if (!matches) continue;
            int specificity = specificity(rule.type());
            if (best == null
                    || rule.priority() > best.priority()
                    || (rule.priority() == best.priority() && specificity > bestSpecificity)) {
                best = rule;
                bestSpecificity = specificity;
            }
        }
        if (best == null) return OptionalLong.empty();
        return OptionalLong.of(best.value());
    }

    public Optional<Rule> resolveAny(String itemId, Set<String> tags) {
        Objects.requireNonNull(itemId, "itemId");
        Objects.requireNonNull(tags, "tags");

        Rule best = null;
        int bestSpecificity = -1;
        for (Rule rule : rules) {
            boolean matches = switch (rule.type()) {
                case ITEM -> itemId.equals(rule.id());
                case TAG -> tags.contains(rule.id());
                case DEFAULT -> true;
            };
            if (!matches) continue;
            int specificity = specificity(rule.type());
            if (best == null
                    || rule.priority() > best.priority()
                    || (rule.priority() == best.priority() && specificity > bestSpecificity)) {
                best = rule;
                bestSpecificity = specificity;
            }
        }
        return Optional.ofNullable(best);
    }

    private static int specificity(Type t) {
        return switch (t) {
            case ITEM -> 3;
            case TAG -> 2;
            case DEFAULT -> 1;
        };
    }
}
