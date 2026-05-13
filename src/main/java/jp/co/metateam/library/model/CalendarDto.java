package jp.co.metateam.library.model;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
public class CalendarDto {

    // 書籍名
    private String title;

    // 総在庫数
    private Long stockCount;

    // 日付ごとの在庫数
    private String dayStockCount;

    // 在庫管理番号
    private String stockId;

    // 貸出予定日
    private LocalDate expectedRentalOn;

}