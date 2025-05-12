package QHTT;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class StandardFormConverter extends AppPanel
{
    public static class LPModel
    {
        public double[] objectiveCoeffs;                                //he so ham muc tieu
        public List<double[]> constraintCoeffs = new ArrayList<>();     //ma tran he so rang buoc BDT
        public List<String> constraintSigns = new ArrayList<>();        //ma tran dau
        public List<Double> rhsValues = new ArrayList<>();              //danh sach he so tu do
    }

    public static LPModel parseInput(AppPanel _app, int _numVars, int _numCons)
    {
        LPModel lpModel = new LPModel();

        //Xu ly ham muc tieu
        JPanel objectivePanel = _app.objectivePanel;
        JComboBox<String> maxMinBox = (JComboBox<String>) objectivePanel.getComponent(0);
        boolean isMax = maxMinBox.getSelectedItem().equals("Max");

        lpModel.objectiveCoeffs = new double[_numVars];
        int offset = 2;

        for(int i = 0; i < _numVars; i++)
        {
            JTextField field = (JTextField) objectivePanel.getComponent(offset + i * 2);
            double coeff = Double.parseDouble(field.getText().trim());

            lpModel.objectiveCoeffs[i] = isMax ? -coeff : coeff;        //neu max thi nhan voi -1
        }

        //Xu ly cac rang buoc
        JPanel constraintsPanel = _app.constraintsPanel;

        for(int i = 0; i < _numCons; i++)
        {
            JPanel rowPanel = (JPanel) constraintsPanel.getComponent(i);
            double[] coeffs = new double[_numVars];

            //doc he so rang buoc
            for(int j = 0; j < _numVars; j++)
            {
                JTextField field = (JTextField) rowPanel.getComponent(j * 2);
                coeffs[j] = Double.parseDouble(field.getText().trim());
            }

            //doc dau rang buoc
            JComboBox<String> signBox = (JComboBox<String>) rowPanel.getComponent(_numVars * 2);
            String sign = (String) signBox.getSelectedItem();

            //doc he so tu do
            JTextField rhsField = (JTextField) rowPanel.getComponent(_numVars * 2 + 1);
            double rhsValue = Double.parseDouble(rhsField.getText().trim());

            lpModel.constraintCoeffs.add(coeffs);
            lpModel.constraintSigns.add(sign);
            lpModel.rhsValues.add(rhsValue);
        }

        return lpModel;
    }

}
