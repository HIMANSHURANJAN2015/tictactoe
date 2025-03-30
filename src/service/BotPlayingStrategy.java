package service;

import models.Game;
import models.Move;
import models.Player;

public interface BotPlayingStrategy {
    Move makeMove(Player player, Game game);
}
