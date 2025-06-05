package QHTT;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class StandardFormTransformer extends JFrame
{
    private JButton solveButton;
    private Double[][] tableData;

    // Model chứa dữ liệu đầu vào (chưa chuẩn hóa)
    public static class LPModel
    {
        public double[] objectiveCoeffs;                    // Hệ số hàm mục tiêu
        public List<double[]> constraintCoeffs = new ArrayList<>(); // Hệ số ràng buộc
        public List<String> constraintSigns = new ArrayList<>();    // Dấu ràng buộc (<=, =, >=)
        public List<Double> rhsValues = new ArrayList<>();          // Hệ số vế phải
        public boolean isMax;
    }

    // Model chứa dữ liệu đã chuyển sang dạng chuẩn
    public static class StandardLPModel
    {
        public double[] objectiveCoeffs;                   // Hệ số hàm mục tiêu mở rộng
        public List<double[]> constraintCoeffs = new ArrayList<>(); // Hệ số ràng buộc mở rộng
        public List<Double> rhsValues = new ArrayList<>();
        public List<String> signs = new ArrayList<>();
        public int numOriginalVars;
        public int numSlackVars;
        public boolean isMax;
    }


    //Chuyển đổi sang dạng chuẩn, xử lý dấu và thêm biến slack
    public static StandardLPModel convertToStandardForm(LPModel _lpModel)
    {
        StandardLPModel standardModel = new StandardLPModel();
        int numVars = _lpModel.objectiveCoeffs.length;
        int numConstraints = _lpModel.constraintCoeffs.size();
        standardModel.isMax = _lpModel.isMax;

        int numSlackVars = 0;

        //Đếm số biến slack cần thêm
        for (String sign : _lpModel.constraintSigns)
        {
            if (sign.equals("<="))
            {
                numSlackVars++;
            }
        }

        standardModel.numOriginalVars = numVars;
        standardModel.numSlackVars = numSlackVars;

        int totalVars = numVars + numSlackVars;
        standardModel.objectiveCoeffs = new double[totalVars];

        //Sao chép hệ số hàm mục tiêu biến gốc
        for (int i = 0; i < numVars; i++) {
            standardModel.objectiveCoeffs[i] = _lpModel.objectiveCoeffs[i];
        }

        //Biến slack hệ số 0 trong hàm mục tiêu
        for (int i = numVars; i < numVars + numSlackVars; i++) {
            standardModel.objectiveCoeffs[i] = 0.0;
        }


        int slackIndex = numVars;

        for (int i = 0; i < numConstraints; i++) {
            double[] originalCoeffs = _lpModel.constraintCoeffs.get(i);
            double[] extendedCoeffs = new double[totalVars];

            //Sao chép hệ số gốc
            for (int j = 0; j < numVars; j++) {
                extendedCoeffs[j] = originalCoeffs[j];
            }

            String sign = _lpModel.constraintSigns.get(i);

            if (sign.equals("<=")) {
                extendedCoeffs[slackIndex++] = 1.0;
            }//thêm biến slack


            standardModel.constraintCoeffs.add(extendedCoeffs);
            standardModel.rhsValues.add(_lpModel.rhsValues.get(i));
            standardModel.signs.add(sign);
        }

        return standardModel;
    }


        // Hiển thị bảng dạng chuẩn với JTable
        public void showStandardModel(StandardLPModel _model)
        {
            StandardLPModel standardModel = new StandardLPModel();
            int rows = _model.constraintCoeffs.size() + 1; // +1 cho hàm mục tiêu
            int cols = _model.objectiveCoeffs.length + 1;  // +1 cho RHS
            boolean isMax = _model.isMax;


            String[] columnNames = new String[cols];
            // Đặt tên biến gốc x1, x2,...
            for (int i = 0; i < _model.numOriginalVars; i++)
            {
                columnNames[i] = "x" + (i + 1);
            }
            // Biến slack w1, w2,...
            for (int i = _model.numOriginalVars; i < _model.numOriginalVars + _model.numSlackVars; i++)
            {
                columnNames[i] = "w" + (i - _model.numOriginalVars + 1);
            }


            columnNames[cols - 1] = "b";

            Double[][] tableData = new Double[rows][cols];

            // Hàm mục tiêu (dòng 0)
            for (int j = 0; j < _model.objectiveCoeffs.length; j++)
            {
                double value = _model.objectiveCoeffs[j];
                tableData[0][j] = value;

            }

            // Ràng buộc
            for (int i = 0; i < _model.constraintCoeffs.size(); i++)
            {
                double[] coeffs = _model.constraintCoeffs.get(i);
                for (int j = 0; j < coeffs.length; j++)
                {
                    tableData[i + 1][j] = coeffs[j];
                }
                tableData[i + 1][cols - 1] = _model.rhsValues.get(i);
            }

            for (int j = 1; j < _model.numSlackVars + 2; j++)
            {
                tableData[0][cols - j] = 0.00;
            }

            JTable table = new JTable(tableData, columnNames);
            JScrollPane scrollPane = new JScrollPane(table);

            JPanel mainPanel = new JPanel(new BorderLayout());
            mainPanel.add(scrollPane, BorderLayout.CENTER);

            solveButton = new JButton("Solve");
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            buttonPanel.add(solveButton);

            mainPanel.add(buttonPanel, BorderLayout.SOUTH);

            JFrame frame = new JFrame("Standard Form");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setSize(700, 350);
            frame.setLocationRelativeTo(null);
            frame.add(mainPanel);
            frame.setVisible(true);

            solveButton.addActionListener(e ->
            {
                try
                {
                    SimplexSolver.solveAndDisplay(_model);
                    frame.setVisible(false);
                } catch (Exception ex)
                {
                    JOptionPane.showMessageDialog(frame, "Lỗi: " + ex.getMessage());
                }
            });
        }
    }
