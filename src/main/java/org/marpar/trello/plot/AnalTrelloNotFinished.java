package org.marpar.trello.plot;

import java.text.DateFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.trello4j.Trello;
import org.trello4j.TrelloImpl;
import org.trello4j.model.Action;
import org.trello4j.model.Card.Label;


public class AnalTrelloNotFinished {

    public static final String API_KEY = Optional.ofNullable(System.getenv("API_KEY")).orElse(System.getProperty("API_KEY"));

    public static final String TOKEN = Optional.ofNullable(System.getenv("TOKEN")).orElse(System.getProperty("TOKEN"));

    private static final String DONE_COLUMN_ID = "5812fee10c9e21b5c3ce2260";

    private static final String QA_COLUMN_ID = "5812fedf7beed67d280c3fc8";

    private static final String TODO_COLUMN_ID = "5812fecc64325cf97b6886f0";

    private static final String CLARIFY_REQUIREMENTS_COLUMN_ID = "5812ff249f263e5628cebbef";

    public static void main(String[] args) {
        new AnalTrelloNotFinished().timeSpentPerCard();
    }

    public void timeSpentPerCard() {
        Trello trello = new TrelloImpl(API_KEY, TOKEN);

        Map<MyCard, List<Action>> allCardsActions = actionsPerEachCardInTheListIncludingTODO(trello, DONE_COLUMN_ID);

        System.out.println("Task;;Priority level;;Date of creation;;Number of days from to creation to clarification;;Number of days from clarification to be validated by LNG;;Number of days from clarification to completed;;Notes;;Link");

        allCardsActions.keySet()
                       .stream()
                       .filter(key -> allCardsActions.get(key).size() > 1)
                       .forEach(key -> System.out.println(key.getCard().getName() + ";;" + joinLabels(key.getCard().getLabels()) + ";;" + getStartDate(allCardsActions, key) +
                                                          ";;" + sla(allCardsActions, key) + ";;" + ";;;;" + getHyperlink(key)

        ));

    }

    /**
     * @param actionsPerCardWithTODO
     * @param key
     * @return
     */
    private String getStartDate(Map<MyCard, List<Action>> actionsPerCardWithTODO, MyCard key) {
        try {
            DateFormat df = DateFormat.getDateInstance(DateFormat.DEFAULT);

            return "" + df.format(actionsPerCardWithTODO.get(key).get(actionsPerCardWithTODO.get(key).size() - 1).getDate());
        } catch (Exception e) {
            return " - ";
        }
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
    private List<Action> removeListActions(List<Action> actionsPerCard) {
        return actionsPerCard.stream()
                             .filter(action -> action.getData().getCard() != null)
                             .filter(action -> action.getData().getCard().getIdList() != null)
                             .filter(action -> !action.getData().getCard().getIdList().equals(DONE_COLUMN_ID))
                             .collect(Collectors.toList());
    }

    private Map<MyCard, List<Action>> actionsPerEachCardInTheListIncludingTODO(Trello trello, String listId) {
        return trello.getCardsByBoard("5812fea76a3f3c10df9d49b0")
                     .parallelStream()
                     .filter(card -> !card.getIdList().equals(DONE_COLUMN_ID))
                     .collect(Collectors.toMap(card -> new MyCard(card), card -> trello.getActionsByCard(card.getId())));
    }

}
