package com.example.simactivation;



import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

private final CustomerRepository customerRepository;

public CustomerController(CustomerRepository customerRepository) {
    this.customerRepository = customerRepository;
}

// Add customer
@PostMapping
public ResponseEntity<?> addCustomer(@RequestBody Customer customer) {

    if (customerRepository.existsByMobile(customer.getMobile())) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body("Mobile number already registered!");
    }

    if (customerRepository.existsBySimNumber(customer.getSimNumber())) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body("SIM number already registered!");
    }

    if (customer.getApplicationReference() == null ||
            customer.getApplicationReference().isBlank()) {
        customer.setApplicationReference(
            "SIM-" + java.util.UUID.randomUUID()
                .toString().substring(0, 8).toUpperCase()
        );
    }

    Customer savedCustomer = customerRepository.save(customer);

    return ResponseEntity.status(HttpStatus.CREATED)
            .body(savedCustomer);
}


//Get all customers - admin session required
@GetMapping
public ResponseEntity<?> getAllCustomers(HttpSession session) {

 Boolean loggedIn =
         (Boolean) session.getAttribute("adminLoggedIn");

 if (!Boolean.TRUE.equals(loggedIn)) {
     return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
             .body("Please login as admin first");
 }

 return ResponseEntity.ok(customerRepository.findAll());
}

// Get customer by ID
@GetMapping("/{id}")
public ResponseEntity<?> getCustomerById(@PathVariable Long id) {

    return customerRepository.findById(id)
            .<ResponseEntity<?>>map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Customer not found with ID: " + id));
}

// Search customer by mobile number
@GetMapping("/search/mobile/{mobile}")
public ResponseEntity<?> searchByMobile(@PathVariable String mobile) {

    Customer customer = customerRepository.findByMobile(mobile);

    if (customer == null) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Customer not found!");
    }

    return ResponseEntity.ok(customer);
}

// Search customer by SIM number
@GetMapping("/search/sim/{simNumber}")
public ResponseEntity<?> searchBySimNumber(
        @PathVariable String simNumber) {

    Customer customer = customerRepository.findBySimNumber(simNumber);

    if (customer == null) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Customer not found!");
    }

    return ResponseEntity.ok(customer);
}

// Update customer
@PutMapping("/{id}")
public ResponseEntity<?> updateCustomer(
        @PathVariable Long id,
        @RequestBody Customer details) {

    Customer customer = customerRepository.findById(id).orElse(null);

    if (customer == null) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Customer not found with ID: " + id);
    }

    if (customerRepository.existsByMobileAndIdNot(
            details.getMobile(), id)) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body("Mobile number already registered!");
    }

    if (customerRepository.existsBySimNumberAndIdNot(
            details.getSimNumber(), id)) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body("SIM number already registered!");
    }

    customer.setFirstName(details.getFirstName());
    customer.setLastName(details.getLastName());
    customer.setEmail(details.getEmail());
    customer.setDateOfBirth(details.getDateOfBirth());
    customer.setAddress(details.getAddress());
    customer.setIdProof(details.getIdProof());
    customer.setMobile(details.getMobile());
    customer.setSimNumber(details.getSimNumber());

    // Keep existing Aadhaar if no new value was provided
    if (details.getAadhaar() != null
            && !details.getAadhaar().isBlank()) {
        customer.setAadhaar(details.getAadhaar());
    }

    Customer updatedCustomer = customerRepository.save(customer);

    return ResponseEntity.ok(updatedCustomer);
}



//Update SIM activation status - admin session required
@PutMapping("/{id}/status")
public ResponseEntity<?> updateActivationStatus(
     @PathVariable Long id,
     @RequestBody java.util.Map<String, String> request,
     HttpSession session) {

 Boolean loggedIn =
         (Boolean) session.getAttribute("adminLoggedIn");

 if (!Boolean.TRUE.equals(loggedIn)) {
     return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
             .body("Please login as admin first");
 }

 Customer customer =
         customerRepository.findById(id).orElse(null);

 if (customer == null) {
     return ResponseEntity.status(HttpStatus.NOT_FOUND)
             .body("Customer not found with ID: " + id);
 }

 if (request == null || request.get("activationStatus") == null) {
     return ResponseEntity.badRequest()
             .body("Activation status is required");
 }

 String status = request.get("activationStatus").trim();

 if (!(status.equalsIgnoreCase("PENDING")
         || status.equalsIgnoreCase("APPROVED")
         || status.equalsIgnoreCase("REJECTED"))) {

     return ResponseEntity.badRequest()
             .body("Invalid status. Use PENDING, APPROVED, or REJECTED.");
 }

 customer.setActivationStatus(status.toUpperCase());

 Customer updatedCustomer = customerRepository.save(customer);

 return ResponseEntity.ok(updatedCustomer);
}


// Delete customer
@DeleteMapping("/{id}")
public ResponseEntity<String> deleteCustomer(@PathVariable Long id) {

    if (!customerRepository.existsById(id)) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Customer not found with ID: " + id);
    }

    customerRepository.deleteById(id);

    return ResponseEntity.ok("Customer deleted successfully!");
}

}