import java.util.ArrayList;
import java.util.List;

public class Building {
    protected final List<Elevator> elevators;
    public static final int TOP_FLOOR = 9;
    public static final int BOTTOM_FLOOR = 0;


    public Building(int numberOfElevators) {
        elevators = new ArrayList<>(numberOfElevators);
        for (int i = 0; i < numberOfElevators; i++) {
            Elevator elevator = new Elevator(i + 1);
            elevators.add(elevator);
            Thread elevatorThread = new Thread(elevator);
            elevatorThread.start();
        }
    }

    public void addListeners(Iterable<ElevatorListener> listeners) {
        for (Elevator elevator : elevators)
            for (ElevatorListener listener : listeners)
                elevator.addElevatorListener(listener);
    }

    public void requestElevator(int floor) {
        Elevator bestElevator = findBestElevator(floor);
        if (bestElevator == null) return;
        bestElevator.addRequest(new Elevator.Request(floor, Elevator.RequestType.IN));
    }

    public void requestElevator(int floor, Elevator.RequestType type, int elevatorId) {
        elevators.get(elevatorId - 1).addRequest(new Elevator.Request(floor, type));
    }

    private Elevator findBestElevator(int floor) {
        Elevator bestElevator = null;
        int minDistance = Integer.MAX_VALUE;

        for (Elevator elevator : elevators) {
            int distance = calculateEffectiveDistance(elevator, floor);
            if (distance < minDistance) {
                minDistance = distance;
                bestElevator = elevator;
            }
        }
        return bestElevator;
    }

    private int calculateEffectiveDistance(Elevator elevator, int floor) {
        int currentFloor = elevator.getCurrentFloor();
        int topFloor = elevator.topFloorRequest();
        int bottomFloor = elevator.bottomFloorRequest();

        return switch (elevator.getState()) {
            case IDLE -> Math.abs(currentFloor - floor);
            case MOVING_UP ->
                    floor >= currentFloor ? floor - currentFloor : (topFloor - currentFloor + topFloor - floor);
            case MOVING_DOWN ->
                    floor <= currentFloor ? currentFloor - floor : (floor - currentFloor + floor - bottomFloor);
            case null -> Integer.MAX_VALUE;
        };
    }
}
