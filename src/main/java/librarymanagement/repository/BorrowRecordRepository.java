
package librarymanagement.repository;

import librarymanagement.model.BorrowRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BorrowRecordRepository
        extends JpaRepository<BorrowRecord, Long> {

    List<BorrowRecord> findByStudent_IdOrderByIssueDateDesc(
            Long studentId);

    long countByStudent_IdAndStatus(
            Long studentId, String status);

    Optional<BorrowRecord> findByIdAndStudent_IdAndStatus(
            Long id, Long studentId, String status);

    List<BorrowRecord> findByStatusOrderByDueDateAsc(
            String status);

    List<BorrowRecord> findByStatusAndDueDateBefore(
            String status, LocalDate date);

    long countByStatus(String status);

    long countByBook_Id(Long bookId);
}
