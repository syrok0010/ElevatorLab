import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        int elevatorCount = 2;
        var building = new Building(elevatorCount);
        var generator = new RequestGenerator(building);
        var buildingUI = new BuildingUI(building);
        List<ElevatorListener> listeners = new ArrayList<>();
        listeners.add(buildingUI);
        listeners.add(generator);
        building.addListeners(listeners);
        for (int i = 1; i <= elevatorCount; i++) {
            buildingUI.onElevatorStatusChange(i, 0, Elevator.ElevatorState.IDLE);
        }
        buildingUI.setVisible(true);

        var genThread = new Thread(generator::generateRequests);
        genThread.start();
    }
}
