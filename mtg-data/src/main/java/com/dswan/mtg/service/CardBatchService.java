package com.dswan.mtg.service;

import com.dswan.mtg.domain.entity.CardEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CardBatchService {
    @PersistenceContext
    private EntityManager entityManager;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveBatch(List<CardEntity> batch) {
        for (CardEntity card : batch) {
            entityManager.merge(card);
        }
        entityManager.flush();
        entityManager.clear();
    }
}
