package io.pragmia.beatrice.repository;

import io.pragmia.beatrice.model.NlpCommand;
import io.pragmia.beatrice.model.NlpCommandStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NlpCommandRepository extends JpaRepository<NlpCommand, String> {
    List<NlpCommand> findByStatus(NlpCommandStatus status);
    List<NlpCommand> findByRequestedBy(String userId);
}
