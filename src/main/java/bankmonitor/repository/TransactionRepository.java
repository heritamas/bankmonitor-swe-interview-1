package bankmonitor.repository;

import bankmonitor.model.TransactionDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<TransactionDTO, Long> { }
