package tenant.acme.domain;

public record PrescriptionAggregate(String id) {
    // Rule 1: All state = fold(events)
    public PrescriptionAggregate apply(Object event) {
        return this; // fold logic generated from EventSchema
    }
}
