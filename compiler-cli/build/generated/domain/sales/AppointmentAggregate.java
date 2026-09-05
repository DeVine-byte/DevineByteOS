package tenant.acme.domain;

public record AppointmentAggregate(String id) {
    // Rule 1: All state = fold(events)
    public AppointmentAggregate apply(Object event) {
        return this; // fold logic generated from EventSchema
    }
}
