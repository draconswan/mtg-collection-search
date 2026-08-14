package com.dswan.mtg.listener;

import com.dswan.mtg.domain.entity.DeckCardEntity;
import com.dswan.mtg.domain.entity.DeckEntity;
import com.dswan.mtg.domain.entity.UserCardCollectionEntity;
import com.dswan.mtg.domain.entity.UserCardDeckAssignmentEntity;
import com.dswan.mtg.repository.DeckRepository;
import com.dswan.mtg.repository.UserCardCollectionRepository;
import com.dswan.mtg.repository.UserCardDeckAssignmentRepository;
import com.dswan.mtg.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        value = "mtg.sync-on-login",
        havingValue = "true"
)
public class UserLoginSyncListener {
    private final DeckRepository deckRepository;
    private final UserRepository userRepository;
    private final UserCardCollectionRepository userCardCollectionRepository;
    private final UserCardDeckAssignmentRepository userCardDeckAssignmentRepository;

    @EventListener
    @Transactional
    public void handleLogin(AuthenticationSuccessEvent event) {
        String username = event.getAuthentication().getName();
        var user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return;
        }
        syncUserDecks(user.getId());
    }

    private void syncUserDecks(Long userId) {
        var decks = deckRepository.findByUserId(userId);
        for (DeckEntity deck : decks) {
            syncDeck(deck);
        }
    }

    private void syncDeck(DeckEntity deck) {
        for (DeckCardEntity dc : deck.getCards()) {
            if (Boolean.TRUE.equals(dc.getChecked()) && !Boolean.TRUE.equals(dc.getProxy())) {
                var collection = userCardCollectionRepository
                        .findByUserAndCard(deck.getUser(), dc.getCard())
                        .orElseGet(() -> {
                            var e = new UserCardCollectionEntity();
                            e.setUser(deck.getUser());
                            e.setCard(dc.getCard());
                            e.setQuantity(dc.getQuantity());
                            return userCardCollectionRepository.save(e);
                        });
                if (collection.getQuantity() < dc.getQuantity()) {
                    collection.setQuantity(dc.getQuantity());
                    userCardCollectionRepository.save(collection);
                }
                var assignment = userCardDeckAssignmentRepository
                        .findByDeckIdAndUserCollectionId(deck.getId(), collection.getId())
                        .stream()
                        .findFirst()
                        .orElseGet(() -> {
                            var a = new UserCardDeckAssignmentEntity();
                            a.setDeckId(deck.getId());
                            a.setUserCollectionId(collection.getId());
                            a.setAssignedQuantity(0);
                            return a;
                        });
                assignment.setAssignedQuantity(dc.getQuantity());
                userCardDeckAssignmentRepository.save(assignment);
            }
        }
    }
}
