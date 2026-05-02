package io.pragmia.canto.repository;

import io.pragmia.canto.model.FlowTree;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FlowTreeRepository extends JpaRepository<FlowTree, String> {
    Optional<FlowTree> findByNameAndActiveTrue(String name);
    Optional<FlowTree> findFirstByActiveTrue();
}
