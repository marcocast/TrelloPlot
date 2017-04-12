package org.marpar.trello.plot;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.trello4j.Trello;
import org.trello4j.TrelloImpl;
import org.trello4j.model.Action;
import org.trello4j.model.Card;


public class AnalTrello {

    private static final String TOKEN = "";

    private static final String API_KEY = "";

    private static final String COLUMN_ID = "";

    public static void main(String[] args) {
        new AnalTrello().timeSpentPerCard();
    }

    public void timeSpentPerCard() {
        Trello trello = new TrelloImpl(API_KEY, TOKEN);

        Map<Card, List<Action>> actionsPerCard = actionsPerEachCardInTheList(trello, COLUMN_ID);

        System.out.println("Total cards retrieved : " + actionsPerCard.size());

        actionsPerCard.keySet().stream()
                .filter(key -> actionsPerCard.get(key)
                        .size() > 1)
                .forEach(key -> System.out.println(key.getName() + " took :" +
                                            TimeFormatter.formatTime(
                                                actionsPerCard.get(key).get(0).getDate().getTime() -
                                                actionsPerCard.get(key).get(actionsPerCard.get(key).size() - 1).getDate().getTime())));

    }

    private Map<Card, List<Action>> actionsPerEachCardInTheList(Trello trello, String listId) {
        return trello.getCardsByList(listId).parallelStream()
                .collect(Collectors.toMap(card -> card, card -> trello.getActionsByCard(card.getId())));
    }

}
