import java.util.Random;

public class RequestGenerator implements ElevatorListener {
    private static final int MaxRequests = 50;
    private final Random rand = new Random();
    private final Building building;

    RequestGenerator(Building building) {
        this.building = building;
    }

    public void generateRequests() {
        for (int i = 0; i < MaxRequests; i++) {
            building.requestElevator(rand.nextInt(10));
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public void onElevatorStatusChange(int elevatorId, int floor, Elevator.ElevatorState state) {
    }

    @Override
    public void personEntered(int elevatorId, int floor) {
        building.requestElevator(rand.nextInt(10), Elevator.RequestType.OUT, elevatorId);
    }
}
