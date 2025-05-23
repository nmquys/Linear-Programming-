package QHTT;

public class Simplex_test {

    public static void main(String[] args) {
        
        boolean quit = false;
        
        // Example problem:
        //  maximize 3x + 5y 
        //=-minimize -3x - 5y
        
        // subject to   x +  y <= 4
        //        and   x + 3y <= 6
        
        // 		z =   - 3x - 5y 
        // => 	0 = z + 3x + 5y
        
        // {1*x1 + 1*x2 + 1w1 + 0w2 = 4},   b1
        // {1*x1 + 3*x2 + 0w1 + 1w2 = 6},	b2
        // {3x1  + 5x2  + 0w1 + 0w2 = 0},	bz      
        
        // <=> { 
        float[][] standardized =  {
                { 1,   1,    1,  0,   4},
                { 1,   3,    0,  1,   6},
                {3,  5,    0,  0,   0}
        };
 /*       
        // row and column do not include
        // right hand side values
        // and objective row
        Simplex simplex = new Simplex(2, 4);
        
        simplex.fillTable(standardized);

        // print it out
        System.out.println("---Starting set---");
        simplex.print();
        
        // if table is not optimal re-iterate
        int cnt = 1;
        while(!quit){
            Simplex.ERROR err = simplex.compute(2);
            if(err == Simplex.ERROR.NOT_OPTIMAL)
            {
            	System.out.println("Iteration " + cnt + ":");
            	simplex.print();
            }
            cnt = cnt + 1;
            if(cnt == 10)
            {
            	System.out.println("Hetcuu");break;
            }
            	
            if(err == Simplex.ERROR.IS_OPTIMAL){
                System.out.println("---Solution is optimal---");
            	simplex.print();
                quit = true;
            }
            else if(err == Simplex.ERROR.UNBOUNDED){
                System.out.println("---Solution is unbounded---");
                quit = true;
            }
        }
        */
 //_______________________________________________________________________      
        // test 2 - phase
        // min  x1  +  3x2  +   x3
        // subject to
        // 	   2x1  + -5x2  +   x3  +  1w1  +  0w2  = -5
        // 	   2x1  + -1x2  +  2x3  +  0w1  +  1w2  =  4
        //float[][] standardized2 =  {
        //        { 2,  -5,   1,   1,   0,   -5},
        //        { 2,  -1,   2,   0,   1,    4},
        //        {-1,  -3,  -1,   0,   0,    0}
        //};
        
        double[][] standardized2 = {
        		{ -1, -1, 1, 0, 0, -3},
        		{ -1,  1, 0, 1, 0, -1},
        		{ -1,  2, 0, 0, 1,  2},
        		{ 1, 3, 0, 0, 0,  0} 
        };
        
        Simplex phase1 = new Simplex(3, 6);// length + x0
        phase1.fillTable_phase1(standardized2);
        // print it out
        System.out.println("---Starting phase 1---");
        phase1.print();
        phase1.compute(3);
        System.out.println("Iteration 1: ");
        phase1.print();
     // if table is not optimal re-iterate
        int cnt = 2;
        quit = false;
        while(!quit){
            Simplex.ERROR err = phase1.compute(1);
            if(err == Simplex.ERROR.NOT_OPTIMAL)
            {
            	System.out.println("Iteration " + cnt + ":");
            	phase1.print();
            }
            cnt = cnt + 1;
            if(cnt == 10)
            {
            	System.out.println("Hetcuu");break;
            }
            	
            if(err == Simplex.ERROR.IS_OPTIMAL){
                System.out.println("---Solution is optimal---");
            	phase1.print();
                quit = true;
            }
            else if(err == Simplex.ERROR.UNBOUNDED){
                System.out.println("---Solution is unbounded---");
                quit = true;
            }
            else if(err == Simplex.ERROR.NO_SOLUTION) {
            	System.out.println("---No solution");
            	quit = true;
            }
        }
        phase1.fillTable_phase2(standardized2[standardized2.length - 1]);
        System.out.println("Begin phase 2");
        phase1.print();
        quit = false;
        while(!quit){
            Simplex.ERROR err = phase1.compute(1);
            if(err == Simplex.ERROR.NOT_OPTIMAL)
            {
            	System.out.println("Iteration " + cnt + ":");
            	phase1.print();
            }
            cnt = cnt + 1;
            if(cnt == 10)
            {
            	System.out.println("Hetcuu");break;
            }
            	
            if(err == Simplex.ERROR.IS_OPTIMAL){
                System.out.println("---Solution is optimal---");
            	phase1.print();
                quit = true;
            }
            else if(err == Simplex.ERROR.UNBOUNDED){
                System.out.println("---Solution is unbounded---");
                quit = true;
            }
            
        }
    } 
}