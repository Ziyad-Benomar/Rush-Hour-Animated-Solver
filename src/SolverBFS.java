import java.util.HashSet;
import java.util.LinkedList;

public class SolverBFS {
	private static final int EXECUTE = 1;
	private static final int EXPLORE = 0;
	private static final int CANCEL = -1;
	
	
	/**
	 * (BFS - Backtracking) algorithm returning optimal solution to the puzzle:
	 * ------------------------------------------------------------------------
	 * - we construct and explore a tree of all the possible configurations of the puzzle,
	 * - The moves with actions EXECUTE or CANCEL in the queue are used to move in the tree:
	 *   go from a parent node (configuration) to one its child by executing a move, or in the 
	 *   opposite way canceling the same move.
	 * - The moves with the action EXPLORE are executed, added to the queue with action EXECUTE,
	 *   then all the possible next moves are added to the queue with action EXPLORE in order to 
	 *   be treated later, and finally the move is added with action CANCEL, indicating that after 
	 *   exploring its children we must go back to the previous configuration (backtracking).
	 * @return A list of the winning moves is constructed during this process and returned by the end
	 * @remark We store the integer representations of the visited configuration in "visited"
	 */
	public static LinkedList<CarMove> solve(RushHour puzzle){
		// at each step of the algorithm, this is the list of 
		// moves played to get to the current configuration
		var winningMoves = new LinkedList<CarMove>();
		// initialize queue with possible moves in the initial configuration
		var queue = new LinkedList<MoveAction>();
		var moves = puzzle.getPossibleMoves();
		for (var carMove: moves)
			queue.add(new MoveAction(carMove, EXPLORE));
		// list of visited configurations: we store their intRepresentation (see class RushHour)
		var visited = new HashSet<Integer>();
		// the algorithm's iterations
		while (!queue.isEmpty()) {
			// Get the next move-action
			var moveAction = queue.pop();
			var carMove = moveAction.carMove;
			var action = moveAction.action;
			
			if (action == EXPLORE) {
				// play the move and explore the resulting configuration
				puzzle.move(carMove);
				if (puzzle.won()) {
					winningMoves.add(carMove);
					break;
				}
				int representation = puzzle.integerRepresentation();
				// if this configuration is visited before, stop here
				if (!visited.contains(representation)) {
					visited.add(representation);
					queue.add(new MoveAction(carMove, EXECUTE)); // to reach this configuration
					var nextMoves = puzzle.getPossibleMoves(carMove.carId); // do not move the same car again
					for (var nextCarMove: nextMoves) // add children to the queue (next tree depth)
						queue.add(new MoveAction(nextCarMove, EXPLORE));
					queue.add(new MoveAction(carMove, CANCEL)); // to go back to parent configuration
				}
				// cancel the move, go back to parent
				puzzle.cancelMove(carMove);
			} 
			else if (action == EXECUTE) { // play the move: go from parent to child
				puzzle.move(carMove);
				queue.add(new MoveAction(carMove, EXECUTE));
				winningMoves.add(carMove); // track the played moves
			}
			else { // cancel the move: go back from child to parent
				puzzle.cancelMove(carMove);
				queue.add(new MoveAction(carMove, CANCEL));
				winningMoves.removeLast(); // track the played moves
			}
		}
		// get the final move pushing the red car to the exit
		var finalMove = new CarMove(1, puzzle.distToExit());
		cancelMoves(puzzle, winningMoves);
		// add the final winning move
		winningMoves.add(finalMove);
		return winningMoves;
	}
	
	
	
	// cancels a list of last played moves, changes the puzzle accordingly and shows animation
	private static void cancelMoves(RushHour puzzle, LinkedList<CarMove> prevMoves) {
		var reversedMoves = new LinkedList<CarMove>();
		for (var carMove: prevMoves)
			reversedMoves.addFirst(carMove);
		for (var carMove: reversedMoves)
			puzzle.cancelMove(carMove);
	}
}




//used in the queue in method solve()
class MoveAction { 
	CarMove carMove;
	int action; // = EXECUTE, EXPLORE or CANCEL
	
	MoveAction(CarMove carMove, int action){
		this.carMove = carMove;
		this.action = action;
	}
}

