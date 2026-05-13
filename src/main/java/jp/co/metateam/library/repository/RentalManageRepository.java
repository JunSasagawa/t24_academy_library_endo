package jp.co.metateam.library.repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import jp.co.metateam.library.model.RentalManage;

@Repository
public interface RentalManageRepository extends JpaRepository<RentalManage, Long> {

    List<RentalManage> findAll();

    Optional<RentalManage> findById(Long id);

    // statusで絞り込み
    @Query("""
        SELECT rm
        FROM RentalManage rm
        WHERE rm.status = ?1
    """)
    List<RentalManage> rentalStatus(Integer status);

    // 「貸出待ち」「貸出中」の件数取得
    @Query("""
        SELECT COUNT(rm)
        FROM RentalManage rm
        WHERE rm.stock.id = ?1
        AND rm.status IN (0, 1)
    """)
    long countByStockIdAndStatusIn(String stockId);

    // 自分以外の「貸出待ち」「貸出中」
    @Query("""
        SELECT COUNT(rm)
        FROM RentalManage rm
        WHERE rm.stock.id = ?1
        AND rm.status IN (0, 1)
        AND rm.id <> ?2
    """)
    long countByStockIdAndStatusInAndIdNot(
        String stockId,
        Long id
    );

    // 期間重複チェック
    @Query("""
        SELECT COUNT(rm)
        FROM RentalManage rm
        WHERE rm.stock.id = ?1
        AND rm.status IN (0, 1)
        AND (
            rm.expectedRentalOn <= ?2
            AND rm.expectedReturnOn >= ?3
        )
    """)
    long countByStockIdAndStatusAndTermsIn(
        String stockId,
        Date expectedReturnOn,
        Date expectedRentalOn
    );

    // 自分以外の期間重複チェック
    @Query("""
        SELECT COUNT(rm)
        FROM RentalManage rm
        WHERE rm.stock.id = ?1
        AND rm.status IN (0, 1)
        AND rm.id <> ?2
        AND (
            rm.expectedRentalOn <= ?3
            AND rm.expectedReturnOn >= ?4
        )
    """)
    long countByStockIdAndStatusAndIdNotAndTermsIn(
        String stockId,
        Long id,
        Date expectedReturnOn,
        Date expectedRentalOn
    );
}