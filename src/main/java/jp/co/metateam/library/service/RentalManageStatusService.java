package jp.co.metateam.library.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import jp.co.metateam.library.model.RentalManage;
import jp.co.metateam.library.repository.RentalManageRepository;

@Service
public class RentalManageStatusService {

    @Autowired
    private final RentalManageRepository rentalManageRepository;

    public RentalManageStatusService(RentalManageRepository rentalManageRepository) {
        this.rentalManageRepository = rentalManageRepository;
    }

    @Transactional
    public List <RentalManage> findAll() {
        List <RentalManage> rentalManageList = this.rentalManageRepository.findAll();

        return rentalManageList;
    }
    @Transactional
    public List <RentalManage> rentalStatus(Integer status) {
        List <RentalManage> statusList = this.rentalManageRepository.rentalStatus(status);

        return statusList;
    }
}
