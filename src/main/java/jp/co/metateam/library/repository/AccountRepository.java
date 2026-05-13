package jp.co.metateam.library.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import jp.co.metateam.library.model.Account;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    Account findByEmail(String email);

    Optional<Account> findByEmployeeId(String employeeId);

}