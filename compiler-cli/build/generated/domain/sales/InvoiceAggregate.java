package tenant.acme.domain;

public record InvoiceAggregate(String id) {
    // Rule 1: All state = fold(events)
    public InvoiceAggregate apply(Object event) {
        return this; // fold logic generated from EventSchema
    }
}
