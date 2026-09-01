package tenant.acme.domain;

public record StockAggregate(String id) {
    // Rule 1: All state = fold(events)
    public StockAggregate apply(Object event) {
        return this; // fold logic generated from EventSchema
    }
}
