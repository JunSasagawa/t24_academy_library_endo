package jp.co.metateam.library.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jp.co.metateam.library.repository.BookMstRepository;

@Service
public class BookMstDeleteService {

    private final BookMstRepository bookMstRepository;

    @Autowired
    public BookMstDeleteService(BookMstRepository bookMstRepository){
        this.bookMstRepository = bookMstRepository;
    }

    public long findCount(Long id) {
        return bookMstRepository.findCount(id);
    }

    @Transactional
    public void deleteById(Long id) {
        bookMstRepository.deleteById(id);
    }

    @Transactional
    public void deleteAll() {
        bookMstRepository.deleteAll();
    }
}
