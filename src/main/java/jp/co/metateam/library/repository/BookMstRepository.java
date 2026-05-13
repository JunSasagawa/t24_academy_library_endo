package jp.co.metateam.library.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import jp.co.metateam.library.model.BookMst;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

public interface BookMstRepository extends JpaRepository<BookMst, Long> {
	List<BookMst> findAll();

	Optional<BookMst> findById(BigInteger id);

	// 削除したい書籍が貸出中か確認
	@Query(value = """
			SELECT COUNT(*)
			FROM rental_manage rm
			JOIN stocks s
			    ON rm.stock_id = s.id
			WHERE s.book_id = :id
			AND rm.status = 1
			""", nativeQuery = true)
	long findCount(Long id);

	// 削除のSQL文
	@Modifying
	@Transactional
	@Query(value = """
			DELETE rm, s
			FROM rental_manage rm
			INNER JOIN stocks s
			    ON s.id = rm.stock_id
			WHERE s.book_id = :id
			""", nativeQuery = true)
	long deleteBook(Long id);

}
