package bankmonitor.repository;

import bankmonitor.model.Transaction;
import bankmonitor.model.TransactionV2;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LegacyTransactionRepository extends JpaRepository<Transaction, Long> { }
