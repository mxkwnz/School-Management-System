package school.repository;

import school.model.ParentStudent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParentStudentRepository extends JpaRepository<ParentStudent, Long> {
    List<ParentStudent> findByParentUserId(String parentUserId);
    List<ParentStudent> findByStudentUserId(String studentUserId);
    Optional<ParentStudent> findByParentUserIdAndStudentUserId(String parentUserId, String studentUserId);
}

