package jp.co.metateam.library.controller;

import java.util.List;
import java.util.Optional;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import jp.co.metateam.library.service.AccountService;
import jp.co.metateam.library.model.RentalManage;
import jp.co.metateam.library.model.RentalManageDto;
import jp.co.metateam.library.service.RentalManageService;
import jp.co.metateam.library.service.RentalManageStatusService;
import jp.co.metateam.library.service.StockService;
import jp.co.metateam.library.values.RentalStatus;
import jp.co.metateam.library.model.Account;
import jp.co.metateam.library.model.CalendarDto;
import jp.co.metateam.library.model.Stock;
import lombok.extern.log4j.Log4j2;

/**
 * 貸出管理関連クラスß
 */
@Log4j2
@Controller
public class RentalManageController {

    private final AccountService accountService;
    private final RentalManageService rentalManageService;
    private final RentalManageStatusService rentalManageStatusService;
    private final StockService stockService;

    @Autowired
    public RentalManageController(
            AccountService accountService,
            RentalManageService rentalManageService,
            RentalManageStatusService rentalManageStatusService,
            StockService stockService) {
        this.accountService = accountService;
        this.rentalManageService = rentalManageService;
        this.rentalManageStatusService = rentalManageStatusService;
        this.stockService = stockService;
    }

    /**
     * 貸出一覧画面初期表示
     * 
     * @param model
     * @return
     */
    @GetMapping("/rental/index")
    public String index(Model model) {
        // 貸出管理テーブルから全件取得
        List<RentalManage> rentalManageList = this.rentalManageService.findAll();

        model.addAttribute("rentalManageList", rentalManageList);

        return "rental/index";
    }

    // 絞り込み機能
    @PostMapping("/rental/index")
    public String index(Model model, Integer status) {

        List<RentalManage> statusList = null;
        List<RentalManage> rentalManageList = null;

        if (status != null) {
            statusList = this.rentalManageStatusService.rentalStatus(status);
        } else {
            rentalManageList = this.rentalManageService.findAll();
        }

        if (statusList != null) {
            model.addAttribute("rentalManageList", statusList);
        } else if (rentalManageList != null) {
            model.addAttribute("rentalManageList", rentalManageList);
        }

        return "rental/index";
    }

    // 貸出一覧画面に渡すデータをmodelに追加
    @GetMapping("/rental/add")
    public String add(CalendarDto calendarDto, Model model,
            @RequestParam(value = "stockId", required = false) String StockId,
            @RequestParam(value = "expectedRentalOn", required = false) @DateTimeFormat(pattern = "yyyy/MM/dd") Date expectedRentalOn) {
        List<Account> accounts = this.accountService.findAll();
        List<Stock> stockList = this.stockService.findStockAvailableAll();

        model.addAttribute("accounts", accounts);
        model.addAttribute("stockList", stockList);
        model.addAttribute("rentalStatus", RentalStatus.values());

        if (!model.containsAttribute("rentalManageDto")) {
            model.addAttribute("rentalManageDto", new RentalManageDto());

            RentalManageDto rentalManageDto = new RentalManageDto();

            if (expectedRentalOn != null) {
                rentalManageDto.setExpectedRentalOn(expectedRentalOn);
            }
            if (StockId != null) {
                rentalManageDto.setStockId(StockId);

            }
            model.addAttribute("rentalManageDto", rentalManageDto);
        }

        return "rental/add";
    }

    @PostMapping("/rental/add")
    public String save(@Valid @ModelAttribute RentalManageDto rentalManageDto, BindingResult result,
            RedirectAttributes ra, Model model) {
        try {

            Date expectedRentalOn = rentalManageDto.getExpectedRentalOn();
            Date expectedReturnOn = rentalManageDto.getExpectedReturnOn();

            Optional<String> dateError = rentalManageDto.ValidDateTime(expectedRentalOn, expectedReturnOn);

            if (dateError.isPresent()) {
                FieldError fieldError = new FieldError("rentalManageDto", "expectedReturnOn", dateError.get());

                result.addError(fieldError);

                throw new Exception("Validation error.");
            }

            // 貸出可否チェック
            String stockId = rentalManageDto.getStockId();
            Integer status = rentalManageDto.getStatus();

            Long stockcount = this.rentalManageService.countByStockIdAndStatusIn(stockId);

            if (status == 0 || status == 1) {

                if (!(stockcount == 0)) {

                    Long rentalcount = this.rentalManageService.countByStockIdAndStatusAndTermsIn(stockId,
                            expectedReturnOn, expectedRentalOn);

                    if (!(stockcount == rentalcount)) {
                        String rentalError = "この期間は貸出できません";

                        result.addError(new FieldError("rentalmanageDto", "expectedRentalOn", rentalError));
                        result.addError(new FieldError("rentalmanageDto", "expectedReturnOn", rentalError));

                    }
                }
            }

            if (result.hasErrors()) {
                throw new Exception("Validation error.");
            }

            // 登録処理
            this.rentalManageService.save(rentalManageDto);

            return "redirect:/rental/index";
            
        } catch (Exception e) {
            log.error(e.getMessage());

            List<Account> accounts = this.accountService.findAll();
            List<Stock> stockList = this.stockService.findStockAvailableAll();

            model.addAttribute("accounts", accounts);
            model.addAttribute("stockList", stockList);
            model.addAttribute("rentalStatus", RentalStatus.values());

            ra.addFlashAttribute("rentalManageDto", rentalManageDto);
            ra.addFlashAttribute("org.springframework.validation.BindingResult.rentalManageDto", result);

            return "rental/add";
        }
    }

