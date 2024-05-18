import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Elevator implements Runnable {
    public enum ElevatorState {
        IDLE, MOVING_UP, MOVING_DOWN
    }
    public enum RequestType {
        IN, OUT
    }
    public record Request(int floor, RequestType type){}


    private int currentFloor;
    private final ConcurrentLinkedQueue<Request> requestQueue;
    private ElevatorState state;
    private final int id;
    private final ArrayList<ElevatorListener> listeners;

    public int getId() {
        return id;
    }

    public int topFloorRequest() {
        synchronized (requestQueue)
        {
            return this.requestQueue.stream().mapToInt(e -> e.floor).max().orElse(Building.TopFloor);
        }
    }

    public int bottomFloorRequest() {
        synchronized (requestQueue) {
            return this.requestQueue.stream().mapToInt(e -> e.floor).min().orElse(Building.BottomFloor);
        }
    }

    public int getCurrentFloor() {
        return currentFloor;
    }

    private void setCurrentFloor(int newCurrentFloor) {
        currentFloor = newCurrentFloor;
        if (listeners != null) {
            for (ElevatorListener listener : listeners)
                listener.onElevatorStatusChange(id, currentFloor, state);
        }
    }

    public ElevatorState getState() {
        return state;
    }

    private void setCurrentState(ElevatorState newState) {
        state = newState;
        if (listeners != null) {
            for (ElevatorListener listener : listeners)
                listener.onElevatorStatusChange(id, currentFloor, state);
        }
    }

    public Elevator(int id) {
        this.id = id;
        setCurrentState(ElevatorState.IDLE);
        setCurrentFloor(Building.BottomFloor);
        this.requestQueue = new ConcurrentLinkedQueue<>();
        this.listeners = new ArrayList<>();
    }

    public void addElevatorListener(ElevatorListener listener) {
        listeners.add(listener);
    }

    public void addRequest(Request request) {
        if (requestQueue.peek() != null && requestQueue.peek().floor == request.floor)
            return;
        System.out.println("Request added to Elevator " + id + ": " + request);
        requestQueue.add(request);
        synchronized (this) {
            notifyAll();
        }
    }

    private Integer getNextFloor() {
        synchronized (requestQueue) {
            if (requestQueue.isEmpty())
                return null;

            return switch (state) {
                case IDLE -> requestQueue.peek().floor;
                case MOVING_UP -> requestQueue.stream().mapToInt(v -> v.floor).filter(e -> e >= currentFloor).min().orElseThrow();
                case MOVING_DOWN -> requestQueue.stream().mapToInt(v -> v.floor).filter(e -> e <= currentFloor).max().orElseThrow();
            };
        }
    }

    @Override
    public void run() {
        System.out.println("Elevator " + id + " has started");
        while (true) {
            Integer nextFloor;
            synchronized (this) {
                while ((nextFloor = getNextFloor()) == null) {
                    try {
                        setCurrentState(ElevatorState.IDLE);
                        System.out.println("Elevator " + id + " waits");
                        wait(); // Wait for new requests
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                if (nextFloor == currentFloor) {
                    requestQueue.poll();
                    continue;
                }
                setCurrentState(nextFloor > currentFloor ? ElevatorState.MOVING_UP : ElevatorState.MOVING_DOWN);
            }
            moveTo(nextFloor);
        }
    }

    private void moveTo(int floor) {
        try {
            System.out.println("Elevator " + id + " moving from " + currentFloor + " to " + floor);
            while (currentFloor != floor) {
                this.setCurrentFloor(currentFloor < floor ? currentFloor + 1 : currentFloor - 1);
                boolean someoneEntered, someRemoved;
                synchronized (requestQueue) {
                    someoneEntered = requestQueue.stream().anyMatch(r -> r.floor == currentFloor && r.type == RequestType.IN);
                    someRemoved = requestQueue.removeIf(r -> r.floor == currentFloor);
                }
                if (currentFloor != floor && someRemoved)
                    stopAndContinue();
                if (someoneEntered)
                    for (ElevatorListener listener : listeners)
                        listener.personEntered(id, currentFloor);
                Thread.sleep(1000L);
            }
            System.out.println("Elevator " + id + " arrived at floor " + currentFloor);
            setCurrentState(ElevatorState.IDLE);
            Thread.sleep(1000L);
            requestQueue.removeIf(r -> r.floor == currentFloor);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void stopAndContinue() throws InterruptedException {
        var currentState = this.state;
        Thread.sleep(1000L);
        setCurrentState(ElevatorState.IDLE);
        Thread.sleep(1000L);
        setCurrentState(currentState);
    }
}