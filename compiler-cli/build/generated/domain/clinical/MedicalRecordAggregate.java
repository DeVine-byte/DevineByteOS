package tenant.acme.domain;

public record MedicalRecordAggregate(String id) {
    // Rule 1: All state = fold(events)
    public MedicalRecordAggregate apply(Object event) {
        return this; // fold logic generated from EventSchema
    }
}
