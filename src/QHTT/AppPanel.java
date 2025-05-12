package QHTT;

import javax.swing.*;
import java.awt.*;

public class AppPanel extends JFrame
{
    private JTextField varField, constraintField;
    private JPanel inputPanel;
    protected JPanel objectivePanel;
    protected JPanel constraintsPanel;
    private JButton generateButton, solveButton;

    public AppPanel()
    {
        this.init();

        this.setTitle("Linear Programming Input");
        this.setSize(500, 700);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
    }

    private void init()
    {
        JPanel mainPanel = new JPanel(new BorderLayout());
        this.setContentPane(mainPanel);

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Total Variables:"));
        varField = new JTextField(3);
        topPanel.add(varField);

        topPanel.add(new JLabel("Total Constraints:"));
        constraintField = new JTextField(3);
        topPanel.add(constraintField);

        generateButton = new JButton("Generate");
        topPanel.add(generateButton);
        mainPanel.add(topPanel, BorderLayout.NORTH);

        inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));
        inputPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JScrollPane scrollPane = new JScrollPane(inputPanel);
        scrollPane.setBorder(null);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        generateButton.addActionListener(e -> GenerateInputs());
    }

    private void GenerateInputs()
    {
        inputPanel.removeAll();

        int numVars = Integer.parseInt(varField.getText());
        int numCons = Integer.parseInt(constraintField.getText());

        ObjectivePanel(numVars);
        ConstraintsPanel(numVars, numCons);

        solveButton = new JButton("Solve");
        inputPanel.add(solveButton);

        inputPanel.revalidate();
        inputPanel.repaint();
    }

    private void ObjectivePanel(int _numVars)
    {
        objectivePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        objectivePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JComboBox<String> maxMinBox = new JComboBox<>(new String[]{"Max", "Min"});
        objectivePanel.add(maxMinBox);
        objectivePanel.add(new JLabel(" z = "));

        PrintLoop(_numVars, objectivePanel);

        inputPanel.add(objectivePanel);
    }

    private void ConstraintsPanel(int _numVars, int _numCons) {
        constraintsPanel = new JPanel();
        constraintsPanel.setLayout(new BoxLayout(constraintsPanel, BoxLayout.Y_AXIS));
        constraintsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        for (int i = 0; i < _numCons; i++)
        {
            JPanel rowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
            rowPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

            PrintLoop(_numVars, rowPanel);

            JComboBox<String> signBox = new JComboBox<>(new String[]{"<=", ">=", "="});
            rowPanel.add(signBox);

            JTextField rhsField = new JTextField(3);
            rowPanel.add(rhsField);

            constraintsPanel.add(rowPanel);
        }

        inputPanel.add(constraintsPanel);
    }

    private void PrintLoop(int _numVars, JPanel _x)
    {
        for (int j = 0; j < _numVars; j++)
        {
            JTextField coeffField = new JTextField(3);
            _x.add(coeffField);
            _x.add(new JLabel("x" + (j + 1) + (j < _numVars - 1 ? " + " : "")));
        }
    }

    public static void main(String[] args)
    {
        new AppPanel();
    }
}
