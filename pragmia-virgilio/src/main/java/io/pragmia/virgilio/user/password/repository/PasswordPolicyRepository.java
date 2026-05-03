
package io.pragmia.virgilio.user.password.repository;

import io.pragmia.virgilio.user.password.model.PasswordPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PasswordPolicyRepository extends JpaRepository<PasswordPolicy, String> {}
