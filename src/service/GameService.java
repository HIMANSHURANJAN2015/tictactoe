package service;

import exception.InvalidCellChosenException;
import models.*;

import java.util.Collections;
import java.util.List;

public class GameService {

    //Since I need BoardService at only 1 place, so its better not to save it as instance variable
    //and pass it as parameter to the only method that needs it.
    //private BoardService boardService;

    public Game createGame(int size, List<Player> players){
        //For the Game we need CheckWinnerUtil. So creating its object
        CheckWinnerUtil checkWinnerUtil = new CheckWinnerUtil(size);

        Game newGame = Game.builder()
                .size(size)
                .players(players)
                .checkWinnerUtil(checkWinnerUtil)
                .build(); // builds game objects -> builds the board object -> builds the matrix of cells -> builds the cell object
        return newGame;
    }

    public Game startGame(Game game){
        game.setGameState(GameState.IN_PROGRESS);
        List<Player> players = game.getPlayers();
        Collections.shuffle(players);
        return game;
    }

    public Move makeMove(Game game, Player player, int row, int col) {
        Cell cell = game.getBoard().getCells().get(row).get(col);
        if(cell.getCellState() != CellState.EMPTY){
            throw new InvalidCellChosenException("Cell is already full");
        }
        cell.setCellState(CellState.FILLED);
        cell.setPlayer(player);
        Move move = new Move(cell, player);
        game.getMoves().add(move);
        game.getPlayedBoards().add(game.getBoard().clone());
        game.setNextMovePlayerIndex((game.getNextMovePlayerIndex() + 1) % (game.getPlayers().size()));
        return move;
    }

    public Move makeMoveBot(BotPlayingStrategy strategy, Game game, Player player) {
        Cell cell = strategy.findNextMove(game, player);
        if(cell == null) {
            throw new InvalidCellChosenException("Cell is empty");
        }
        return this.makeMove(game, player, cell.getRow(), cell.getCol());
    }

    public Game endGameWithDraw(Game game){
        game.setGameState(GameState.DRAW);
        return game;
    }

    public Game endGameWithWinner(Game game, Player winner){
        game.setGameState(GameState.WINNER_DONE);
        game.setWinner(winner);
        return game;
    }

    public Game undoMove(Game game, int undoCount) {
        List<Board> playedBoards = game.getPlayedBoards();
        List<Move> moveList = game.getMoves();
        int movesPlayed = moveList.size();

        moveList = moveList.subList(0, movesPlayed - undoCount);
        playedBoards = playedBoards.subList(0, movesPlayed - undoCount);
        //game.setBoard(playedBoards.getLast());getLast() added in java 21 so wont work in java 17
        game.setBoard(playedBoards.get(playedBoards.size() - 1));
        return game;
    }

    public void replay(BoardService boardService, Game game){
        List<Board> playedBoards = game.getPlayedBoards();
        for(Board board : playedBoards){
            boardService.printBoard(board);
            System.out.println("-------------------------");
        }
    }
}
// Steps after playing a move
// Update - Board, Cell
// CheckWinner + checkDraw
// next player
// store move
// store board
