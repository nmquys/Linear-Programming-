package QHTT;

import java.util.List;

public class TableDataExtractor {

    /**
     * Chuyển dữ liệu từ StandardLPModel sang bảng double[][] dùng cho Simplex.
     * Hàm mục tiêu sẽ được đặt ở dòng CUỐI cùng của bảng.
     *
     * @param model Mô hình bài toán dạng chuẩn.
     * @return Bảng dữ liệu dùng cho thuật toán Simplex.
     */
    public static double[][] extractTableData(StandardFormTransformer.StandardLPModel model) {
        int rows = model.constraintCoeffs.size() + 1; // +1 cho dòng hàm mục tiêu
        int cols = model.objectiveCoeffs.length + 1;  // +1 cho vế phải (b)

        double[][] table = new double[rows][cols];

        // Gán các ràng buộc vào các dòng đầu tiên
        List<double[]> constraints = model.constraintCoeffs;
        for (int i = 0; i < constraints.size(); i++) {
            double[] row = constraints.get(i);
            for (int j = 0; j < row.length; j++) {
                table[i][j] = row[j];
            }
            table[i][cols - 1] = model.rhsValues.get(i);
        }

        // Gán hệ số hàm mục tiêu vào dòng cuối cùng
        int lastRow = rows - 1;
        for (int j = 0; j < model.objectiveCoeffs.length; j++) {
            table[lastRow][j] = -model.objectiveCoeffs[j];
        }

        return table;
    }
}