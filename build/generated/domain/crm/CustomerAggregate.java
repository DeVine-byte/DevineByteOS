package tenant.acme.domain;

public record CustomerAggregate(String id) {
    // Rule 1: All state = fold(events)
    public CustomerAggregate apply(Object event) {
        return this; // fold logic generated from EventSchema
    }
}
