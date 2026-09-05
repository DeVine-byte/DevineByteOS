package tenant.acme.domain;

public record BedAggregate(String id) {
    // Rule 1: All state = fold(events)
    public BedAggregate apply(Object event) {
        return this; // fold logic generated from EventSchema
    }
}
