package service;

import models.Cell;
import models.Game;
import models.Player;

public interface BotPlayingStrategy {
    Cell findNextMove(Game game, Player player);
}
