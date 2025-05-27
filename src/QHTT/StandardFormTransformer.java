package QHTT;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class StandardFormTransformer extends JFrame {
    private JButton solveButton;
    private Double[][] tableData;

    // Model chứa dữ liệu đầu vào (chưa chuẩn hóa)
    public static class LPModel {
        public double[] objectiveCoeffs;                    // Hệ số hàm mục tiêu
        public List<double[]> constraintCoeffs = new ArrayList<>(); // Hệ số ràng buộc
        public List<String> constraintSigns = new ArrayList<>();    // Dấu ràng buộc (<=, =, >=)
        public List<Double> rhsValues = new ArrayList<>();          // Hệ số vế phải
        public boolean isMax;
    }

    // Model chứa dữ liệu đã chuyển sang dạng chuẩn
    public static class StandardLPModel {
        public double[] objectiveCoeffs;                   // Hệ số hàm mục tiêu mở rộng
        public List<double[]> constraintCoeffs = new ArrayList<>(); // Hệ số ràng buộc mở rộng
        public List<Double> rhsValues = new ArrayList<>();
        public List<String> signs = new ArrayList<>();
        public int numOriginalVars;
        public int numSlackVars;
        public boolean isMax;
    }

    // Phân tích dữ liệu từ giao diện, trả về LPModel gốc
    /*public static LPModel parseInput(AppPanel _app, int _numVars, int _numCons) {
        LPModel lpModel = new LPModel();

        JPanel objectivePanel = _app.objectivePanel;
        JComboBox<String> maxMinBox = (JComboBox<String>) objectivePanel.getComponent(0);
        boolean isMax = maxMinBox.getSelectedItem().equals("Max");
        lpModel.isMax = isMax;

        lpModel.objectiveCoeffs = new double[_numVars];
        int offset = 2; // Giả sử offset phần tử nhập hệ số bắt đầu

        for (int i = 0; i < _numVars; i++) {
            JTextField field = (JTextField) objectivePanel.getComponent(offset + i * 2);
            double coeff = Double.parseDouble(field.getText().trim());
            lpModel.objectiveCoeffs[i] = isMax ? -coeff : coeff; //Max đổi dấu để dùng Simplex minimization
        }

        JPanel constraintsPanel = _app.constraintsPanel;

        for (int i = 0; i < _numCons; i++) {
            JPanel rowPanel = (JPanel) constraintsPanel.getComponent(i);
            double[] coeffs = new double[_numVars];

            // Đọc dấu ràng buộc
            JComboBox<String> signBox = (JComboBox<String>) rowPanel.getComponent(_numVars * 2);
            String sign = (String) signBox.getSelectedItem();

            // Đọc hệ số ràng buộc
            for (int j = 0; j < _numVars; j++) {
                JTextField field = (JTextField) rowPanel.getComponent(j * 2);
                coeffs[j] = Double.parseDouble(field.getText().trim());
            }

            // Đọc hệ số vế phải
            JTextField rhsField = (JTextField) rowPanel.getComponent(_numVars * 2 + 1);
            double rhsValue = Double.parseDouble(rhsField.getText().trim());

            // Nếu dấu là >= thì đổi dấu và chuyển thành <=
            if (sign.equals(">=")) {
                for (int j = 0; j < _numVars; j++) {
                    coeffs[j] *= -1;
                }
                rhsValue *= -1;
                sign = "<=";
            }

            // Lưu lại
            lpModel.constraintCoeffs.add(coeffs);
            lpModel.constraintSigns.add(sign);
            lpModel.rhsValues.add(rhsValue);
        }

        return lpModel;
    }*/

    //Chuyển đổi sang dạng chuẩn, xử lý dấu và thêm biến slack
    public static StandardLPModel convertToStandardForm(LPModel _lpModel) {
        StandardLPModel standardModel = new StandardLPModel();
        int numVars = _lpModel.objectiveCoeffs.length;
        int numConstraints = _lpModel.constraintCoeffs.size();
        standardModel.isMax = _lpModel.isMax;

        int numSlackVars = 0;

        //Đếm số biến slack cần thêm
        for (String sign : _lpModel.constraintSigns) {
            if (sign.equals("<=")) {
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
        public void showStandardModel(StandardLPModel _model) {
            StandardLPModel standardModel = new StandardLPModel();
            int rows = _model.constraintCoeffs.size() + 1; // +1 cho hàm mục tiêu
            int cols = _model.objectiveCoeffs.length + 1;  // +1 cho RHS
            boolean isMax = _model.isMax;


            String[] columnNames = new String[cols];
            // Đặt tên biến gốc x1, x2,...
            for (int i = 0; i < _model.numOriginalVars; i++) {
                columnNames[i] = "x" + (i + 1);
            }
            // Biến slack w1, w2,...
            for (int i = _model.numOriginalVars; i < _model.numOriginalVars + _model.numSlackVars; i++) {
                columnNames[i] = "w" + (i - _model.numOriginalVars + 1);
            }


            columnNames[cols - 1] = "b";

            Double[][] tableData = new Double[rows][cols];

            // Hàm mục tiêu (dòng 0)
            for (int j = 0; j < _model.objectiveCoeffs.length; j++) {
                double value = _model.objectiveCoeffs[j];
                tableData[0][j] = value;
                System.out.println("Selected Objective: " + (isMax ? "Maximize" : "Minimize"));

            }

            // Ràng buộc
            for (int i = 0; i < _model.constraintCoeffs.size(); i++) {
                double[] coeffs = _model.constraintCoeffs.get(i);
                for (int j = 0; j < coeffs.length; j++) {
                    tableData[i + 1][j] = coeffs[j];
                }
                tableData[i + 1][cols - 1] = _model.rhsValues.get(i);
            }

            for (int j = 1; j < _model.numSlackVars + 2; j++) {
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

            JFrame frame = new JFrame("Bảng Dạng Chuẩn");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setSize(700, 350);
            frame.setLocationRelativeTo(null);
            frame.add(mainPanel);
            frame.setVisible(true);

            solveButton.addActionListener(e -> {
                try {
                    SimplexSolver.solveAndDisplay(_model);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, "Lỗi: " + ex.getMessage());
                }
            });

        }


    }
