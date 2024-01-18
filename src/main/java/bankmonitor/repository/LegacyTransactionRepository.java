package bankmonitor.repository;

import bankmonitor.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LegacyTransactionRepository extends JpaRepository<Transaction, Long> { }
