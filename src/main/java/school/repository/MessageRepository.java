package school.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.model.Message;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findBySenderUserPkAndReceiverUserPkOrderByCreatedAtAsc(Long senderUserPk, Long receiverUserPk);

    List<Message> findBySenderUserPkOrReceiverUserPkOrderByCreatedAtDesc(Long senderUserPk, Long receiverUserPk);
}


