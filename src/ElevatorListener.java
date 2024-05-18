public interface ElevatorListener {
    void onElevatorStatusChange(int elevatorId, int floor, Elevator.ElevatorState state);
    void personEntered(int elevatorId, int floor);
}
