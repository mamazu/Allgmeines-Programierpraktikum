package breakthroughPP.player.ai;

import breakthroughPP.preset.Move;
import breakthroughPP.preset.PresetException;
import breakthroughPP.preset.Setting;

import java.util.ArrayList;
import java.util.Random;

public class EasyAI extends AbstractAI {

	/**
	*	Constructor for the EasyAI if the board dimensions are known
	*
	*/
	
	public EasyAI(){
		super();
	}
	
	@Override
	public Move getMove() throws PresetException {
		return getNextMove();
	}
	
	/**
	* Method to get a winning move , a move where a enemy stone will get eliminated or a random move
	*
	* @return move that the easy AI wants to go
	*/
	
	public Move getNextMove(){
		int colorNow = board.getCurrentPlayer();
		Random rand = new Random();
		Move moveNow = getWinningMove(colorNow);
		if(moveNow != null){
			return moveNow;
		}
		moveNow = getCaptureMove(colorNow);
		if(moveNow != null){
			return moveNow;
		}
		ArrayList<Move> now = board.getPossibleMoves(colorNow);
		int n = rand.nextInt(now.size());
		moveNow = now.get(n);
		return moveNow;
	}
	
}


