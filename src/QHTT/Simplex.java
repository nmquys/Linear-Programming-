package QHTT;
import java.util.ArrayList;

public class Simplex {
    private int rows, cols; // row and column
    private float[][] table; // simplex tableau
    private boolean solutionIsUnbounded = false;
    private String variable_list[]; // list of variables;
    private int cur_pivot_row, cur_pivot_column; // for phase 1, cut pivot row and current pivot column
    private String cur_out_variable;

    public static enum ERROR{
        NOT_OPTIMAL,
        IS_OPTIMAL,
        UNBOUNDED,
        NO_SOLUTION
    };
    
	int get_rows() {
		return rows;
	}
    int get_cols() {
        return cols;
    }
    private ERROR cur_error = ERROR.NOT_OPTIMAL; // current error
    public ERROR get_error() {
    	return cur_error;
    }
    
    public Simplex(int numOfConstraints, int numOfUnknowns){
        rows = numOfConstraints+1; // row number + 1 
        cols = numOfUnknowns+1;   // column number + 1
        table = new float[rows][]; // create a 2d array
        variable_list = new String[cols + 1]; // create variable list
        // initialize references to arrays
        for(int i = 0; i < rows; i++){
            table[i] = new float[cols + 1];
        }
    }
    
	public void set_variable_list(String variable_list[]) {
		this.variable_list = variable_list;
	}
    
	public void set_variable_list_phase1(String variable_list[]) {
		this.variable_list[0] = "x0"; // set x0 for phase 1
		for (int i = 0; i < variable_list.length; i++) {
			this.variable_list[i+1] = variable_list[i]; // set x1, x2, ..., w1, w2, ...
		}
	}
	
