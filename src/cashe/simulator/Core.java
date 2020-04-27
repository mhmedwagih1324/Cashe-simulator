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
public class Core implements Runnable{
    boolean[] inst_opcode ;         //holds either it's a read(0) or write (1)
    int[] inst_operand ;            //holds address of a block to be read from or write in
    int[] inst_val ;                //used in case of writing, contains values to be written
    Cashe c ;
    Cashe[] other ;
    int core_num ;
    
    @Override
    public synchronized void run() {
        for(int i = 0 ; i < 4 ; ++i){
            decode_inst(i);
        }
    }
    //constructor :)))) sh
    public Core(int core_index, Cashe all [],
                boolean[] commands, int[] addresses, int[] values){
        core_num = core_index ;
        other = all ;
        c = all[core_index] ;
        inst_opcode = commands ;
        inst_operand = addresses ;
        inst_val = values ;
    }
    public void decode_inst(int inst_numb){
        System.out.println("Thread " + core_num + " executing instruction " + inst_numb);
        if(inst_opcode[inst_numb] == false)
            read(inst_numb);
        else
            write(inst_numb);
    }
    public void write(int inst_numb){   //optimized :)
        int x = inst_operand[inst_numb] ;   //address
        boolean f = false ;
        CasheSimulator.home_dir[x][1] = 1 ;   //dirty bit set as we are writing
        CasheSimulator.home_dir[x][core_num+2] = 1 ; //core bit set as we are writing
        //checking if it's in this cashe
        System.out.println("\t"+core_num+"- We need to write " + inst_val[inst_numb] + " into block " + x);
        System.out.println("\t"+core_num+"-We check this Cashe for the block");
        for(int i = 0 ; i < 4 ; ++i){   //loops on blocks of this cashe
            if(c.address[i] == x){//it's found in the cashe 
                System.out.println("\t"+core_num+"- We found the block in the cashe");
                if(c.state[i] == 'm'){ //modified
                    //state still modified
                    c.val[i] = inst_val[inst_numb] ;    //write val in the cashe
                    System.out.println("\t"+core_num+"-it's found modified, we just change its value and update Home directory");
                }
                else if(c.state[i] == 'i' || c.state[i] == 's'){//invalid or shared
                    c.state[i] = 'm';   //modified
                    c.val[i] = inst_val[inst_numb] ;    //write val in the cashe
                    invalidate_and_execlude(x);
                    if(c.state[i] == 'i')
                        System.out.println("\t"+core_num+"it's found invalid, we change its value and invalidate the block in other cashes and update Home directory");
                    else
                        System.out.println("\t"+core_num+"it's found shared, we change its value and invakidate the block in other cashes and update Home directory");
                }
                f = true ;  //we found the element to write on in the cashe
                break;
            }
        }
        if(f == false){ //not found in the cashe
            //print write miss
            System.out.println("\t"+core_num+"-Write miss");
            System.out.println("\t"+core_num+"-it's not found in this cashe");
            //update my cashe and write in it the value
            CasheSimulator.home_dir[x][core_num+2] = 1 ;   //core bit set
            //call a function to get an index to write
            int room = get_room();
            //write the val in the index of the cashe
            c.state[room] = 'm' ;   //we are writing so its state is modified
            c.address[room] = x ;   //the address of the block
            c.val[room] = inst_val[inst_numb] ; //the value written to the block
            invalidate_and_execlude(x);
            //add some info here
            System.out.println("\t"+core_num+"-We find a block in the cashe to write the block on it");
            System.out.println("\t"+core_num+"-We invalidate the block in other cashes and home directory");
        }
    }
    public void read(int inst_numb){        //optimized :)
        int x = inst_operand[inst_numb] ;   //address
        boolean f = false ;
        //checking my cashe
        System.out.println("\t"+core_num+"-We need to read the block " + x + " from memory");
        System.out.println("\t"+core_num+"-We check this Cashe for the block");
        for(int i = 0 ; i < 4 ; ++i){       //loops on blocks of this cashe
            if(c.address[i] == x){//it's found in the cashe
                System.out.println("\t"+core_num+"-We found the block in the Cashe");
                if(c.state[i] == 's' || c.state[i] == 'm'){
                    System.out.println("\t"+core_num+"-it's found Shared or modified so its value is good to use");
                    System.out.println("\t"+core_num+"-We pick the value and continue");
                }
                else{       //invalid
                    //print it's invalid
                    System.out.println("\t"+core_num+"-it's found in the cashe invalid");
                    //check other cashes for the value, if found update the cashe and return true
                    System.out.println("\t"+core_num+"-We check other Cashes for the value");
                    boolean found_in_home = check_other_Cashes(x, i) ;
                    //if not found get from memory and update the cashe
                    if(!found_in_home){ //didn't find in home_dir
                        //we get it from memory and put it in the cashe
                        System.out.println("\t"+core_num+"-We didn't find the block in the home directory");
                        c.val[i] = CasheSimulator.memory[x] ;    //replace the val in this cashe
                        c.state[i] = 's' ;  //check this plz    //change state
                        CasheSimulator.home_dir[x][core_num+2] = 1; //core bit set
                        System.out.println("\t"+core_num+"-We get the block from the memory and update the home directory");
                    }
                }
                f = true ;
                break;
            }
        }
        if(!f){ //not found in the cashe    //check other cashes then memory
            //print read miss
            System.out.println("\t"+core_num+"-read miss");
            System.out.println("\t"+core_num+"-We didn't find the block in the Cashe");
            //call a function to get an index to write
            int index = get_room();
            System.out.println("\t"+core_num+"-We find a block in the cashe to swap it with the demanded block");
            //check other cashes for the value, if found update the cashe and return true
            boolean found_in_home = check_other_Cashes(x, index) ;
            //if not found get from memory and update the cashe
            if(!found_in_home){ //didn't find in home_dir
                //we get it from memory and put it in the cashe
                c.val[index] = CasheSimulator.memory[x] ;    //replace the val in this cashe
                c.state[index] = 's' ;  //check this plz    //change state
                CasheSimulator.home_dir[x][core_num+2] = 1; //core bit set
                System.out.println("\t"+core_num+"-We didn't find the block in the home directory");
                System.out.println("\t"+core_num+"-We get the block from the memory and update the home directory");
            }
        }
    }
    
    
    //checks other cashes if they have a good value of the block and updates current cashe if so
    boolean check_other_Cashes(int x, int index){   //x is the address of the block to be read
        //index is the block replaced in the cashe
        for(int j = 0 ; j < 4 ; ++j){   //loop on cores
            if(CasheSimulator.home_dir[x][j+2] == 1){   //core j has the value demanded
                for(int k = 0 ; k < 4 ; ++k){           //loop on blocks of core j
                    if(other[j].address[k] == x){       // found the val
                        c.val[index] = other[j].val[k] ;    //replace the val in this cashe
                        c.state[index] = 's' ;              //state changed
                        other[j].state[k] = 's' ;       //state of the other cashe block becomes shared
                        CasheSimulator.home_dir[x][core_num+2] = 1; //core bit set
                        System.out.println("value replaced from the other core");
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    
    //invalidates the block with the address in other cashes and home dir
    void invalidate_and_execlude(int x){
        //invalidate other cashes that have the block
        for(int j = 0 ; j < 4 ; ++j){       //loop on cashes and home_dir
            if(j != core_num){      //execlude my cashe from the invalidation process
                if(CasheSimulator.home_dir[x][j+2] == 1){     //the cashe has the block
                    CasheSimulator.home_dir[x][j+2] = 0 ;   //core bit reset
                    for(int k = 0 ; k < 4 ; ++k){   //loop on blocks in cashe j
                        if(other[j].address[k] == x){
                            other[j].state[k] = 'i' ;   // invalidate this element in the cashe
                            break;
                        }
                    }
                }
            }
        }
    }
    
    ///gets a room for the block in the cashe
    int get_room(){
        int r ;     //the index of the room
        int sh_idx = -1 ;    //index of shared block in the cashe
        for(int i = 0 ; i < 4 ; ++i){   //loop on blocks of this cashe
            if(c.state[i] == 'i'){ //found an invalid block
                return i;       //it's the best place to put the value
            }
            else if(c.state[i] == 's'){ //found a shared block
                sh_idx = i ;    //keep this index to use if we didn't find any invalid block
            }
        }
        //if we reached here this means that there is no invalid block in the cashe
        //We check if there were any shared blocks as a second option
        if(sh_idx != -1){   //we found a shared block in the cashe
            //here we need to invalid its bit in the home_dir
            CasheSimulator.home_dir[c.address[sh_idx]][core_num+2] = 0 ;
            //if the dirty bit was 0 this means that it's not modified and we don't need a write back
            if(CasheSimulator.home_dir[c.address[sh_idx]][1] == 0)
                return sh_idx;
            //if it was dirty we check if there were more than one cashe that has the block
            int cnt = 0, mdx = -1 ;     //mdx is the cashe number that has the modified
            for(int j = 0 ; j < 4 ; ++j){   //loop on cores of the home_dir to count cashes that has the block
                if (CasheSimulator.home_dir[c.address[sh_idx]][j+2] == 1){  //cashe j has the block
                    ++cnt;
                    mdx = j;    //it will be used if it was changed only once
                }
            }
            //if there is at least 2 cashes that has the block, then they are still shared
            if(cnt >= 2)
                ;//do nothing
            //else if it's only one we just make its state as modified
            else{
                //we get the index of the block in the cashe to set its state as modified
                for(int i = 0 ; i < 4 ; ++i){   //loop on blocks of the other cashe
                    if(other[mdx].address[i] == c.address[sh_idx]){ //we found the same address
                        other[mdx].state[i] = 'm' ; //we change its state to modified
                        break;
                    }
                }
            }
            //then return its index in the cashe
            return sh_idx;
            //just think here 
        }
        else{   //this case means that all blocks in the cashe are in the modified state
            Random rand = new Random();
            r = Math.abs(rand.nextInt())%4 ;    //pick a random block index
            //we know it's a modified block
            //we need to write back its value in the memory
            CasheSimulator.memory[c.address[r]] = c.val[r] ;
            //clear its bit in home_dir and the dirty bit also
            CasheSimulator.home_dir[c.address[r]][1] = 0 ;//dirty reset
            CasheSimulator.home_dir[c.address[r]][core_num+2] = 0 ;//core reset
            //return the index
            return r;
        }
    }
}
