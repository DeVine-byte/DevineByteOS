# DevineByteOS
organization "Acme Corp" {

    tenant "tenant-1"

    entity Customer {
        id: UUID
        name: String
        email: String
    }

    workflow Onboarding {

        state START
        state VERIFY
        state ACTIVE
        state REJECTED

        transition START -> VERIFY
        transition VERIFY -> ACTIVE
        transition VERIFY -> REJECTED

        rule "must verify email"
    }

    event CustomerCreated {
        source: Customer
    }

    policy "email validation" {
        allow if email contains "@"
    }

    kpi "active customers"
}
