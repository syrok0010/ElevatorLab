import javax.swing.*;
import java.awt.*;

public class BuildingUI extends JFrame implements ElevatorListener {
    private final Building building;
    private final JPanel[] elevatorPanels;

    public BuildingUI(Building building) {
        this.building = building;
        this.elevatorPanels = new JPanel[building.elevators.size()];

        setTitle("Elevator System");
        setSize(400, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(1, building.elevators.size()));

        for (int i = 0; i < building.elevators.size(); i++) {
            elevatorPanels[i] = createElevatorPanel(i + 1);
            add(elevatorPanels[i]);
        }

        JPanel controlPanel = createControlPanel();
        add(controlPanel, BorderLayout.SOUTH);
    }

    private JPanel createElevatorPanel(int elevatorId) {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createTitledBorder("Elevator " + elevatorId));
        panel.setLayout(new GridLayout(10, 1));
        for (int i = Building.TopFloor; i >= Building.BottomFloor; i--) {
            JLabel label = new JLabel("Floor " + i);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            panel.add(label);
        }
        return panel;
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createTitledBorder("Control Panel"));
        panel.setLayout(new GridLayout(2, 1));

        JLabel label = new JLabel("Request Elevator at Floor:");
        label.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(label);

        JComboBox<Integer> floorComboBox = new JComboBox<>();
        for (int i = Building.BottomFloor; i <= Building.TopFloor; i++) {
            floorComboBox.addItem(i);
        }

        JButton requestButton = new JButton("Request");
        requestButton.addActionListener(e -> {
            if (floorComboBox.getSelectedItem() == null) return;
            int floor = (int) floorComboBox.getSelectedItem();
            building.requestElevator(floor);
        });

        panel.add(floorComboBox);
        panel.add(requestButton);

        return panel;
    }


    @Override
    public void onElevatorStatusChange(int elevatorId, int floor, Elevator.ElevatorState state) {
        System.out.println(elevatorId + " " + floor + " " + state);
        SwingUtilities.invokeLater(() -> {
            JPanel panel = elevatorPanels[elevatorId - 1];
            for (Component component : panel.getComponents()) {
                if (component instanceof JLabel label) {
                    if (label.getText().contains("Floor")) {
                        label.setBackground(null);
                        label.setOpaque(false);
                    }
                }
            }
            JLabel currentLabel = (JLabel) panel.getComponent(Building.TopFloor - floor);
            currentLabel.setBackground(switch (state) {
                case IDLE -> Color.RED;
                case MOVING_UP -> Color.GREEN;
                case MOVING_DOWN -> Color.BLUE;
            });
            currentLabel.setOpaque(true);
        });
    }

    @Override
    public void personEntered(int elevatorId, int floor) {}
}
