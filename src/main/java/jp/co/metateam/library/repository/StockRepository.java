package jp.co.metateam.library.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jp.co.metateam.library.model.Stock;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {

    List<Stock> findAll();

    List<Stock> findByDeletedAtIsNull();

    List<Stock> findByDeletedAtIsNullAndStatus(Integer status);

    Optional<Stock> findById(String id);

    List<Stock> findByBookMst_IdAndStatus(Long bookId, Integer status);

    // タイトルごとの在庫数
    @Query(value = """
            SELECT bm.title, COUNT(st.id) AS availableStockCount
            FROM book_mst bm
            LEFT JOIN stocks st
                ON bm.id = st.book_id
                AND st.status = 0
            GROUP BY bm.title
            """, nativeQuery = true)
    List<Object[]> findTitleAndStockCount();

    // 指定日に貸出中か確認
    @Query(value = """
            SELECT COUNT(*)
            FROM rental_manage rm
            JOIN stocks s
                ON rm.stock_id = s.id
            JOIN book_mst bm
                ON s.book_id = bm.id
            WHERE rm.expected_rental_on <= :day
            AND :day <= rm.expected_return_on
            AND bm.title = :title
            """, nativeQuery = true)
    Long findUnavailableDates(
            @Param("title") String title,
            @Param("day") LocalDate day);

    // 利用可能な在庫取得
    @Query(value = "SELECT s.id " +
            "FROM stocks s " +
            "JOIN book_mst b ON b.id = s.book_id " +
            "LEFT JOIN rental_manage rm ON rm.stock_id = s.id " +
            "WHERE b.title = :title " +
            "AND s.status = '0'", nativeQuery = true)
    List<String> findAvailableDates(@Param("title") String title);
}