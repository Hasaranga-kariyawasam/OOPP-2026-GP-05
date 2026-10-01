package bmicalculator;

import javax.swing.*;
import java.awt.*;

public class BMIFrame extends JFrame {
    private final JComboBox<String> unitBox =
            new JComboBox<>(new String[]{"Metric (kg, m)", "English (lb, in)"});
    private final JLabel heightLabel = new JLabel();
    private final JLabel weightLabel = new JLabel();
    private final JTextField heightField = new JTextField(10);
    private final JTextField weightField = new JTextField(10);
    private final JLabel answerLabel = new JLabel("Enter your details and press Submit");

    public BMIFrame() {
        super("BMI Calculator");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel form = new JPanel(new GridLayout(3, 2, 8, 8));
        form.add(new JLabel("Units"));
        form.add(unitBox);
        form.add(heightLabel);
        form.add(heightField);
        form.add(weightLabel);
        form.add(weightField);

        JButton submit = new JButton("Submit");
        JButton clear = new JButton("Clear");
        JPanel buttons = new JPanel();
        buttons.add(submit);
        buttons.add(clear);

        JTextArea reference = new JTextArea(
                "BMI VALUES\n"
              + "Underweight: less than 18.5\n"
              + "Normal: between 18.5 and 24.9\n"
              + "Overweight: between 25 and 29.9\n"
              + "Obese: 30 or greater");
        reference.setEditable(false);
        reference.setOpaque(false);

        answerLabel.setBorder(BorderFactory.createEtchedBorder());

        JPanel south = new JPanel(new BorderLayout(8, 8));
        south.add(answerLabel, BorderLayout.NORTH);
        south.add(reference, BorderLayout.CENTER);

        JPanel root = new JPanel(new BorderLayout(8, 12));
        root.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        root.add(form, BorderLayout.NORTH);
        root.add(buttons, BorderLayout.CENTER);
        root.add(south, BorderLayout.SOUTH);
        setContentPane(root);

        updateLabels();
        unitBox.addActionListener(e -> updateLabels());
        submit.addActionListener(e -> calculate());
        clear.addActionListener(e -> clearFields());
        getRootPane().setDefaultButton(submit);

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
