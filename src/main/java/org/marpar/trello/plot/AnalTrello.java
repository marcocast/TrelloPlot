package org.marpar.trello.plot;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.trello4j.Trello;
import org.trello4j.TrelloImpl;
import org.trello4j.model.Action;
import org.trello4j.model.Card;
import org.trello4j.model.Card.Label;


public class AnalTrello {

    public static final String API_KEY = Optional.ofNullable(System.getenv("API_KEY")).orElse(System.getProperty("API_KEY"));

    public static final String TOKEN = Optional.ofNullable(System.getenv("TOKEN")).orElse(System.getProperty("TOKEN"));

    private static final String DONE_COLUMN_ID = "5812fee10c9e21b5c3ce2260";

    private static final String QA_COLUMN_ID = "5812fedf7beed67d280c3fc8";

    private static final String TODO_COLUMN_ID = "5812fecc64325cf97b6886f0";

    private static final String CLARIFY_REQUIREMENTS_COLUMN_ID = "5812ff249f263e5628cebbef";

    public static void main(String[] args) {
        new AnalTrello().timeSpentPerCard();
    }

    public void timeSpentPerCard() {
        Trello trello = new TrelloImpl(API_KEY, TOKEN);

        Map<MyCard, List<Action>> actionsPerCard = actionsPerEachCardInTheList(trello, DONE_COLUMN_ID);
        Map<MyCard, List<Action>> actionsPerCardWithTODO = actionsPerEachCardInTheListIncludingTODO(trello, DONE_COLUMN_ID);

        System.out.println("Task;;Priority level;;Number of days from to creation to clarification;;Number of days from clarification to be validated by LNG;;Number of days from clarification to completed;;Notes;;Link");

        actionsPerCard.keySet()
                      .stream()
                      .filter(key -> actionsPerCard.get(key).size() > 1)
                      .forEach(key -> System.out.println(key.getCard().getName() + ";;" + joinLabels(key.getCard().getLabels()) + ";;" + sla(actionsPerCardWithTODO, key) + ";;" +
                                                         toQA(actionsPerCard, key) + ";;" + toDone(actionsPerCard, key) + ";;;;" + getHyperlink(key)

        ));

    }

    private String getHyperlink(MyCard key) {
        return key.getCard().getUrl();
    }

    private String sla(Map<MyCard, List<Action>> actionsPerCard, MyCard key) {
        try {
            return "" + AnalTrelloSLA.sla(actionsPerCard, key);
        } catch (Exception e) {
            return " - ";
        }
    }

    //=HYPERLINK("https://trello.c/c/oX1EyCvw/149-exception-occurred-in-reifyassynchronous-while-sending-request")

    private String toQA(Map<MyCard, List<Action>> actionsPerCard, MyCard key) {
        try {
            return "" + WorkingDays.getWorkingDaysBetweenTwoDates(actionsPerCard.get(key).get(actionsPerCard.get(key).size() - 1).getDate(),

                                                                  findFirstInQA(actionsPerCard, key).get().getDate());

        } catch (Exception e) {
            return " - ";
        }
    }

    private Optional<Action> findFirstAfterTodo(Map<Card, List<Action>> actionsPerCard, Card key) {
        return actionsPerCard.get(key)
                             .stream()
                             .filter(action -> action.getData().getCard() != null)
                             .filter(action -> action.getData().getCard().getIdList() != null)
                             .filter(action -> action.getData().getCard().getIdList().equals(CLARIFY_REQUIREMENTS_COLUMN_ID))
                             .findFirst();
    }

    private Action findFirst(Map<Card, List<Action>> actionsPerCard, Card key) {
        return actionsPerCard.get(key).get(actionsPerCard.get(key).size() - 1);
    }

    private Optional<Action> findFirstInQA(Map<MyCard, List<Action>> actionsPerCard, MyCard key) {
        return actionsPerCard.get(key)
                             .stream()
                             .filter(action -> action.getData().getCard() != null)
                             .filter(action -> action.getData().getCard().getIdList() != null)
                             .filter(action -> action.getData().getCard().getIdList().equals(QA_COLUMN_ID))
                             .findFirst();
    }

    private int toDone(Map<MyCard, List<Action>> actionsPerCard, MyCard key) {
        return WorkingDays.getWorkingDaysBetweenTwoDates(actionsPerCard.get(key).get(actionsPerCard.get(key).size() - 1).getDate(), actionsPerCard.get(key).get(0).getDate());
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

    private Map<MyCard, List<Action>> actionsPerEachCardInTheList(Trello trello, String listId) {
        return trello.getCardsByList(listId)
                     .parallelStream()
                     .collect(Collectors.toMap(card -> new MyCard(card), card -> removeFirstListActions(trello.getActionsByCard(card.getId()))));
    }

    private Map<MyCard, List<Action>> actionsPerEachCardInTheListIncludingTODO(Trello trello, String listId) {
        return trello.getCardsByList(listId).parallelStream().collect(Collectors.toMap(card -> new MyCard(card), card -> trello.getActionsByCard(card.getId())));
    }

}
