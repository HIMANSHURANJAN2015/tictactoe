
import models.Game;
import service.BoardService;
import service.GameService;
import service.PlayerService;
import controller.GameController;

public class Main {
    public static void main(String[] args) {
        System.out.println("WELCOME TO TICTACTOE GAME");

        GameController gameController = new GameController(new PlayerService(),
                new BoardService(), new GameService());

        Game game = gameController.createGame();
        gameController.startGame(game);
    }
}
// TODO -> remove validations from service layer
// TODO -> implement undo and replay methods