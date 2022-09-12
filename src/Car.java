import java.awt.Color;
import java.util.ArrayList;

public class Car {
	public static final int HORIZONTAL = 0;
	public static final int VERTICAL = 1;
	
	int id;
	int x,y;
	final int size;
	final boolean isHorizontal;
	
	public Car(int id, int x, int y, int size, int orientation){
		this.id = id;
		this.x = x;
		this.y = y;
		this.size = size;
		this.isHorizontal = (orientation == HORIZONTAL);
	}
	
	
	// returns a list of the cells (X,Y) occupied by the 
	public Cell[] getOccupiedCells() {
		Cell[] cells = new Cell[size];
		int i = 0;
		if (isHorizontal)
			for (int dx = 0; dx < size; dx++)
				cells[i++] = new Cell(x + dx, y);
		else
			for (int dy = 0; dy < size; dy++)
				cells[i++] = new Cell(x, y + dy);
		return cells;
	}
	

	// for horizontal cars, the variable coordinate is x, while y is constant
	// for vertical cars, the variable coordinate y while x is constant
	public int getVariableCoordinate() {
		return this.isHorizontal? x: y;
	}
	
}


