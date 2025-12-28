package dev.coffer.adapter.fabric.execution;

import dev.coffer.adapter.fabric.config.CurrencyConfig;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Objects;

/**
 * Formats currency amounts according to configured currency definition.
 *
 * Assumes all adapter values are stored in the minor unit for the currency
 * (decimals represents the shift).
 */
public final class MoneyFormatter {

    private final CurrencyConfig.CurrencyDefinition currency;

    public MoneyFormatter(CurrencyConfig.CurrencyDefinition currency) {
        this.currency = Objects.requireNonNull(currency, "currency");
    }

    public CurrencyConfig.CurrencyDefinition currency() {
        return currency;
    }

    public String format(long amountMinorUnits) {
        if (currency.decimals() == 0) {
            return decorate(Long.toString(amountMinorUnits), amountMinorUnits);
        }
        BigDecimal value =
                BigDecimal.valueOf(amountMinorUnits, currency.decimals());
        return decorate(value.stripTrailingZeros().toPlainString(), amountMinorUnits);
    }

    private String decorate(String numeric, long amountMinorUnits) {
        if (!currency.symbol().isBlank()) {
            return currency.symbol() + numeric;
        }
        String unit = Math.abs(amountMinorUnits) == 1 ? currency.name() : currency.pluralName();
        return numeric + " " + unit;
    }

    public String describe() {
        String base = currency.name();
        if (!currency.symbol().isBlank()) {
            base = currency.symbol() + " (" + currency.name() + ")";
        }
        return base + ", decimals=" + currency.decimals();
    }
}
