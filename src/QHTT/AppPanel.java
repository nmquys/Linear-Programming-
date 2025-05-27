package QHTT;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class AppPanel extends JFrame
{
    private JTextField varField, constraintField;
    private JPanel inputPanel;
    protected JPanel objectivePanel;
    protected JPanel constraintsPanel;
    private JButton generateButton, convertButton;

    public AppPanel()
    {
        this.init();

        this.setTitle("Linear Programming Input");
        this.setSize(500, 350);
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

        convertButton = new JButton("Convert");
        inputPanel.add(convertButton);

        inputPanel.revalidate();
        inputPanel.repaint();

        convertButton.addActionListener(e ->
        {
            try {
                convertAndDisplayStandardForm(numVars, numCons);
            } catch (Exception ex)
            {
                JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage());
            }
        });
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

    private void ConstraintsPanel(int _numVars, int _numCons)
    {
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


    private void convertAndDisplayStandardForm(int numVars, int numCons)
    {
        StandardFormTransformer.LPModel model = new StandardFormTransformer.LPModel();
        model.objectiveCoeffs = new double[numVars];
        model.constraintCoeffs = new ArrayList<>();
        model.rhsValues = new ArrayList<>();
        model.constraintSigns = new ArrayList<>();

        //Lấy hệ số hàm mục tiêu
        Component[] components = objectivePanel.getComponents();

        JComboBox<String> maxMinBox = (JComboBox<String>) objectivePanel.getComponent(0);
        model.isMax = maxMinBox.getSelectedItem().equals("Max");

        int compIndex = 2; //bỏ qua JComboBox và JLabel "z ="
        for (int i = 0; i < numVars; i++)
        {
            JTextField coeffField = (JTextField) components[compIndex];
            if(model.isMax)
            {
                model.objectiveCoeffs[i] = -Double.parseDouble(coeffField.getText());
            }
            else
            {
                model.objectiveCoeffs[i] = Double.parseDouble(coeffField.getText());
            }
            compIndex += 2; // bỏ qua JLabel "x +"
        }

        // Lấy hệ số ràng buộc và dấu
        for (int i = 0; i < numCons; i++)
        {
            JPanel rowPanel = (JPanel) constraintsPanel.getComponent(i);
            Component[] rowComponents = rowPanel.getComponents();

            double[] coeffs = new double[numVars];
            int index = 0;
            for (int j = 0; j < numVars; j++)
            {
                JTextField coeffField = (JTextField) rowComponents[index];
                coeffs[j] = Double.parseDouble(coeffField.getText());
                index += 2; // skip JLabel
            }

            JComboBox<String> signBox = (JComboBox<String>) rowComponents[index];
            String sign = (String) signBox.getSelectedItem();

            JTextField rhsField = (JTextField) rowComponents[index + 1];
            double rhs = Double.parseDouble(rhsField.getText());

            // Nếu dấu là >= thì đổi dấu sang <= (chuyển về <=)
            if (sign.equals(">="))
            {
                for (int j = 0; j < coeffs.length; j++)
                {
                    coeffs[j] *= -1;
                }
                rhs *= -1;
                sign = "<=";
            }

            model.constraintCoeffs.add(coeffs);
            model.rhsValues.add(rhs);
            model.constraintSigns.add(sign);
        }

        // Chuyển sang dạng chuẩn
        StandardFormTransformer.StandardLPModel standardModel = StandardFormTransformer.convertToStandardForm(model);

        // Hiển thị bảng
        StandardFormTransformer viewer = new StandardFormTransformer();
        viewer.showStandardModel(standardModel);
    }

    public static void main(String[] args)
    {
        new AppPanel();
    }
}