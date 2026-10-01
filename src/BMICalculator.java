import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class BMICalculator {
    private JPanel BMICalculator;
    private JPanel mainPanel;
    private JComboBox unitComboBox;
    private JTextField weightField;
    private JTextField heightField;
    private JButton calculateButton;
    private JLabel resultLabel;
    private JTextArea BMIVALUESUnderweightLessTextArea;
    private JLabel BMICAL;
    private JLabel WightLable;
    private JLabel HeightLable;

    // Constructor to connect the action listener to the button
    public BMICalculator() {
        calculateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                calculateBMI();
            }
        });
    }

    // Core business logic to calculate BMI values
    private void calculateBMI() {
        try {
            double weight = Double.parseDouble(weightField.getText());
            double height = Double.parseDouble(heightField.getText());
            double bmi = 0;

            // Prevent crash or infinite values if height is 0 or negative
            if (height <= 0 || weight <= 0) {
                JOptionPane.showMessageDialog(mainPanel, "Height and Weight must be greater than zero.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String selectedUnit = (String) unitComboBox.getSelectedItem();

            // Apply formula based on metric or english selection
            if (selectedUnit != null && selectedUnit.contains("Metric")) {
                // FIX: If user inputs height in centimeters (e.g., 170 instead of 1.7), convert it to meters automatically
                if (height > 3.0) {
                    height = height / 100.0;
                }
                bmi = weight / (height * height);
            } else {
                // English formula: (weight * 703) / (height * height)
                bmi = (weight * 703) / (height * height);
            }

            // Determine specific health tier threshold classification
            String category;
            if (bmi < 18.5) {
                category = "Underweight";
            } else if (bmi <= 24.9) {
                category = "Normal";
            } else if (bmi <= 29.9) {
                category = "Overweight";
            } else {
                category = "Obese";
            }

            // Update UI components with computed results
            resultLabel.setText(String.format("BMI: %.2f (%s)", bmi, category));

        } catch (NumberFormatException ex) {
            // Error handling for empty or non-numeric inputs
            JOptionPane.showMessageDialog(mainPanel, "Please enter valid numeric values.", "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("BMICalculator");
        frame.setContentPane(new BMICalculator().BMICalculator);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setSize(450, 400); // Fixed dimensions for comfortable visibility
        frame.setVisible(true);
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }
}
