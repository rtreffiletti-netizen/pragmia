package io.pragmia.minos.repository;

import io.pragmia.minos.model.Policy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PolicyRepository extends JpaRepository<Policy, String> {
    List<Policy> findByActiveTrueOrderByPriorityAsc();
}
