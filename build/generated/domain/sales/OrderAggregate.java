package tenant.acme.domain;

public record OrderAggregate(String id) {
    // Rule 1: All state = fold(events)
    public OrderAggregate apply(Object event) {
        return this; // fold logic generated from EventSchema
    }
}
