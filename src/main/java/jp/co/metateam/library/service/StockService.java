package jp.co.metateam.library.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jp.co.metateam.library.constants.Constants;
import jp.co.metateam.library.model.BookMst;
import jp.co.metateam.library.model.CalendarDto;
import jp.co.metateam.library.model.Stock;
import jp.co.metateam.library.model.StockDto;
import jp.co.metateam.library.repository.BookMstRepository;
import jp.co.metateam.library.repository.RentalManageRepository;
import jp.co.metateam.library.repository.StockRepository;

@Service
public class StockService {

    private final BookMstRepository bookMstRepository;
    private final StockRepository stockRepository;
    private final RentalManageRepository rentalManageRepository;

    @Autowired
    public StockService(
            BookMstRepository bookMstRepository,
            StockRepository stockRepository,
            RentalManageRepository rentalManageRepository) {
        this.bookMstRepository = bookMstRepository;
        this.stockRepository = stockRepository;
        this.rentalManageRepository = rentalManageRepository;
    }

    @Transactional
    public List<Stock> findAll() {
        return this.stockRepository.findByDeletedAtIsNull();
    }

    @Transactional
    public List<Stock> findStockAvailableAll() {
        return this.stockRepository
                .findByDeletedAtIsNullAndStatus(
                        Constants.STOCK_AVAILABLE);
    }

    @Transactional
    public Stock findById(String id) {
        return this.stockRepository.findById(id).orElse(null);
    }

    @Transactional
    public List<Object[]> findTitleAndStockCount() {
        return this.stockRepository.findTitleAndStockCount();
    }

    @Transactional
    public Long findUnavailableDates(String title, LocalDate day) {
        return this.stockRepository.findUnavailableDates(title, day);
    }

    @Transactional
    public List<String> findAvailableDates(String title) {
        return this.stockRepository.findAvailableDates(title);
    }

    @Transactional
    public void save(StockDto stockDto) throws Exception {

        BookMst bookMst = this.bookMstRepository
                .findById(stockDto.getBookId())
                .orElse(null);

        if (bookMst == null) {
            throw new Exception("BookMst record not found.");
        }

        Stock stock = new Stock();

        stock.setId(stockDto.getId());
        stock.setBookMst(bookMst);
        stock.setStatus(stockDto.getStatus());
        stock.setPrice(stockDto.getPrice());

        this.stockRepository.save(stock);
    }

    @Transactional
    public void update(String id, StockDto stockDto)
            throws Exception {

        Stock stock = findById(id);

        if (stock == null) {
            throw new Exception("Stock record not found.");
        }

        BookMst bookMst = stock.getBookMst();

        if (bookMst == null) {
            throw new Exception("BookMst record not found.");
        }

        stock.setId(stockDto.getId());
        stock.setBookMst(bookMst);
        stock.setStatus(stockDto.getStatus());
        stock.setPrice(stockDto.getPrice());

        this.stockRepository.save(stock);
    }

    public List<Object> generateDaysOfWeek(
            int year,
            int month,
            LocalDate startDate,
            int daysInMonth) {

        List<Object> daysOfWeek = new ArrayList<>();

        for (int dayOfMonth = 1; dayOfMonth <= daysInMonth; dayOfMonth++) {

            LocalDate date = LocalDate.of(year, month, dayOfMonth);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(
                    "dd(E)",
                    Locale.JAPANESE);

            daysOfWeek.add(date.format(formatter));
        }

        return daysOfWeek;
    }

    public List<List<CalendarDto>> generateValues(
            Integer year,
            Integer month,
            Integer daysInMonth) {

        List<Object[]> titleCount = findTitleAndStockCount();

        List<List<CalendarDto>> stock = new ArrayList<>();

        for (Object[] data : titleCount) {

            List<CalendarDto> daysCount = new ArrayList<>();

            String title = (String) data[0];

            Long stockCount = ((Number) data[1]).longValue();

            // ← ここで1回だけ取得
            List<String> availableStocks = findAvailableDates(title);

            for (int i = 1; i <= daysInMonth; i++) {

                LocalDate day = LocalDate.of(year, month, i);

                CalendarDto calendarDto = new CalendarDto();

                calendarDto.setTitle(title);

                calendarDto.setStockCount(stockCount);

                calendarDto.setExpectedRentalOn(day);

                Long unavailableCount = findUnavailableDates(title, day);

                if (!availableStocks.isEmpty()) {

                    calendarDto.setStockId(
                            availableStocks.get(0));
                }

                Long dayStockCount = stockCount - unavailableCount;

                if (dayStockCount > 0) {

                    calendarDto.setDayStockCount(
                            String.valueOf(dayStockCount));

                } else {

                    calendarDto.setDayStockCount("×");
                }

                daysCount.add(calendarDto);
            }

            stock.add(daysCount);
        }

        return stock;
    }
}