import java.util.ArrayList;
import java.util.HashMap;

public class RushHour {
	Parking parking;
	private int maxPosition;
	
	private boolean redCarCreated = false;
	private int numCars = 1;
	HashMap<Integer, Car> cars = new HashMap<Integer, Car>();
	
	
	public RushHour(int width, int height) {
		parking = new Parking(width, height);
		maxPosition = Math.max(width, height);
	}
	
	
	// - It is essential that the car ids are exactly 1,...,numCars
	// - This function is an injection from the set of all possible configurations
	//   using the same cars to set of positive integers.
	// - It is used in the solver to keep track of the visited configurations,
	//   it has the advantage of giving a light representation (int) and thus
	//   consume few memory resources
	int integerRepresentation() {
		int representation = 0;
		int power = 1;
		for (int carId = 1; carId < numCars + 1; carId ++) {
			representation += power * cars.get(carId).getVariableCoordinate();
			power *= maxPosition;
		}
		return representation;
	}
	
	
	// Give the id the next car to create
	// We can only have one red car, it has id = 1. 
	private int nextCarId(boolean isRed) {
		int carId = 0;
		if (isRed) {
			if (redCarCreated)
				throw new Error("Red car has already been created, you cannot add a second one");
			carId = 1;
			redCarCreated = true;
		} else
			carId = ++numCars;
		return carId;
	}
	
	/**
	 * 
	 * @param x, y coordinates of the first cell of the car (left - up)
	 * @param size: number of cells occupied by the car
	 * @param orientation: HORIZONTAL or VERTICAL
	 * @param isRed: is this the red car ?
	 * @return true if the car have been added, false otherwise (if the specified 
	 *       place is not available on the parking)
	 */
	public boolean addCar(int x, int y, int size, int orientation, boolean isRed) {
		// check that position is free
		if (!parking.isFree(x, y, size, orientation)) {
			System.out.println("the chosen parking place ("
				+ x + "," + y + "," + size + "," 
				+ (orientation==Car.HORIZONTAL? "HORIZONTAL": "VERTICAL") +
				") is not available or out of bounds");
			return false;
		}
		// Create car object
		int carId = nextCarId(isRed);
		Car car = new Car(carId, x, y, size, orientation);
		// put it on the parking
		parking.putCar(car);
		// add to the cars' hashmap
		cars.put(carId, car);
		return true;
	}
	
	// if isRed is not specified, then the car is blue by default
	public boolean addCar(int x, int y, int size, int orientation) {
		return addCar(x, y, size, orientation, false);
	}
	
	
	// return all the possible moves of a car
	public ArrayList<CarMove> getCarPossibleMoves(int carId) {
		var car = cars.get(carId);
		var carMoves = new ArrayList<CarMove>();
		if (car.isHorizontal) {
			int dx = -1;
			while (parking.isFree(car.x + dx, car.y))
				carMoves.add(new CarMove(car.id, dx--));
			dx = 1;
			while (parking.isFree(car.x + car.size - 1 + dx, car.y))
				carMoves.add(new CarMove(car.id, dx++));
		}
		else {
			int dy = -1;
			while (parking.isFree(car.x, car.y + dy))
				carMoves.add(new CarMove(car.id, dy--));
			dy = 1;
			while (parking.isFree(car.x, car.y + car.size - 1 + dy))
				carMoves.add(new CarMove(car.id, dy++));
		}
		return carMoves;
	}
	
	
	// return a list of all the possible moves of all the cars
	public ArrayList<CarMove> getPossibleMoves(){
		return getPossibleMoves(-1);
	}
	
	// return a list of all the possible moves of all the cars except 
	// the car with (id == ignoreCarId)
	public ArrayList<CarMove> getPossibleMoves(int ignoreCarId) {
		var allMoves = new ArrayList<CarMove>();
		for (var carId: cars.keySet()) {
			if (carId != ignoreCarId) {
				var carMoves = getCarPossibleMoves(carId);
				allMoves.addAll(carMoves);
			}
		}
		return allMoves;		
	}
	
	
	// "move" must be an available move, otherwise an error is thrown in parking.putCar(car).
	public void move(CarMove carMove) {
		var car = cars.get(carMove.carId);
		// remove car from parking
		parking.removeCar(car);
		// modify car coordinates
		if (car.isHorizontal)
			car.x += carMove.dxy;
		else
			car.y += carMove.dxy;
		// put it back in the parking
		parking.putCar(car);
	}
	
	// do the opposite move of carMove
	public void cancelMove(CarMove carMove) {
		move(new CarMove(carMove.carId, - carMove.dxy));
	}
	
	
	// returns the distance between the redCr and the exit cell
	public int distToExit() {
		Car redCar = cars.get(1);
		return parking.width - (redCar.x + redCar.size);
	}
	
	// returns true if the game can be won in one move
	public boolean won() {
		Car redCar = cars.get(1);
		return parking.isFree(redCar.x + redCar.size, redCar.y, distToExit(), Car.HORIZONTAL);
	}
	
	
	// for testing
	public void showParking() {
		parking.showBoard();
	}
}




class CarMove{
	int carId;
	int dxy;
	
	CarMove(int carId, int dxy){
		this.carId = carId;
		this.dxy = dxy;
	}
}