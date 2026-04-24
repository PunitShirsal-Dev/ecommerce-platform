// Example Spring Cloud Contract (Groovy DSL) for Inventory → Order compatibility.
// Place under `src/test/resources/contracts` and enable `spring-cloud-contract-maven-plugin`
// on the producer (inventory-service) to generate stubs consumed by order-service tests.

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    request {
        method 'GET'
        url '/api/inventory/p1'
        headers {
            header('Authorization', anyNonBlankString())
        }
    }
    response {
        status 200
        headers {
            contentType(applicationJson())
        }
        body([
                productId: 'p1',
                quantityOnHand: 100
        ])
    }
}
