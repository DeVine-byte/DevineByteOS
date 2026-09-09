package tenant.acme.domain;

public record PatientAggregate(String id) {
    // Rule 1: All state = fold(events)
    public PatientAggregate apply(Object event) {
        return this; // fold logic generated from EventSchema
    }
}
