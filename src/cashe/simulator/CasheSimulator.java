/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cashe.simulator;

import java.util.Random;

/**
 *
 * @author Mohamed Wagih
 */

public class CasheSimulator {
    public static int[][] home_dir = new int[16][6] ;
    // Col0 -> address
    // Col1 -> dirty
    // Col2,3,4,5 core availability
    public static Cashe[] all_cashes = new Cashe[4];
    public static int[] memory = new int [16];  //memory
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        //initialize cashes
        for(int j = 0 ;j < 4 ; ++j){    //loops on cashes
            all_cashes[j] = new Cashe();
            for(int i = 0 ; i < 4 ; ++i){   //loops on blocks of a cashe
                all_cashes[j].address[i] = j*4+i;
                all_cashes[j].state[i] = 'i' ;
                all_cashes[j].val[i] = -1;
            }
        }
        //initializing home directory
        for(int i = 0 ; i < 16 ; ++i){
            home_dir[i][0] = i ;
            home_dir[i][1] = 0 ;
            home_dir[i][2] = 0 ;
            home_dir[i][3] = 0 ;
            home_dir[i][4] = 0 ;
            home_dir[i][5] = 0 ;
        }
        
        //initialize memory
        for(int i = 0 ; i < 16 ; ++i){
            memory[i] = i ;  //addresses of elements in the memory
            Random rand = new Random();
            memory[i] = Math.abs(rand.nextInt())%100 ;   //value of each element in the memory
        }
        
        //initialize instructions
        //commands
        boolean[] commands0 = {false, false, true, false};
        boolean[] commands1 = {false, false, true, false};
        boolean[] commands2 = {false, false, true, false};
        boolean[] commands3 = {false, false, true, false};
        //addresses
        int[] addresses0 = {1, 2, 3, 4};
        int[] addresses1 = {1, 2, 3, 4};
        int[] addresses2 = {1, 2, 3, 4};
        int[] addresses3 = {1, 2, 3, 4};
        //values
        int[] values0 = {0, 0, 93, 0};
        int[] values1 = {0, 0, 93, 0};
        int[] values2 = {0, 0, 93, 0};
        int[] values3 = {0, 0, 93, 0};
        
        Core c0 = new Core(0, all_cashes, commands0, addresses0, values0) ;
        Core c1 = new Core(1, all_cashes, commands1, addresses1, values1) ;
        Core c2 = new Core(2, all_cashes, commands2, addresses2, values2) ;
        Core c3 = new Core(3, all_cashes, commands3, addresses3, values3) ;
        Thread t0 = new Thread(c0) ;
        Thread t1 = new Thread(c1) ;
        Thread t2 = new Thread(c2) ;
        Thread t3 = new Thread(c3) ;
        t0.start();
        t1.start();
        t2.start();
        t3.start();
    }
    
}