    @GetMapping("/rental/{id}/edit")
    public String edit(@PathVariable("id") String id, Model model) {
        List<Account> accounts = this.accountService.findAll();
        List<Stock> stockList = this.stockService.findStockAvailableAll();

        model.addAttribute("accounts", accounts);
        model.addAttribute("stockList", stockList);
        model.addAttribute("rentalStatus", RentalStatus.values());

        if (!model.containsAttribute("rentalManageDto")) {
            model.addAttribute("rentalManageDto", new RentalManageDto());

            RentalManageDto rentalManageDto = new RentalManageDto();
            RentalManage rentalManage = this.rentalManageService.findById(Long.valueOf(id));

            rentalManageDto.setId(rentalManage.getId());
            rentalManageDto.setEmployeeId(rentalManage.getAccount().getEmployeeId());
            rentalManageDto.setStockId(rentalManage.getStock().getId());
            rentalManageDto.setStatus(rentalManage.getStatus());
            rentalManageDto.setExpectedRentalOn(rentalManage.getExpectedRentalOn());
            rentalManageDto.setExpectedReturnOn(rentalManage.getExpectedReturnOn());

            model.addAttribute("rentalManageDto", rentalManageDto);
        }

        return "rental/edit";
    }

    @PostMapping("/rental/{id}/edit")
    public String update(@PathVariable("id") String id, @Valid @ModelAttribute RentalManageDto rentalManageDto,
            BindingResult result, RedirectAttributes ra, Model model) {
        try {

            Date expectedRentalOn = rentalManageDto.getExpectedRentalOn();
            Date expectedReturnOn = rentalManageDto.getExpectedReturnOn();

            Optional<String> dateError = rentalManageDto.ValidDateTime(expectedRentalOn, expectedReturnOn);

            if (dateError.isPresent()) {
                FieldError fieldError = new FieldError("rentalManageDto", "expectedReturnOn", dateError.get());

                result.addError(fieldError);

                throw new Exception("Validation error.");
            }

            RentalManage rentalManage = this.rentalManageService.findById(Long.valueOf(id));

            String validerror = rentalManageDto.validateStatus(rentalManage.getStatus());
            if (validerror != null) {
                result.addError(new FieldError("rentalManageDto", "status", validerror));
            }

            String stockId = rentalManageDto.getStockId();
            Long ID = rentalManageDto.getId();
            Integer status = rentalManageDto.getStatus();

            Long stockcount = this.rentalManageService.countByStockIdAndStatusInAndIdNot(stockId, ID);

            if (status == 0 || status == 1) {

                if (!(stockcount == 0)) {

                    Long rentalcount = this.rentalManageService.countByStockIdAndStatusAndIdNotAndTermsIn(stockId, ID,
                            expectedReturnOn, expectedRentalOn);

                    if (!(stockcount == rentalcount)) {
                        String rentalError = "この期間は貸出できません";
                        result.addError(new FieldError("rentalmanageDto", "expectedRentalOn", rentalError));
                        result.addError(new FieldError("rentalmanageDto", "expectedReturnOn", rentalError));

                    }
                }

            }

            if (result.hasErrors()) {
                throw new Exception("Validation error.");
            }

            // 変更処理
            rentalManageService.update(Long.valueOf(id), rentalManageDto);

            return "redirect:/rental/index";
            
        } catch (Exception e) {
            log.error(e.getMessage());

            ra.addFlashAttribute("rentalManageDto", rentalManageDto);
            ra.addFlashAttribute("org.springframework.validation.BindingResult.rentalManageDto", result);

            List<Account> accounts = this.accountService.findAll();
            List<Stock> stockList = this.stockService.findStockAvailableAll();

            model.addAttribute("accounts", accounts);
            model.addAttribute("stockList", stockList);
            model.addAttribute("rentalStatus", RentalStatus.values());

            return "rental/edit";

        }
    }
}