	public void set_variable_list_phase2() {
		for (int i = 0; i < variable_list.length - 1; i++) {
			this.variable_list[i] = variable_list[i + 1]; // set x1, x2, ..., w1, w2, ..., x0 is not used in phase 2
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
    public void fillTable(double[][] tableData){
        for(int i = 0; i < rows; i++){
			for (int j = 0; j < cols; j++) {
				table[i][j] = (float) tableData[i][j]; // initialize all values to 0
			}
        }
    }
    
    public String getVariableIn() {
		if (cur_pivot_column == -1) {
			return "none"; // if no pivot column is selected
		}
    	return variable_list[cur_pivot_column]; // return the variable in current pivot column
    }

	public String getVariableOut() {
		if (cur_pivot_row == -1) {
			return "none"; // if no pivot row is selected
		}
		return cur_out_variable; // return the variable in current pivot row
	}
    
    public String[][] print_dictionary(){
    	int col_idx[] = new int[rows-1];
		for (int j = 0; j < cols-1; j++) {
			int num_0 = 0;
			int idx_1 = -1;
			for (int i = 0; i < rows - 1; i++) {
				if (Math.abs(table[i][j]) == 0.0f) {
					num_0 = num_0 + 1;
				}
				if (table[i][j] == 1.0) {
					idx_1 = i;
				}
			}
			//System.out.println(j + " " + num_0 + " " + idx_1);
			
			if (num_0 == rows - 2 && idx_1 != -1) {
				col_idx[idx_1] = j;
			} 
			//for(int i = 0;i <rows-1;i++)System.out.print(col_idx[i] + "\t");System.out.println();
		}
		
		String ans[][] = new String[rows][cols+1];
		
		ans[0][0] = "z  = ";
		
		if (table[rows-1][cols-1] >= 0)
			ans[0][1] = String.format(" %.2f",table[rows - 1][cols - 1]);
		else
			ans[0][1] = String.format("%.2f",table[rows - 1][cols - 1]);
		
		
		for(int j = 0; j < cols - 1; j++) {
			if (table[rows-1][j] != 0.0 || table[rows-1][j] != -0.0) {
				if (table[rows-1][j] < 0)
					ans[0][j+2] = (" + " + String.format("%.2f", -table[rows-1][j]) + "*" + variable_list[j]);
				else
					ans[0][j+2] = (" - " + String.format("%.2f",  table[rows-1][j]) + "*" + variable_list[j]);
			}
			else {
				ans[0][j+2] = ("\t");
			}
		}
		for (int i = 0; i < rows - 1; i++) {
			ans[i+1][0] = (variable_list[col_idx[i]] + " = ");
			if (table[i][cols-1] >= 0)
				ans[i+1][1] = String.format(" %.2f",table[i][cols - 1]);
			else
				ans[i+1][1] = String.format("%.2f",table[i][cols - 1]);
			for (int j = 0; j < cols - 1; j++) {
				if(j == col_idx[i]) {
					ans[i+1][j + 2] = "\t";
					continue; // skip the column that is basic variable
				}
				if(table[i+1][j] == -0.0f)
					table[i+1][j] = 0.0f; // convert -0.0 to 0.0
				
				if (Math.abs(table[i][j]) != 0.0) {
					if (table[i][j] < 0)
						ans[i+1][j+2] = (" + " + String.format("%.2f", -table[i][j]) + "*" + variable_list[j]);
					else
						ans[i+1][j+2] = (" - " + String.format("%.2f",  table[i][j]) + "*" + variable_list[j]);
				}
				else {
					ans[i+1][j+2] = ("\t");
				}
			}
		}
		//remove unnecessary tabs
		for (int j = 1; j < cols + 1; j++) {
			int check = 1;
			for (int i = 0; i < rows; i++) {
				if(ans[i][j] != "\t") {
					check = 0;
				}
			}
			if(check == 1) {
				for (int i = 0; i < rows; i++) {
					ans[i][j] = "";
				}
			}
			
		}
		
		/*System.out.println("Simplex Dictionary:");
		for (int j = 0; j < cols+1; j++) {
			System.out.print(ans[0][j] + "\t");
		}
		System.out.println();
		for (int j = 0; j < cols+1; j++) {
			System.out.print("---------------");
		}
		System.out.println();
		for (int i = 1; i < rows; i++) {
			for (int j = 0; j < cols + 1; j++) {
				System.out.print(ans[i][j] + "\t");
			}
			System.out.println();
		}*/
		
		return ans;
    }
    
    // finds the next entering column
    private int findEnteringColumn(int type){
        // type = 1: simplex
    	// type = 2: bland
    	// type = 3: phase 1 (bland)
    	
    	float[] values = new float[cols];
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
    private float[] calculateRatios(int column){
        float[] positiveEntries = new float[rows];
        float[] res = new float[rows];
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
                float val = positiveEntries[i];
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
    	cur_pivot_row = -1; // reset current pivot row
    	if(type == 3)
    		cur_pivot_column = 0; // choose x0 as pivot column in phase 1
		else
			cur_pivot_column = -1; // reset current pivot column
    	
    	int pivotColumn = 0;
        if(type != 3)
        {
        	// step 1
        	if(checkOptimality() == true){
        		// check if and only if xi = x0 if phase1
        		if(type == 3){ 
        			for(int i = 1; i < cols; i++) {
        				if(table[rows-1][i] != 0) {
        					cur_error = ERROR.NO_SOLUTION;
        					return ERROR.NO_SOLUTION;
        				}
        			}
        			if(table[rows-1][0] != -1.0){
        				cur_error = ERROR.NO_SOLUTION;
        				return ERROR.NO_SOLUTION;
        			}
        			cur_error = ERROR.IS_OPTIMAL;
        			return ERROR.IS_OPTIMAL;
        		}
        		cur_error = ERROR.IS_OPTIMAL; // solution is optimal
        		return ERROR.IS_OPTIMAL;
        	}
        
        	// step 2
        	// find the entering column
        	pivotColumn = findEnteringColumn(type);
        	cur_pivot_column = pivotColumn; // save current pivot column
        	//System.out.println("Pivot Column: "+(pivotColumn+1));
        }
        // if type = 3 then we are using phase 1, i.e. we choose x0
        
        // step 3
        // find departing value
        float[] ratios = calculateRatios(pivotColumn);
		/*for (int i = 0; i < ratios.length; i++) {
			System.out.println(ratios[i] + "\t");
		}*/
		//System.out.println(solutionIsUnbounded + " " + type);
        if(type != 3) {
        	if(solutionIsUnbounded == true) {
        		cur_error = ERROR.UNBOUNDED;
        		return ERROR.UNBOUNDED; // return unbounded error
        	}
        }
        else{
			for (int i = 0; i < ratios.length; i++) {
				ratios[i] = table[i][cols - 1] / table[i][pivotColumn];
			}
			solutionIsUnbounded = false; //skip for phase 1
			/*
			for (int i = 0; i < ratios.length; i++) {
			 
				System.out.println(ratios[i] + "\t");
			}
			System.out.println();
			*/
        }
        
        //System.out.println("Ratios: ");
		//for (int i = 0; i < ratios.length; i++)
		//	System.out.print(ratios[i] + " ");
		//System.out.println();
		//System.out.println("OK" + solutionIsUnbounded);
        int pivotRow = findSmallestValue(ratios);
        if(type == 3) {
        	pivotRow = findLargestValue(ratios);
        }
        //System.out.println("Pivot row: "+ (pivotRow+1));
        
        // step 4
        // form the next tableau
        cur_pivot_row = pivotRow; // save current pivot row
        //System.out.println(cur_pivot_row + " " + cur_pivot_column);
        

		if(cur_pivot_row > -1) {
			for (int j = 0; j < cols - 1; j++) {
				if (table[cur_pivot_row][j] == 1.0f) {
					int check = 1;
					for(int i = 0; i < rows-1; i++) {
						if (Math.abs(table[i][j]) != 0.0f && i != cur_pivot_row) {
							check = 0;
							break;
						}
					}
					if(check == 1) {
						cur_out_variable = variable_list[j]; // save current out variable
						break;
					}
				}
			}
		}
        
        formNextTableau(pivotRow, pivotColumn);
        
        // since we formed a new table so return NOT_OPTIMAL
        cur_error = ERROR.NOT_OPTIMAL;
        return ERROR.NOT_OPTIMAL;
    }
    
    
    
    // Forms a new tableau from precomuted values.
    private void formNextTableau(int pivotRow, int pivotColumn){
        float pivotValue = table[pivotRow][pivotColumn];
        float[] pivotRowVals = new float[cols];
        float[] pivotColumnVals = new float[cols];
        float[] rowNew = new float[cols];
        
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
                    float c = pivotColumnVals[i];
                    table[i][j] = table[i][j] - (c * rowNew[j]);
                }
            }
        }
        
        // replace the row
        System.arraycopy(rowNew, 0, table[pivotRow], 0, rowNew.length);
    }
    
    
    // find the smallest positive value in an array
    public int findSmallestValue(float[] data){
        float minimum ;
        int c, location = 0;
        minimum = -1;
        
        for(c = 0; c < data.length-1; c++){
            if(data[c] > 0){
            	if(minimum == -1) {
            		minimum = data[c];
            		location = c;
            	}
            	else if(Float.compare(data[c], minimum) < 0 && data[c] >= 0){
                    minimum = data[c];
                    location  = c;
                }
            }
            //System.out.println(data.length+" minimum ="+minimum);
        }
        
        return location;
    }
    // find the first positive value in an array
	private int findFirstPositiveValue(float[] data) {
		float minimum;
		int c, location = 0;
		minimum = data[0];

		for (c = 0; c < data.length; c++) {
			if (data[c] > 0.0f && Math.abs(data[c]) > 0.0f) {
				location = c;
				return location; // return the first positive value found
			}
		}

		return location;
	}
    
