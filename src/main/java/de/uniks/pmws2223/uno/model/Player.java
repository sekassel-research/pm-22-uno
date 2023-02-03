package de.uniks.pmws2223.uno.model;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Collections;
import java.util.Collection;
import java.beans.PropertyChangeSupport;

public class Player
{
   public static final String PROPERTY_NAME = "name";
   public static final String PROPERTY_CARDS = "cards";
   public static final String PROPERTY_PREVIOUS_PLAYER = "previousPlayer";
   public static final String PROPERTY_NEXT_PLAYER = "nextPlayer";
   private String name;
   private List<Card> cards;
   private Player previousPlayer;
   private Player nextPlayer;
   protected PropertyChangeSupport listeners;

   public String getName()
   {
      return this.name;
   }

   public Player setName(String value)
   {
      if (Objects.equals(value, this.name))
      {
         return this;
      }

      final String oldValue = this.name;
      this.name = value;
      this.firePropertyChange(PROPERTY_NAME, oldValue, value);
      return this;
   }

   public List<Card> getCards()
   {
      return this.cards != null ? Collections.unmodifiableList(this.cards) : Collections.emptyList();
   }

   public Player withCards(Card value)
   {
      if (this.cards == null)
      {
         this.cards = new ArrayList<>();
      }
      if (!this.cards.contains(value))
      {
         this.cards.add(value);
         value.setOwner(this);
         this.firePropertyChange(PROPERTY_CARDS, null, value);
      }
      return this;
   }

   public Player withCards(Card... value)
   {
      for (final Card item : value)
      {
         this.withCards(item);
      }
      return this;
   }

   public Player withCards(Collection<? extends Card> value)
   {
      for (final Card item : value)
      {
         this.withCards(item);
      }
      return this;
   }

   public Player withoutCards(Card value)
   {
      if (this.cards != null && this.cards.remove(value))
      {
         value.setOwner(null);
         this.firePropertyChange(PROPERTY_CARDS, value, null);
      }
      return this;
   }

   public Player withoutCards(Card... value)
   {
      for (final Card item : value)
      {
         this.withoutCards(item);
      }
      return this;
   }

   public Player withoutCards(Collection<? extends Card> value)
   {
      for (final Card item : value)
      {
         this.withoutCards(item);
      }
      return this;
   }

   public Player getPreviousPlayer()
   {
      return this.previousPlayer;
   }

   public Player setPreviousPlayer(Player value)
   {
      if (this.previousPlayer == value)
      {
         return this;
      }

      final Player oldValue = this.previousPlayer;
      if (this.previousPlayer != null)
      {
         this.previousPlayer = null;
         oldValue.setNextPlayer(null);
      }
      this.previousPlayer = value;
      if (value != null)
      {
         value.setNextPlayer(this);
      }
      this.firePropertyChange(PROPERTY_PREVIOUS_PLAYER, oldValue, value);
      return this;
   }

   public Player getNextPlayer()
   {
      return this.nextPlayer;
   }

   public Player setNextPlayer(Player value)
   {
      if (this.nextPlayer == value)
      {
         return this;
      }

      final Player oldValue = this.nextPlayer;
      if (this.nextPlayer != null)
      {
         this.nextPlayer = null;
         oldValue.setPreviousPlayer(null);
      }
      this.nextPlayer = value;
      if (value != null)
      {
         value.setPreviousPlayer(this);
      }
      this.firePropertyChange(PROPERTY_NEXT_PLAYER, oldValue, value);
      return this;
   }

   public boolean firePropertyChange(String propertyName, Object oldValue, Object newValue)
   {
      if (this.listeners != null)
      {
         this.listeners.firePropertyChange(propertyName, oldValue, newValue);
         return true;
      }
      return false;
   }

   public PropertyChangeSupport listeners()
   {
      if (this.listeners == null)
      {
         this.listeners = new PropertyChangeSupport(this);
      }
      return this.listeners;
   }

   @Override
   public String toString()
   {
      final StringBuilder result = new StringBuilder();
      result.append(' ').append(this.getName());
      return result.substring(1);
   }

   public void removeYou()
   {
      this.withoutCards(new ArrayList<>(this.getCards()));
      this.setPreviousPlayer(null);
      this.setNextPlayer(null);
   }
}
