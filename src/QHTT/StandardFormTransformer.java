package QHTT;

import java.util.ArrayList;
import java.util.List;

public class StandardFormTransformer        //chuyen ve dang "=" va them he so de dung dang bang
{

    public static class StandardLPModel
    {
        public double[] objectiveCoeffs;
        public List<double[]> constraintCoeffs = new ArrayList<>();
        public List<Double> rhsValues = new ArrayList<>();
        public int numOriginalVars;
        public int numSlackVars;
    }

    public static StandardLPModel convertToStandardForm(StandardFormConverter.LPModel _lpModel)
    {
        StandardLPModel standardModel = new StandardLPModel();
        int numVars = _lpModel.objectiveCoeffs.length;
        int numConstraints = _lpModel.constraintCoeffs.size();

        standardModel.numOriginalVars = numVars;
        standardModel.numSlackVars = numConstraints;

        //bo sung cac bien w1, w2
        int totalVars = numVars + numConstraints;

        //mo rong ham muc tieu
        standardModel.objectiveCoeffs = new double[totalVars];
        for(int i = 0; i < numVars; i++)
        {
            standardModel.objectiveCoeffs[i] = _lpModel.objectiveCoeffs[i];
        }
        for(int i = numVars; i < totalVars; i++)
        {
            standardModel.objectiveCoeffs[i] = 0.0; //he so bien w1, w2 = 0
        }

        //mo rong cac rang buoc
        for(int i = 0; i < numConstraints; i++)
        {
            double[] originalCoeffs = _lpModel.constraintCoeffs.get(i);
            double[] extendedCoeffs = new double[totalVars];

            // sao chep cac he so goc
            for(int j = 0; j < numVars; j++)
            {
                extendedCoeffs[j] = originalCoeffs[j];
            }

            //them bien va cho he so bien do bang 1
            extendedCoeffs[numVars + i] = 1.0;

            standardModel.constraintCoeffs.add(extendedCoeffs);
            standardModel.rhsValues.add(_lpModel.rhsValues.get(i));
        }

        return standardModel;
    }
}
