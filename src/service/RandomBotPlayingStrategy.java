package service;

import models.*;

import java.util.List;

public class RandomBotPlayingStrategy implements BotPlayingStrategy {

    public Cell findNextMove(Game game, Player player) {
        Board board = game.getBoard();
        for(List<Cell> cells : board.getCells()){
            for(Cell cell : cells){
                if(cell.getCellState().equals(CellState.EMPTY)){
                    return cell;
                }
            }
        }
        return null;
    }
}