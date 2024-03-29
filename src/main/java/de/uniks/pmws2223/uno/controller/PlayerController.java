package de.uniks.pmws2223.uno.controller;

import de.uniks.pmws2223.uno.Constants.CARD_TYPE;
import de.uniks.pmws2223.uno.model.Card;
import de.uniks.pmws2223.uno.model.Player;
import de.uniks.pmws2223.uno.service.AnimationService;
import de.uniks.pmws2223.uno.service.BotService;
import de.uniks.pmws2223.uno.service.GameService;
import javafx.animation.Interpolator;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class PlayerController implements Controller {

    final private Player player;
    private PropertyChangeListener cardListener;
    private PropertyChangeListener currentPlayerListener;
    private GameService gameService;
    private BotService botService;
    private AnimationService animationService;
    private Pane wishColorParent;
    private Label nameLabel;

    public PlayerController(Player player, GameService gameService, Pane wishColorParent, BotService botService, AnimationService animationService) {
        this.player = player;
        this.gameService = gameService;
        this.wishColorParent = wishColorParent;
        this.botService = botService;
        this.animationService = animationService;
    }

    @Override
    public String getTitle() {
        return null;
    }

    @Override
    public void init() {
    }

    @Override
    public Parent render() throws IOException {
        HBox cardBox;
        Parent parent;
        if (player.isIsBot()) {
            VBox parentBox = new VBox();
            parentBox.setMinWidth(333);
            cardBox = new HBox();
            Label botName = new Label();
            nameLabel = botName;
            botName.setText(player.getName());
            botName.setFont(Font.font("Comic Sans MS", 29));
            botName.setUnderline(true);
            botName.setTranslateX(12);
            cardBox.setMaxWidth(200);
            cardBox.setPrefHeight(96);
            cardBox.setAlignment(Pos.CENTER);
            parentBox.getChildren().add(botName);
            parentBox.getChildren().add(cardBox);
            parentBox.setAlignment(Pos.CENTER);
            parent = parentBox;
        } else {
            cardBox = new HBox();
            cardBox.setPrefWidth(380);
            cardBox.setPrefHeight(160);
            cardBox.setLayoutX(317);
            cardBox.setLayoutY(452);
            cardBox.setAlignment(Pos.CENTER);
            cardBox.setScaleX(1.5);
            cardBox.setScaleY(1.5);
            parent = cardBox;
        }

        cardListener = event -> {
            if (event.getNewValue() != null) {
                Card newCard = (Card) event.getNewValue();
                Player owner = newCard.getOwner();
                Pane newUICard = gameService.generateUICard(newCard, owner.isIsBot());

                cardBox.getChildren().add(newUICard);
                flyInCards(newUICard);
            } else if (event.getOldValue() != null) {
                cardBox.getChildren().removeIf(card -> card.getUserData().equals(event.getOldValue()));
            }
        };

        currentPlayerListener = event -> {
            if (player.isIsBot()) {
                if (event.getNewValue() == null) {
                    nameLabel.setTextFill(Color.BLACK);
                } else {
                    nameLabel.setTextFill(Color.RED);
                    Timer timer = new Timer();
                    TimerTask task = new TimerTask() {

                        @Override
                        public void run() {
                            Platform.runLater(() -> {
                                player.setWishedColor(botService.wishBotColor(player));
                                if (player.getDebtCount() <= 0) {
                                    Card cardToPlay = botService.checkCardsForPlayable(player, null);
                                    if (cardToPlay != null) gameService.playCard(cardToPlay);
                                    else gameService.drawCard(true);
                                } else {
                                    Card cardToPlay = botService.checkCardsForPlayable(player, player.getDebtCount() == 2 ? CARD_TYPE.DRAW_TWO.toString() : CARD_TYPE.WILD_DRAW_FOUR.toString());
                                    if (cardToPlay != null) {
                                        gameService.playCard(cardToPlay);
                                    } else {
                                        for (int i = 0; i < player.getDebtCount(); i++) {
                                            gameService.drawCard(false);
                                        }
                                        gameService.passTurn();
                                    }
                                    player.setDebtCount(0);
                                }
                            });
                        }

                    };
                    timer.schedule(task, 2000);
                }
            } else {
                if (event.getNewValue() != null) {
                    if (player.getDebtCount() > 0) {
                        Card cardToPlay = botService.checkCardsForPlayable(player, player.getDebtCount() == 2 ? CARD_TYPE.DRAW_TWO.toString() : CARD_TYPE.WILD_DRAW_FOUR.toString());
                        if (cardToPlay == null) {
                            for (int i = 0; i < player.getDebtCount(); i++) {
                                gameService.drawCard(false);
                            }
                            player.setDebtCount(0);
                            gameService.passTurn();
                        }
                    }
                }
            }
        };

        player.listeners().addPropertyChangeListener(Player.PROPERTY_CURRENT_GAME, currentPlayerListener);
        player.listeners().addPropertyChangeListener(Player.PROPERTY_CARDS, cardListener);

        return parent;
    }

    private void flyInCards(Pane newUICard) {
        for (Node child : newUICard.getChildren()) {
            animationService.moveNode(child, 200, 0, 1, Interpolator.EASE_BOTH).setOnFinished(e -> {
                animationService.fadeTranslate(child, 0, 0, 0, 1, 300);
            });
        }
    }

    @Override
    public void destroy() {
        player.listeners().removePropertyChangeListener(Player.PROPERTY_CARDS, cardListener);
        player.listeners().removePropertyChangeListener(Player.PROPERTY_CURRENT_GAME, currentPlayerListener);
    }

}
