package tenant.acme.domain;

public record PaymentAggregate(String id) {
    // Rule 1: All state = fold(events)
    public PaymentAggregate apply(Object event) {
        return this; // fold logic generated from EventSchema
    }
}