    // find the largest value in an array
    private int findLargestValue(float[] data){
        float maximum = 0;
        int c, location = 0;
        maximum = data[0];
        
        for(c = 1; c < data.length; c++){
            if(Float.compare(data[c], maximum) > 0){
                maximum = data[c];
                location  = c;
            }
        }
        
        return location;
    }
    
    // checks if the table is optimal
    public boolean checkOptimality(){
    	
        for(int i = 0; i < cols-1; i++){
            float val = table[rows-1][i];
            if(val > 0){
                return false; // not optimal
            }
        }
        return true;
    }

    // returns the simplex tableau
    public float[][] getTable() {
        return table;
    }
    
    // fills the simplex tableau with coefficients for phase 1
    public void fillTable_phase1(double[][] phase1Table){
    	this.cols = this.cols + 1;
        for(int i = 0; i < rows-1; i++){
            this.table[i][0] = -1;
			for (int j = 0; j < phase1Table[i].length; j++) {
				this.table[i][j + 1] = (float) phase1Table[i][j]; // initialize all values to 0
			}
        }
        
		for (int i = 1; i < table[0].length; i++) {
			this.table[rows-1][i] = 0;
		}
		this.table[rows - 1][0] = -1;	
    }
    
    // build table for phase 2
	public void fillTable_phase2(double[] phase1Table){
		//fill all 0 last row
		for(int i = 0; i < rows-1; i++){
            for(int j = 0; j < phase1Table.length; j++) {
            	table[i][j] = table[i][j+1];
            }
        }
		
		cols = cols - 1;

		
		for(int j = 0; j < phase1Table.length; j++) {
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
            			table[rows-1][k] = (float) (table[rows-1][k] - phase1Table[j] * table[idx_1][k]);
            	}
            	//System.out.println("(" + idx_1 + "," + j + ")=" + table[idx_1][j]);
            	//this.print();
            }
            else {
            	//System.out.println("data[j] = " + data[j]);
            	table[rows - 1][j] = (float) (table[rows - 1][j] + phase1Table[j]);
            }       
        }
        
        set_variable_list_phase2(); // set variable list for phase 2
        //table[rows-1][cols-1] = -table[rows-1][cols-1];
	}
	
	public String find_all_solutions(int ROW) {
		String solutions = new String();
		int col_idx[] = new int[rows-1];
		for (int j = 0; j < cols-1; j++) {
			int num_0 = 0;
			int idx_1 = -1;
			for (int i = 0; i < rows - 1; i++) {
				if (Math.abs(table[i][j]) == 0.0f) {
					num_0 = num_0 + 1;
				}
				if (table[i][j] == 1.0) {
					idx_1 = i;
				}
			}
			//System.out.println(j + " " + num_0 + " " + idx_1);
			
			if (num_0 == rows - 2 && idx_1 != -1) {
				col_idx[idx_1] = j;
			} 
			//for(int i = 0;i <rows-1;i++)System.out.print(col_idx[i] + "\t");System.out.println();
		}
		//solutions = "";
		//System.out.println("Solutions: " + solutions);
		if (table[ROW][cols - 1] > 0.0f) {
			solutions = String.format(" %.2f", table[ROW][cols - 1]);
		} else if (table[ROW][cols - 1] < -0.0f) {
			solutions = String.format("%.2f", table[ROW][cols - 1]);
		} else {
			solutions = " 0.00";
		}
		
		for (int j = 0; j < cols - 1; j++) {
			int check = 1;
			for(int i = 0; i < rows-1; i++) {
				if (col_idx[i] == j) {
					check = 0; break;
				}
			}
			if(check == 0) {
				continue; // skip the column that is basic variable
			}
			if (table[ROW][j] > -0.0000001f && table[ROW][j] < 0.0000001f) {
				table[ROW][j] = 0.0f; // convert -0.0 to 0.0
			}
			if (table[rows - 1][j] > -0.0000001f && table[rows - 1][j] < 0.0000001f) {
				table[rows - 1][j] = 0.0f; // convert -0.0 to 0.0
			}
			//System.out.println(ROW + ", " + j + ", table[ROW][j] = " + table[ROW][j]);
			if(Math.abs(table[rows-1][j]) > 0.0f) {
				//System.out.println("col " + j + " is not 0, it's equal to " + table[rows-1][j]);
				continue; // skip the column that is 0
			}
			
			String tmp = "";
			if (table[ROW][j] < 0.0f) {
				tmp = " + " + String.format("%.2f", -table[ROW][j]) + "*" + variable_list[j];
			} else {
				tmp = " - " + String.format("%.2f", table[ROW][j]) + "*" + variable_list[j];
			}
			solutions = solutions + tmp;
			
		}
		return solutions;
	}
	
	public ArrayList<Integer> get_basis(){
		ArrayList<Integer> basis = new ArrayList<>();
		int col_idx[] = new int[rows-1];
		for (int j = 0; j < cols-1; j++) {
			int num_0 = 0;
			int idx_1 = -1;
			for (int i = 0; i < rows - 1; i++) {
				if (Math.abs(table[i][j]) == 0.0f) {
					num_0 = num_0 + 1;
				}
				if (table[i][j] == 1.0) {
					idx_1 = i;
				}
			}
			//System.out.println(j + " " + num_0 + " " + idx_1);
			
			if (num_0 == rows - 2 && idx_1 != -1) {
				col_idx[idx_1] = j;
			} 
			//for(int i = 0;i <rows-1;i++)System.out.print(col_idx[i] + "\t");System.out.println();
		}
		for (int i = 0; i < rows - 1; i++) {
			basis.add(col_idx[i]); // add the index of basic variable to the basis
		}
		return basis;
	}
}