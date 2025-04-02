package controller;

import exception.GameDrawnException;
import exception.InvalidCellChosenException;
import exception.InvalidUndoCommandException;
import models.*;
import service.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class GameController {

    //since we want GameController(and also playerService, boardservice and gameservice) to be singleton, so not storing Game as instance variabke
    //rather passing it as parameter
    private PlayerService playerService;
    private BoardService boardService;
    private GameService gameService;
    private Scanner sc;
    private final BotPlayingStrategy botPlayingStrategy = new RandomBotPlayingStrategy();

    public GameController(PlayerService playerService, BoardService boardService, GameService gameService) {
        this.playerService = playerService;
        this.boardService = boardService;
        this.gameService = gameService;
        this.sc = new Scanner(System.in);
    }

    //step-1:create game
    public Game createGame() {
        System.out.println("Please enter the dimension of the game");
        //int size = sc.nextInt();
        /*
            With scanner, nextInt() reads only the integer and ignores remaining characters("\n" in this case). So below
            when I ask user to enter the name, it is automatically taking \n as the name. And then asking me to enter
            symbol as per below code. To fix this there are 3 ways:
            1. Whenever we use nextInt(), we should immediately use nextLine() after that to consume any leftover char.
                Same applies to nextFloat(), nextDouble() etc
            2. Always use nextLine() and parse to other type when needed. e.g.
                int size = Integer.parseInt(sc.nextLine());
                int price = Integer.parseDouble(sc.nextLine());
            3. Use Buffered reader. This doesn't have leftover character issue.
                BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                System.out.println("Enter an integer:");
                int size = Integer.parseInt(br.readLine()); // Reads the integer
                System.out.println("Enter your name:");
                String name = br.readLine(); // Reads the name

             I use method-2
         */
        //
        int size = Integer.parseInt(sc.nextLine());
        List<Player> players = this.createPlayerList(size);
        return gameService.createGame(size, players);
    }

    //Step-1 needs to create players(ideally in Player Controller. but for simplicity in Game Controller
    public List<Player> createPlayerList(int size){
        System.out.println("Please enter 1 if you want a bot, else enter 0");
        int botResult = 1;
        Bot bot;
        List<Player> players = new ArrayList<>();

        if(botResult == 1){
            bot = playerService.createBot("BOT", '$');
            size--;
            players.add(bot);
        }

        for (int i = 0; i < size - 1; i++) {
            System.out.println("Please enter the name for player : " + (i + 1));
            String name = sc.nextLine();
            System.out.println("Please enter the character for the player : " + name);
            char symbol = sc.nextLine().charAt(0);
            players.add(playerService.createPlayer(name, symbol));
        }
        return players;
    }

    //step-2 start game
    public void startGame(Game game) {
        gameService.startGame(game);

        //now starting a infinite loop to make move->check winner
        GameState gameState = this.getGameState(game);
        while(gameState.equals(GameState.IN_PROGRESS)) {
            Move move = this.makeMove(game);
            this.checkWinner(game, move, game.getCheckWinnerUtil());
            gameState = this.getGameState(game);
        }

        //if the game is drawn
        if(gameState.equals(GameState.DRAW)) {
            System.out.println("Game is draw, please start again");
        } else if(gameState.equals(GameState.WINNER_DONE)) {
            Player winner = game.getWinner();
            System.out.println("WINNER : " + winner.getName());
            boardService.printBoard(game.getBoard());
            System.out.println("Do you want a replay? 1 for Yes, 0 for No");
            int replay = Integer.parseInt(sc.nextLine());
            if(replay == 1){
                this.replay(game);
            }
        }
    }

    //Step-3: make move
    public Move makeMove(Game game){
        //Finding next player to make the move
        int nextPlayerIndex = game.getNextMovePlayerIndex();
        Player currentPlayer = game.getPlayers().get(nextPlayerIndex);
        System.out.println("Player to make a move : " + currentPlayer.getName());

        //Asking user if he/she wants undo
        if(!currentPlayer.getPlayerType().equals(PlayerType.BOT)) {
            System.out.println("Do you want to undo to a certain step ? 1 for yes, 0 for No");
            int undo = Integer.parseInt(sc.nextLine());
            if (undo == 1) {
                System.out.println("Please enter the number of moves you want to go back");
                int undoCount = Integer.parseInt(sc.nextLine());
                game = this.undoGame(undoCount, game);
            }
        }

        //Printing the board and asking Player to enter the next move
        boardService.printBoard(game.getBoard());
        Move move = null;
        if(currentPlayer.getPlayerType().equals(PlayerType.HUMAN)) {
            System.out.println("Please enter the row number");
            int row = Integer.parseInt(sc.nextLine());
            System.out.println("Please enter the col number ");
            int col = Integer.parseInt(sc.nextLine());
            // TODO : validate the row and col range within board
            try {
                move =  gameService.makeMove(game, currentPlayer, row, col);
            } catch (InvalidCellChosenException ex) {
                // TODO: handle new entry logic
            }
        } else {
            move = gameService.makeMoveBot(this.botPlayingStrategy, game, currentPlayer);
        }
        return move;
    }

    //step-4: Check winner
    public void checkWinner(Game game, Move move, CheckWinnerUtil checkWinnerUtil) {
        try {
            Player winner = checkWinnerUtil.checkWinner(move);
            if (winner != null) {
                //game is won
                gameService.endGameWithWinner(game, winner);
            }
        } catch (GameDrawnException ex){
            //game is drawn
            gameService.endGameWithDraw(game);
        }
    }

    //check game status
    public GameState getGameState(Game game) {
        return game.getGameState();
    }

    //step-5 undo move
    public Game undoGame(int moves, Game game){
        if(moves > game.getMoves().size()){
            throw new InvalidUndoCommandException("Number of undo steps is invalid");
        }
        return gameService.undoMove(game, moves);
    }

    //step-6 replay
    public void replay(Game game){
        gameService.replay(this.boardService, game);
    }
}