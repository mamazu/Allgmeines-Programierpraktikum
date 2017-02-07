package breakthroughPP.player.ai;

import breakthroughPP.preset.Move;
import breakthroughPP.preset.PresetException;
import breakthroughPP.preset.Setting;

import java.util.ArrayList;
import java.util.Random;

public class HardAI extends AbstractAI {

    /**
     * Constructor for the HardAI if the board dimensions are known
     */

    public HardAI() {
        super();
    }

    @Override
    public Move getMove() throws PresetException {
        return getNextMove();
    }

    /**
     * Method to get a list of moves that got calculated ahead
     *
     * @return ArrayList with Moves that the HardAI wants to go
     */

    private ArrayList<Move> calcGoodMoves(int colorNow) {
        int blueNow = board.getBlueCount();
        int redNow = board.getRedCount();
        int blueNext, blue2Next;
        int redNext, red2Next;
        ArrayList<Move> minMax = new ArrayList<>(); // final array with the maximum value moves of the min values
        ArrayList<Integer> minForMove = new ArrayList<>();    // min value for the specific first move
        int minMaxValue = -10000;
        ArrayList<Move> secondMove;
        ArrayList<Move> firstMove = board.getPossibleMoves(board.getCurrentPlayer());
        for (int i = 0; i < firstMove.size(); i++) {
            if (!board.executeMove(firstMove.get(i))) continue;
            int min = 10000;
            blueNext = board.getBlueCount();
            redNext = board.getRedCount();
            secondMove = board.getPossibleMoves(board.getCurrentPlayer());
            for (Move aSecondMove : secondMove) {
                if (!board.executeMove(aSecondMove)) continue;
                blue2Next = board.getBlueCount();
                red2Next = board.getRedCount();
                if (colorNow == Setting.BLUE) {
                    min = Math.min(overBlue(), min);
                } else if (colorNow == Setting.RED) {
                    min = Math.min(overRed(), min);
                }
                undoMove(aSecondMove, blueNext, blue2Next, redNext, red2Next);
            }
            minForMove.add(min);
            undoMove(firstMove.get(i), blueNow, blueNext, redNow, redNext);
        }
        for (int i = 0; i < minForMove.size(); i++) {
            if (minMaxValue == minForMove.get(i)) {
                minMax.add(firstMove.get(i));
            } else if (minMaxValue < minForMove.get(i)) {
                minMaxValue = minForMove.get(i);
                minMax.clear();
                minMax.add(firstMove.get(i));
            }
        }
        return minMax;
    }

    /**
     * Method to get a winning move or a random next move out of the calculated good moves and when possible
     * not a move that possibly make the enemy win next move
     *
     * @return move that the hard AI wants to go
     */

    public Move getNextMove() {
        int colorNow = board.getCurrentPlayer();
        Random rand = new Random();
        int blueNow = board.getBlueCount();
        int redNow = board.getRedCount();
        int blueNext;
        int redNext;
        Move moveNow = getWinningMove(colorNow);
		if (moveNow != null) {
            return moveNow;
        }
		moveNow = getFreeMove(colorNow);
		if( moveNow != null){
			return moveNow;
		}
        ArrayList<Move> now = calcGoodMoves(colorNow);
        ArrayList<Move> betterMoves = new ArrayList<>();

        for (Move aMove : now) {
            if (!board.executeMove(aMove)) continue;
            Move moveNext = getWinningMove(board.getCurrentPlayer());
            if (moveNext == null) {
                betterMoves.add(aMove);
            }
            blueNext = board.getBlueCount();
            redNext = board.getRedCount();
            undoMove(aMove, blueNow, blueNext, redNow, redNext);
        }
        if (!betterMoves.isEmpty()) {
            int n = rand.nextInt(betterMoves.size());
            return betterMoves.get(n);
        }
        int n = rand.nextInt(now.size());
        moveNow = now.get(n);
        return moveNow;
    }

}
