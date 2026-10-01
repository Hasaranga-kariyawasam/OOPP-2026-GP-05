package bmicalculator;

import javax.swing.*;

public class BMIFrame extends JFrame {
    // These fields come from BMIFrame.form (GUI Designer)
    private JPanel rootPanel;
    private JComboBox<String> unitBox;
    private JLabel heightLabel;
    private JLabel weightLabel;
    private JTextField heightField;
    private JTextField weightField;
    private JButton submitButton;
    private JButton clearButton;
    private JLabel answerLabel;

    public BMIFrame() {
        super("BMI Calculator");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setContentPane(rootPanel);

        updateLabels();
        unitBox.addActionListener(e -> updateLabels());
        submitButton.addActionListener(e -> calculate());
        clearButton.addActionListener(e -> clearFields());
        getRootPane().setDefaultButton(submitButton);

        pack();
        setLocationRelativeTo(null);
    }

    private boolean isMetric() {
        return unitBox.getSelectedIndex() == 0;
    }

    private void updateLabels() {
        heightLabel.setText(isMetric() ? "Height (m)" : "Height (in)");
        weightLabel.setText(isMetric() ? "Weight (kg)" : "Weight (lb)");
    }

    private void calculate() {
        try {
            double height = Double.parseDouble(heightField.getText().trim());
            double weight = Double.parseDouble(weightField.getText().trim());
            if (height <= 0 || weight <= 0) {
                answerLabel.setText("Height and weight must be more than 0");
                return;
            }
            double bmi = isMetric()
                    ? weight / (height * height)
                    : (weight * 703) / (height * height);
            answerLabel.setText(String.format("Your BMI is %.1f (%s)", bmi, category(bmi)));
        } catch (NumberFormatException ex) {
            answerLabel.setText("Please enter valid numbers");
        }
    }

    private String category(double bmi) {
        if (bmi < 18.5) return "Underweight";
        if (bmi < 25) return "Normal";
        if (bmi < 30) return "Overweight";
        return "Obese";
    }

    private void clearFields() {
        heightField.setText("");
        weightField.setText("");
        answerLabel.setText("Enter your details and press Submit");
    }
}
