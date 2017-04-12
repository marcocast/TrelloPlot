package org.marpar.trello.plot;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.trello4j.Trello;
import org.trello4j.TrelloImpl;
import org.trello4j.model.Action;
import org.trello4j.model.Card;


public class AnalTrello {

    public static final String API_KEY = Optional.ofNullable(System.getenv("API_KEY")).orElse(System.getProperty("API_KEY"));

    public static final String TOKEN = Optional.ofNullable(System.getenv("TOKEN")).orElse(System.getProperty("TOKEN"));

    private static final String COLUMN_ID = "5812fee10c9e21b5c3ce2260";

    public static void main(String[] args) {
        new AnalTrello().timeSpentPerCard();
    }

    public void timeSpentPerCard() {
        Trello trello = new TrelloImpl(API_KEY, TOKEN);

        Map<Card, List<Action>> actionsPerCard = actionsPerEachCardInTheList(trello, COLUMN_ID);

        System.out.println("Total cards retrieved : " + actionsPerCard.size());

        actionsPerCard.keySet()
                      .stream()
                      .filter(key -> actionsPerCard.get(key).size() > 1)
                      .forEach(key -> System.out.println(key.getName() + " took :" + WorkingDays.getWorkingDaysBetweenTwoDates(
                                                                                                                               actionsPerCard.get(key)
                                                                                                                                             .get(actionsPerCard.get(key).size() -
                                                                                                                                                  1)
                                                                                                                                             .getDate(),
                                                                                                                               actionsPerCard.get(key).get(0).getDate()) +
                                                         " Days"));

    }

    private Map<Card, List<Action>> actionsPerEachCardInTheList(Trello trello, String listId) {
        return trello.getCardsByList(listId).parallelStream().collect(Collectors.toMap(card -> card, card -> trello.getActionsByCard(card.getId())));
    }

}
