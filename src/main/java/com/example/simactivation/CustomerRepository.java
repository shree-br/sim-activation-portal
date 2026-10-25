
 package com.example.simactivation;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository
        extends JpaRepository<Customer, Long> {

    boolean existsByMobile(String mobile);

    boolean existsBySimNumber(String simNumber);

    boolean existsByMobileAndIdNot(String mobile, Long id);

    boolean existsBySimNumberAndIdNot(String simNumber, Long id);
    Customer findByMobile(String mobile);

    Customer findBySimNumber(String simNumber);
    
    long countByActivationStatusIgnoreCase(String activationStatus);
}
