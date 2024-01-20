package bankmonitor.repository;

import bankmonitor.model.TransactionV2;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionV2Repository extends JpaRepository<TransactionV2, Long>{
}
