package tenant.acme.domain;

public record InsuranceClaimAggregate(String id) {
    // Rule 1: All state = fold(events)
    public InsuranceClaimAggregate apply(Object event) {
        return this; // fold logic generated from EventSchema
    }
}
