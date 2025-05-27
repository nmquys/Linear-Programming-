package QHTT;

public class Simplex {
    private int rows, cols; // row and column
    private double[][] table; // simplex tableau
    private boolean solutionIsUnbounded = false;

    public static enum ERROR{
        NOT_OPTIMAL,
        IS_OPTIMAL,
        UNBOUNDED,
        NO_SOLUTION
    };
    
    public Simplex(int numOfConstraints, int numOfUnknowns){
        rows = numOfConstraints+1; // row number + 1 
        cols = numOfUnknowns+1;   // column number + 1
        table = new double[rows][]; // create a 2d array



        // initialize references to arrays
        for(int i = 0; i < rows; i++){
            table[i] = new double[cols];
        }
    }
    
    // prints out the simplex tableau
    public void print(){
        for(int i = 0; i < rows; i++){
            for(int j = 0; j < cols; j++){
                String value = String.format("%.2f", table[i][j]);
                System.out.print(value + "\t");
            }
            System.out.println();
        }
        System.out.println();
    }

    
    // fills the simplex tableau with coefficients
    public void fillTable(double[][] data){
        for(int i = 0; i < table.length; i++){
            System.arraycopy(data[i], 0, this.table[i], 0, data[i].length);
        }
    }
    
    
    // finds the next entering column
    private int findEnteringColumn(int type){
        // type = 0: simplex
    	// type = 1: bland
    	
    	double[] values = new double[cols];
        int location = 0;
        
        int pos, count = 0; 
        for(pos = 0; pos < cols-1; pos++){
            if(table[rows-1][pos] > 0){
                //System.out.println("positive value found");
                count++;
            }
        }
        
        if(count > 0){
            for(int i = 0; i < cols-1; i++)
                values[i] = table[rows-1][i];
            //for(int i = 0; i < cols-1; i++)
             //   System.out.print(values[i] + "\t");
            //System.out.println();
            
            if(type == 1)
            	location = findLargestValue(values);
            else 
            	location = findFirstPositiveValue(values);
            
        } else location = -1;
        //System.out.println("location = " + location);
        return location;
    }
    
    // calculates the pivot row ratios
    private double[] calculateRatios(int column){
        double[] positiveEntries = new double[rows];
        double[] res = new double[rows];
        int allNegativeCount = 0;
        for(int i = 0; i < rows-1; i++){
            if(table[i][column] > 0){
                positiveEntries[i] = table[i][column];
            }
            else{
                positiveEntries[i] = -1;
                allNegativeCount++;
            }
            //System.out.println(positiveEntries[i]);
        }
        
        if(allNegativeCount == rows-1)
            this.solutionIsUnbounded = true;
        else{
            for(int i = 0;  i < rows-1; i++){
                double val = positiveEntries[i];
                if(val > 0){
                    res[i] = table[i][cols -1] / val;
                }
                else res[i] = -1;
            }
        }
        
        return res;
    }
    
    // computes the values of the simplex tableau
    // should be use in a loop to continously compute until
    // an optimal solution is found
    public ERROR compute(int type){
    	int pivotColumn = 0;
        if(type != 3)
        {
        	// step 1
        	if(checkOptimality() == true){
        		// check if and only if xi = x0 if phase1
        		if(type == 3){ 
        			for(int i = 1; i < cols; i++) {
        				if(table[rows-1][i] != 0) {
        					return ERROR.NO_SOLUTION;
        				}
        			}
        			if(table[rows-1][0] != -1.0){
        				return ERROR.NO_SOLUTION;
        			}
        			return ERROR.IS_OPTIMAL;
        		}
        		return ERROR.IS_OPTIMAL; // solution is optimal
        	}
        
        	// step 2
        	// find the entering column
        	pivotColumn = findEnteringColumn(type);
        	System.out.println("Pivot Column: "+pivotColumn);
        }
        // if type = 3 then we are using phase 1, i.e. we choose x0
        
        // step 3
        // find departing value
        double[] ratios = calculateRatios(pivotColumn);
		for (int i = 0; i < ratios.length; i++) {
			System.out.println(ratios[i] + "\t");
		}
		System.out.println();
        if(type != 3) {
        	if(solutionIsUnbounded == true)
        		return ERROR.UNBOUNDED;
        }
        else{
			for (int i = 0; i < ratios.length; i++) {
				ratios[i] = table[i][cols - 1] / table[i][pivotColumn];
			}
			solutionIsUnbounded = false; //skip for phase 1
			for (int i = 0; i < ratios.length; i++) {
				System.out.println(ratios[i] + "\t");
			}
			System.out.println();
        }
        
        //System.out.println("Ratios: ");
		//for (int i = 0; i < ratios.length; i++)
		//	System.out.print(ratios[i] + " ");
		//System.out.println();
		
        int pivotRow = findSmallestValue(ratios);
        if(type == 3) {
        	pivotRow = findLargestValue(ratios);
        }
        System.out.println("Pivot row: "+ pivotRow);
        
        // step 4
        // form the next tableau
        formNextTableau(pivotRow, pivotColumn);
        
        // since we formed a new table so return NOT_OPTIMAL
        return ERROR.NOT_OPTIMAL;
    }
    
    
    
