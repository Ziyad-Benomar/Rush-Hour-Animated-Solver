import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.HashSet;
import java.util.LinkedList;

import javax.swing.JPanel;

public class AnimatedSolverBFS extends JPanel {
	// Colors and dimensions
	final int CELL_SIZE = 100;
	final int MARGIN = CELL_SIZE / 8;
	final Color BACKGROUND = new Color(200,200,170);
	final Color RED = new Color(220,0,0);
	final Color BLUE = new Color(0,120,170);
	
	// The puzzle
	RushHour puzzle;
	public final int PANEL_WIDTH;
	public final int PANEL_HEIGHT;
	
	// The solver
	// the method solve() contains a queue of moves, and for each move
	// in that queue we do one of the following actions
	private static final int EXECUTE = 1; // apply the move to the puzzle
	private static final int EXPLORE = 0; // but add the possible moves coming after it to the queue
	private static final int CANCEL = -1; // cancel the move in the puzzle (apply the reverse move)
	
	// for animating the moves
	private CarMove currMove = new CarMove(-1,0); // the move being animated
	private float moveFraction = 0; // indicates the advancement of the animation of currMove
	private int numFrames = 10; // number of frames appearing in one cell shift
	private int animationTimeStep; // time delay between two frames
	private int animationShift = 0; // used to have smooth animation: avoids showing unnecessary moves canceled
	

	
	public AnimatedSolverBFS(RushHour puzzle, int animationTimeStep) {
		this.puzzle = puzzle;
		PANEL_WIDTH = puzzle.parking.width * CELL_SIZE;
		PANEL_HEIGHT = puzzle.parking.height * CELL_SIZE;
		this.setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT)); 
		this.setBackground(BACKGROUND);
		this.animationTimeStep = animationTimeStep;
	}
	
	
	// Method for drawing the current configuration of the puzzle, with the currMove animated
	public void paint(Graphics g) {
		super.paint(g);
		Graphics2D g2d = (Graphics2D) g;
		
		// draw grid
		g2d.setColor(new Color(100,100,100));
		for (var x = 0; x < puzzle.parking.width + 1; x++)
			g2d.drawLine(x*CELL_SIZE, 0, x * CELL_SIZE, PANEL_HEIGHT);
		for (var y = 0; y < puzzle.parking.height + 1; y++)
			g2d.drawLine(0, y*CELL_SIZE, PANEL_WIDTH, y*CELL_SIZE);
				
		// draw cars
		g2d.setStroke(new BasicStroke(3));
		for (var car: puzzle.cars.values()) {
			if (car.id == 1)
				g2d.setColor(RED);
			else
				g2d.setColor(BLUE);
			var carWidth  = (car.isHorizontal? car.size : 1);
			var carHeight = (car.isHorizontal? 1 : car.size);
			// animating the move
			int dx = 0;
			int dy = 0;
			if (car.id == currMove.carId) {
				if (car.isHorizontal)
					dx += (int) ((moveFraction + animationShift) * CELL_SIZE);
				else
					dy += (int) ((moveFraction + animationShift) * CELL_SIZE);
			}
			// car fill
			g2d.fillRoundRect(CELL_SIZE * car.x + MARGIN + dx,
					CELL_SIZE * car.y + MARGIN + dy,
					CELL_SIZE * carWidth - 2*MARGIN,
					CELL_SIZE * carHeight - 2*MARGIN,
					2*MARGIN, 2*MARGIN);
			// car parameter
			g2d.setColor(Color.black);
			g2d.drawRoundRect(CELL_SIZE * car.x + MARGIN + dx,
					CELL_SIZE * car.y + MARGIN + dy,
					CELL_SIZE * carWidth - 2*MARGIN,
					CELL_SIZE * carHeight - 2*MARGIN,
					2*MARGIN, 2*MARGIN);
		}
	}
	
	
	
	
	
	/**
	 * (BFS - Backtracking) algorithm returning optimal solution to the puzzle:
	 * ------------------------------------------------------------------------
	 * Implements the same algorithm as SolverBFS.solve(), but contains additional
	 * functionalities for animating the backtracking search for optimal winning moves
	 * @remark it is easier to read SolverBFS.solve() in order to understand 
	 * how the solving algorithm works
	 */
	public LinkedList<CarMove> solve(){
		var winningMoves = new LinkedList<CarMove>();
		
		var queue = new LinkedList<MoveAction>();
		var moves = puzzle.getPossibleMoves();
		for (var carMove: moves)
			queue.add(new MoveAction(carMove, EXPLORE));
		
		var visited = new HashSet<Integer>();
		
		var moveAction = queue.pop();
		while (!queue.isEmpty()) {
			var nextMoveAction = queue.pop();
			var carMove = moveAction.carMove;
			var action = moveAction.action;
			
			if (action == EXPLORE) {
				puzzle.move(carMove);
				if (puzzle.won()) {
					puzzle.cancelMove(carMove);
					addMove(winningMoves, carMove);
					break;
				}
				int representation = puzzle.integerRepresentation();
				if (!visited.contains(representation)) {
					visited.add(representation);
					queue.add(new MoveAction(carMove, EXECUTE));
					var nextMoves = puzzle.getPossibleMoves(carMove.carId);
					for (var nextCarMove: nextMoves)
						queue.add(new MoveAction(nextCarMove, EXPLORE));
					queue.add(new MoveAction(carMove, CANCEL));
				}
				puzzle.cancelMove(carMove);
			} 
			else if (action == EXECUTE) {
				addMove(winningMoves, carMove);
				queue.add(new MoveAction(carMove, EXECUTE));
			}
			else {
				var lastMove = winningMoves.removeLast();
				if (carMove.carId != nextMoveAction.carMove.carId)
					animateMove(lastMove, -1);
				else
					animationShift = lastMove.dxy;
				puzzle.cancelMove(lastMove);
				queue.add(new MoveAction(carMove, CANCEL));
			}
			if (carMove.carId != nextMoveAction.carMove.carId)
				animationShift = 0;
			moveAction = nextMoveAction;
		}
		// add the final winning move: pushing the red car to exit
		var finalMove = new CarMove(1, puzzle.distToExit());
		addMove(winningMoves, finalMove);
		// cancel the winningMoves to bring the puzzle back to its initial state
		cancelMoves(puzzle, winningMoves);
		return winningMoves;
	}
	
	
	
	
	// shows an animation playing the input list of moves 
	public void playMoves(LinkedList<CarMove> moves) {
		var defaultNumFrames = numFrames;
		var defaultAnimationTimeStep = animationTimeStep;
		animationShift = 0;
		numFrames = 100;
		animationTimeStep = 1;
		for (var carMove : moves) {
			animateMove(carMove);
			puzzle.move(carMove);
		}
		numFrames = defaultNumFrames;
		animationShift = defaultAnimationTimeStep;
	}
	
	
	
	// cancels a list of last played moves, changes the puzzle accordingly and shows animation
	private void cancelMoves(RushHour puzzle, LinkedList<CarMove> prevMoves) {
		var reversedMoves = new LinkedList<CarMove>();
		for (var carMove: prevMoves)
			reversedMoves.addFirst(carMove);
		for (var carMove: reversedMoves) {
			animateMove(carMove, -1);
			puzzle.cancelMove(carMove);
		}
			
	}
	
	
	// Adds carMove to winningMoves, applies it to the puzzle, and shows animation
	private void addMove(LinkedList<CarMove> winningMoves, CarMove carMove) {
		winningMoves.add(carMove);
		animateMove(new CarMove(carMove.carId, carMove.dxy - animationShift));
		puzzle.move(carMove);
	}
	
	
	
	// Shows an animation of playing carMove, we can specify an optional argument
	// of direction: when (direction = -1) show animation of the opposite move
	private void animateMove(CarMove carMove) {
		animateMove(carMove, 1);
	}
	
	private void animateMove(CarMove carMove, int direction) {
		if (animationTimeStep == 0)
			return;
		currMove = carMove;
		moveFraction = 0;
		int signDxy = (carMove.dxy > 0 ? 1 : -1);
		for (int i = 1; i < numFrames * signDxy * carMove.dxy; i++) {
			moveFraction += signDxy * direction / (float) numFrames;
			wait(animationTimeStep);
			repaint();
			wait(animationTimeStep);
		}
		currMove = new CarMove(-1,0);
		moveFraction = 0;
		
	}
	
	
	
	public void wait(int milliseconds) {
		try {
		  Thread.sleep(milliseconds);
		} catch (InterruptedException e) {
		  Thread.currentThread().interrupt();
		}
	}
	
	
}
