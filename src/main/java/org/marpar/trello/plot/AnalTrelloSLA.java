package org.marpar.trello.plot;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.trello4j.Trello;
import org.trello4j.model.Action;
import org.trello4j.model.Card;
import org.trello4j.model.Card.Label;


public class AnalTrelloSLA {

    public static final String API_KEY = Optional.ofNullable(System.getenv("API_KEY")).orElse(System.getProperty("API_KEY"));

    public static final String TOKEN = Optional.ofNullable(System.getenv("TOKEN")).orElse(System.getProperty("TOKEN"));

    private static final String DONE_COLUMN_ID = "5812fee10c9e21b5c3ce2260";

    private static final String TODO_COLUMN_ID = "5812fecc64325cf97b6886f0";

    private static final String CLARIFY_REQUIREMENTS_COLUMN_ID = "5812ff249f263e5628cebbef";

    private static final String IN_PROGRESS_COLUMN_ID = "5812fedc701ecf2b46327ff4";

    //    public static void main(String[] args) {
    //        new AnalTrelloSLA().timeSpentPerCard();
    //    }

    //    public void timeSpentPerCard() {
    //        Trello trello = new TrelloImpl(API_KEY, TOKEN);
    //
    //        Map<Card, List<Action>> actionsPerCard = actionsPerEachCardInTheList(trello, DONE_COLUMN_ID);
    //
    //        actionsPerCard.keySet()
    //                      .stream()
    //                      .filter(key -> actionsPerCard.get(key).size() > 1)
    //                      .filter(key -> findFirstAfterTodo(actionsPerCard, key).isPresent())
    //                      .forEach(key -> System.out.println(key.getName() + ";;" + joinLabels(key.getLabels()) + ";;" + sla(actionsPerCard, key) + ";;" + key.getUrl()
    //
    //        ));
    //
    //    }

    public static int sla(Map<MyCard, List<Action>> actionsPerCard, MyCard key) {
        return WorkingDays.getWorkingDaysBetweenTwoDates(findFirst(actionsPerCard, key).getDate(), findFirstAfterTodo(actionsPerCard, key).get().getDate());
    }

    private Action findLast(Map<Card, List<Action>> actionsPerCard, Card key) {
        return actionsPerCard.get(key).get(0);
    }

    private static Optional<Action> findFirstAfterTodo(Map<MyCard, List<Action>> actionsPerCard, MyCard key) {
        return actionsPerCard.get(key)
                             .stream()
                             .filter(action -> action.getData().getCard() != null)
                             .filter(action -> action.getData().getCard().getIdList() != null)
                             .filter(action -> action.getData().getCard().getIdList().equals(CLARIFY_REQUIREMENTS_COLUMN_ID))
                             .findFirst();
    }

    private static Action findFirst(Map<MyCard, List<Action>> actionsPerCard, MyCard key) {
        return actionsPerCard.get(key).get(actionsPerCard.get(key).size() - 1);
    }

    /**
     * @param labels
     * @return
     */
    private String joinLabels(List<Label> labels) {
        return labels.stream().map(i -> i.getColor() + "(" + i.getName() + ")").collect(Collectors.joining(", "));

    }

    /**
     * @param list
     * @return
     */
    private List<Action> removeFirstListActions(List<Action> actionsPerCard) {
        return actionsPerCard.stream()
                             .filter(action -> action.getData().getCard() != null)
                             .filter(action -> action.getData().getCard().getIdList() != null)
                             .filter(action -> !action.getData().getCard().getIdList().equals(TODO_COLUMN_ID))
                             .collect(Collectors.toList());
    }

    private Map<Card, List<Action>> actionsPerEachCardInTheList(Trello trello, String listId) {
        return trello.getCardsByList(listId).parallelStream().collect(Collectors.toMap(card -> card, card -> trello.getActionsByCard(card.getId())));
    }

}
