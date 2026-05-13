package jp.co.metateam.library.model;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Optional;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jp.co.metateam.library.values.RentalStatus;
import lombok.Getter;
import lombok.Setter;

/**
 * 貸出管理DTO
 */
@Getter
@Setter
public class RentalManageDto {

    private Long id;

    @NotEmpty(message="在庫管理番号は必須です")
    private String stockId;

    @NotEmpty(message="社員番号は必須です")
    private String employeeId;

    @NotNull(message="貸出ステータスは必須です")
    private Integer status;

    @DateTimeFormat(pattern="yyyy-MM-dd")
    @NotNull(message="貸出予定日は必須です")
    private Date expectedRentalOn;

    @DateTimeFormat(pattern="yyyy-MM-dd")
    @NotNull(message="返却予定日は必須です")
    private Date expectedReturnOn;

    private Timestamp rentaledAt;

    private Timestamp returnedAt;

    private Timestamp canceledAt;

    private Stock stock;

    private Account account;
    

    public Optional<String> ValidDateTime(Date expectedRentalOn , Date expectedReturnOn) {
        if (expectedRentalOn.compareTo(expectedReturnOn) >= 0) {
            return Optional.of("返却予定日は貸出予定日より後の日付を入力してください");
        }
        return Optional.empty();
    }




    public String validateStatus(Integer previousRentalStatus) {
 
        /*全部Noの場合にエラー
    　　    貸出待ち→貸出中
         * 貸出待ち→キャンセル
         * 貸出中→貸出待ち
         * 貸出中→返却済み
         */
        if (previousRentalStatus == RentalStatus.RENT_WAIT.getValue() && this.status == RentalStatus.RENTAlING.getValue()) {
            return null;
        }
        else if (previousRentalStatus == RentalStatus.RENT_WAIT.getValue() && this.status == RentalStatus.CANCELED.getValue()) {
            return null;
        }
        else if (previousRentalStatus == RentalStatus.RENTAlING.getValue() && this.status == RentalStatus.RENT_WAIT.getValue()) {
            return null;
        }
        else if (previousRentalStatus == RentalStatus.RENTAlING.getValue() && this.status == RentalStatus.RETURNED.getValue()) {
            return null;
        }
        else if (previousRentalStatus == RentalStatus.RENT_WAIT.getValue() && this.status == RentalStatus.RENT_WAIT.getValue()) {
            return null;
        }
        else if (previousRentalStatus == RentalStatus.RENT_WAIT.getValue() && this.status == RentalStatus.RETURNED.getValue()) {
            return String.format("貸出ステータスを貸出待ちから返却済みに編集することはできません", previousRentalStatus, this.status);
        }
        else if (previousRentalStatus == RentalStatus.RENTAlING.getValue() && this.status == RentalStatus.CANCELED.getValue()) {
            return String.format("貸出ステータスを貸出中からキャンセルに編集することはできません", previousRentalStatus, this.status);
        }
        else if (previousRentalStatus == RentalStatus.RENTAlING.getValue() && this.status == RentalStatus.RENTAlING.getValue()) {
            return String.format("貸出ステータスを貸出中から貸出中に編集することはできません", previousRentalStatus, this.status);
        }
        else if (previousRentalStatus == RentalStatus.CANCELED.getValue() && this.status == RentalStatus.CANCELED.getValue()) {
            return String.format("貸出ステータスをキャンセルからキャンセルに編集することはできません", previousRentalStatus, this.status);
        }
        else if (previousRentalStatus == RentalStatus.RETURNED.getValue() && this.status == RentalStatus.RETURNED.getValue()) {
            return String.format("貸出ステータスを返却済みから返却済みに編集することはできません", previousRentalStatus, this.status);
        }
        else if (previousRentalStatus == RentalStatus.RETURNED.getValue() && this.status == RentalStatus.CANCELED.getValue()) {
            return String.format("貸出ステータスを返却済みからキャンセルに編集することはできません", previousRentalStatus, this.status);
        }
        else if (previousRentalStatus == RentalStatus.RETURNED.getValue() && this.status == RentalStatus.RENT_WAIT.getValue()) {
            return String.format("貸出ステータスを返却済みから貸出待ちに編集することはできません", previousRentalStatus, this.status);
        }
        else if (previousRentalStatus == RentalStatus.RETURNED.getValue() && this.status == RentalStatus.RENTAlING.getValue()) {
            return String.format("貸出ステータスを返却済みから貸出中に編集することはできません", previousRentalStatus, this.status);
        }
        else if (previousRentalStatus == RentalStatus.CANCELED.getValue() && this.status == RentalStatus.RENTAlING.getValue()) {
            return String.format("貸出ステータスをキャンセルから貸出中に編集することはできません", previousRentalStatus, this.status);
        }
        else if (previousRentalStatus == RentalStatus.CANCELED.getValue() && this.status == RentalStatus.RETURNED.getValue()) {
            return String.format("貸出ステータスをキャンセルから返却済みに編集することはできません", previousRentalStatus, this.status);
        }

        //変数previousにRentalStatusクラスのstatusに一致するtextを取得する
     
        return null;     
    }

}