    // Forms a new tableau from precomuted values.
    private void formNextTableau(int pivotRow, int pivotColumn){
        double pivotValue = table[pivotRow][pivotColumn];
        double[] pivotRowVals = new double[cols];
        double[] pivotColumnVals = new double[cols];
        double[] rowNew = new double[cols];
        
        // divide all entries in pivot row by entry inpivot column
        // get entry in pivot row
        System.arraycopy(table[pivotRow], 0, pivotRowVals, 0, cols);
        
        // get entry in pivot column
        for(int i = 0; i < rows; i++)
            pivotColumnVals[i] = table[i][pivotColumn];
        
        // divide values in pivot row by pivot value
        for(int  i = 0; i < cols; i++)
            rowNew[i] =  pivotRowVals[i] / pivotValue;
        
        // subtract from each of the other rows
        for(int i = 0; i < rows; i++){
            if(i != pivotRow){
                for(int j = 0; j < cols; j++){
                    double c = pivotColumnVals[i];
                    table[i][j] = table[i][j] - (c * rowNew[j]);
                }
            }
        }
        
        // replace the row
        System.arraycopy(rowNew, 0, table[pivotRow], 0, rowNew.length);
    }
    
    
    // find the smallest positive value in an array
    public int findSmallestValue(double[] data){
        double minimum ;
        int c, location = 0;
        minimum = -1;
        
        for(c = 0; c < data.length-1; c++){
            if(data[c] > 0){
            	if(minimum == -1) {
            		minimum = data[c];
            		location = c;
            	}
            	else if(Double.compare(data[c], minimum) < 0 && data[c] >= 0){
                    minimum = data[c];
                    location  = c;
                }
            }
            //System.out.println(data.length+" minimum ="+minimum);
        }
        
        return location;
    }
    // find the first positive value in an array
	private int findFirstPositiveValue(double[] data) {
		double minimum;
		int c, location = 0;
		minimum = data[0];

		for (c = 1; c < data.length; c++) {
			if (data[c] > 0) {
				minimum = data[c];
				location = c;
			}
		}

		return location;
	}
    
    // find the largest value in an array
    private int findLargestValue(double[] data){
        double maximum = 0;
        int c, location = 0;
        maximum = data[0];
        
        for(c = 1; c < data.length; c++){
            if(Double.compare(data[c], maximum) > 0){
                maximum = data[c];
                location  = c;
            }
        }
        
        return location;
    }
    
    // checks if the table is optimal
    public boolean checkOptimality(){
    	
        for(int i = 0; i < cols-1; i++){
            double val = table[rows-1][i];
            if(val > 0){
                return false; // not optimal
            }
        }
        return true;
    }

    // returns the simplex tableau
    public double[][] getTable() {
        return table;
    }
    
    // fills the simplex tableau with coefficients for phase 1
    public void fillTable_phase1(double[][] data){
        for(int i = 0; i < table.length - 1; i++){
            this.table[i][0] = -1;
        	System.arraycopy(data[i], 0, this.table[i], 1, data[i].length);
        }
        
		for (int i = 1; i < table[0].length; i++) {
			this.table[rows-1][i] = 0;
		}
		this.table[rows - 1][0] = -1;
    }
    
    // build table for phase 2
	public void fillTable_phase2(double[] data){
		//fill all 0 last row
		for(int i = 0; i < rows-1; i++){
            for(int j = 0; j < data.length; j++) {
            	table[i][j] = table[i][j+1];
            }
        }
		
		cols = cols - 1;

		
		for(int j = 0; j < data.length; j++) {
			table[rows - 1][j] = 0;
		}
	
		
        for(int j = 0; j < cols-rows; j++){
        	int num_0 = 0;
        	int idx_1 = -1;
            for(int i = 0; i < rows - 1; i++)
            {
            	if(table[i][j] == 0.0) {
            		//System.out.println("(" + i + "," + j + ")=" + table[i][j]);
            		num_0 = num_0 + 1;
            	}
            	if(table[i][j] == 1.0) {
            		idx_1 = i;
            	}
            	//System.out.println(num_0 + ", " + idx_1);
            }
            
            if(num_0 == rows - 2 && idx_1 != -1){
            	for(int k = 0; k < cols; k++){
            		if(k != j)
            			table[rows-1][k] = table[rows-1][k] - data[j] * table[idx_1][k];
            	}
            	System.out.println("(" + idx_1 + "," + j + ")=" + table[idx_1][j]);
            	this.print();
            }
            else {
            	System.out.println("data[j] = " + data[j]);
            	table[rows - 1][j] = table[rows - 1][j] + data[j];
            }       
        }
        //table[rows-1][cols-1] = -table[rows-1][cols-1];
	}
}