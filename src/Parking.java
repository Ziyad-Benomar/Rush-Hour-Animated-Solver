
public class Parking {
	
	final int width;
	final int height;
	private int[][] board;
			
	Parking(int width, int height){
		this.width = width;
		this.height = height;
		this.board = new int[width][height];
	}
	
	
	/**
	 * puts the car on the parking: the corresponding board cells
	 * will be set to the value car.id
	 * @param car: the cells it occupies are already defined by its attributes 
	 * x,y,size and isHorizontal. Must verify that they are free before
	 * calling this function, otherwise an error is thrown
	 */
	boolean putCar(Car car) {
		var carCells = car.getOccupiedCells();
		for (var cell : carCells) {
			if (!isFree(cell.x, cell.y))
				throw new Error("the destination parking place is not free");
			board[cell.x][cell.y] = car.id;
		}
		return true;
	}
	
	// Removes the car from board: set all it cells to 0
	void removeCar(Car car) {
		var carCells = car.getOccupiedCells();
		for (var cell: carCells)
			board[cell.x][cell.y] = 0;
	}
	
	/**
	 * @return: -1 if (x,y) is not an available place (out of bounds),
	 * 0 if it is free,
	 * carId if the car with id=carId is on (x,y)
	 */
	int getCellOccupier(int x, int y) {
		if (x < 0 || x >= width || y < 0 || y >= height)
			return -1;
		return board[x][y];
			
	}
	

	// returns true if (x,y) is free and not out of bounds, false otherwise
	boolean isFree(int x, int y) {
		return (getCellOccupier(x, y) == 0);
	}
	
	// checks if all the cells moving from (x,y) in "orientation" 
	// by "size" steps are free and not out of bounds
	boolean isFree(int x, int y, int size, int orientation) {
		if (orientation == Car.HORIZONTAL) {
			for (int dx = 0; dx < size; dx++)
				if (!isFree(x + dx, y))
					return false;
		} else {
			for (int dy = 0; dy < size; dy++)
				if (!isFree(x, y + dy))
					return false;
		}
		return true;
	}
	
	// testing
	void showBoard() {
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++)
				System.out.print(board[x][y] + ",");
			System.out.println("");
		}
		System.out.println("");
	}
	
	
	
}






class Cell {
	int x,y;
	Cell(int x, int y){
		this.x = x;
		this.y = y;
	}
	
}