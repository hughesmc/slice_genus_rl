#!/usr/bin/env python
# coding: utf-8

# In[1]:


import numpy as np


# In[2]:


class BandEnv:
    
    def __init__(self,band_decomposition=[],braid_index=12,max_num_bands=20):
        self.band_decomposition=list(band_decomposition[:max_num_bands])
        self.fix_list()
        self.braid_index=braid_index
        self.max_num_bands=max_num_bands
        self.max_num_actions=(self.max_num_bands-1)+(self.max_num_bands-1)+2*(self.max_num_bands+1)*(self.braid_index-1)+(self.max_num_bands-1)
        self.original_length=len(self.band_decomposition)
        self.matrix_size=self.braid_index*(self.braid_index-1)//2
        self.matrices={}
        for jjj in range(-self.braid_index+1,self.braid_index):
            sgn=np.sign(jjj)
            if sgn==-1:
                self.matrices[jjj]=np.linalg.inv(self.LKrep(self.braid_index,np.abs(jjj)))
            elif sgn==1:
                self.matrices[jjj]=self.LKrep(self.braid_index,np.abs(jjj))
            elif sgn==0:
                self.matrices[jjj]=np.identity(self.matrix_size)
        self.mat_decomposition=self.bands_to_mat()
        self.output_size=self.max_num_bands*self.matrix_size*self.matrix_size
        
    def fix_list(self):
        for jjj in range(len(self.band_decomposition)):
            if not isinstance(self.band_decomposition[jjj],list):
                self.band_decomposition[jjj]=[self.band_decomposition[jjj]]
            self.band_decomposition[jjj]=[int(mmm) for mmm in self.band_decomposition[jjj]]
        return
        
    def index(self,n,i,j):
        return int((i-1)*(n-i/2)+j-i-1)
    
    def LKrep(self,n,k):
        M=np.zeros((n*(n-1)//2,n*(n-1)//2))
        q=np.sqrt(2)
        t=np.pi
        for iii in range(1,n):
            for jjj in range(iii+1,n+1):
                if (k<iii-1)or(jjj<k):
                    M[self.index(n,iii,jjj),self.index(n,iii,jjj)]=1
                elif k==iii-1:
                    M[self.index(n,iii-1,jjj),self.index(n,iii,jjj)]= 1
                    M[self.index(n,iii,jjj),self.index(n,iii,jjj)] = 1-q
                elif (k==iii) and (k<jjj-1):
                    M[self.index(n,iii,iii+1),self.index(n,iii,jjj)] = t*q*(q - 1)
                    M[self.index(n,iii+1,jjj),self.index(n,iii,jjj)] = q
                elif (k==iii) and (k ==jjj-1):
                    M[self.index(n,iii,jjj),self.index(n,iii,jjj)] = t*q*q
                elif (iii<k) and (k<jjj - 1):
                    M[self.index(n,iii,jjj),self.index(n,iii,jjj)] = 1
                    M[self.index(n,k,k+1),self.index(n,iii,jjj)] = t*q**(k - iii)*(q - 1)**2
                elif (k==jjj-1):
                    M[self.index(n,iii,jjj-1),self.index(n,iii,jjj)] = 1
                    M[self.index(n,jjj-1,jjj),self.index(n,iii,jjj)] = t*q**(jjj-iii)*(q - 1)
                elif (k==jjj):
                    M[self.index(n,iii,jjj),self.index(n,iii,jjj)]=1-q
                    M[self.index(n,iii,jjj+1),self.index(n,iii,jjj)]=q    
        return M
      
      
      
    def reduce_list(self,lis):
      jjj=0
      while jjj < len(lis)-1:
        if lis[jjj]==-lis[jjj+1]:
          del lis[jjj+1]
          del lis[jjj]
          if jjj>0:
            jjj-=1
        else:
          jjj+=1
      return lis
    
   
      
    def simplify(self):
      for iii in range(len(self.band_decomposition)):
        self.reduce_list(self.band_decomposition[iii])
      return self.band_decomposition

    
    def bands_to_mat(self):
        """ Converts the current band decomposition to a list of matrices using the Lawrence-Krammer representaton. """
        self.mat_decomposition=self.band_decomposition.copy()
        for jjj in range(len(self.band_decomposition)):
            self.mat=np.identity(self.matrix_size)
            for iii in self.mat_decomposition[jjj]:
                self.mat=self.mat@self.matrices[iii]
            self.mat_decomposition[jjj]=self.mat
        return self.mat_decomposition
    
    def invert_band(self,band):
        """ Inverts the given band. """
        return [-band[-iii-1] for iii in range(len(band))]

    def left_slide(self,position):
        """ Performs a left band slide at the band located at a valid position. """
        if position<=0 or position>=len(self.band_decomposition):
            return
        old_left=self.band_decomposition[position-1].copy()
        old_right=self.band_decomposition[position].copy()
        new_right=old_left.copy()
        new_left=old_left.copy()+old_right.copy()+self.invert_band(old_left.copy())
        self.band_decomposition[position-1]=new_left
        self.band_decomposition[position]=new_right
        return
        
    def right_slide(self,position):
        """ Performs a right band slide at the band located at a valid position. """
        if position<0 or position>=len(self.band_decomposition)-1:
            return
        old_left=self.band_decomposition[position].copy()
        old_right=self.band_decomposition[position+1].copy()
        new_left=old_right.copy()
        new_right=self.invert_band(old_right.copy())+old_left.copy()+old_right.copy()
        self.band_decomposition[position]=new_left
        self.band_decomposition[position+1]=new_right
        return  
    
    def create_bands(self,position,index):
        """ Creates a pair of cancelling bands of the form [index],[-index] at the specified position.
        
        If the length of the current band decomposition is greater than (self.max_num_bands-2) then nothing changes.
        
        """
        if len(self.band_decomposition)>self.max_num_bands-2:
            return
        if index<=-self.braid_index or index>=self.braid_index or index==0:
            return
        if position<0 or position>len(self.band_decomposition):
            return
        self.band_decomposition.insert(position,[-index])
        self.band_decomposition.insert(position,[index])
        return
    
    def cancel_bands(self,position):
        """ Cancels a pair of bands at locations (position) and (position+1) when those bands can be cancelled.
        
        When they cannot be cancelled the band decomposition remains unchanged.
        
        """
        if position<0 or position>len(self.band_decomposition)-2:
            return
        self.bands_to_mat()
        if np.allclose(self.mat_decomposition[position]@self.mat_decomposition[position+1],np.identity(self.matrix_size)):
            del self.band_decomposition[position]
            del self.band_decomposition[position]
        return
    
    def score(self):
        """ Returns the score associated to the current state.  """
        return self.original_length-len(self.band_decomposition)
    
    def get_del_list(self):
        """ Returns a list of positions at which the following two bands can be cancelled.  """
        self.bands_to_mat()
        self.del_list=[]
        for iii in range(len(self.band_decomposition)-1):
            if np.allclose(self.mat_decomposition[iii]@self.mat_decomposition[iii+1],np.identity(self.matrix_size)):
                self.del_list.append(iii)
        return self.del_list
    
    def get_action_count(self):
        """ Returns the number of valid actions, i.e. actions that will change the band decomposition.  """
        self.get_del_list()
        self.bd=len(self.band_decomposition)
        if len(self.band_decomposition)>=self.max_num_bands-1:
            self.action_count=(self.bd-1)+(self.bd-1)+len(self.del_list)
        else:
            self.action_count=(self.bd-1)+(self.bd-1)+2*(self.bd+1)*(self.braid_index-1)+len(self.del_list)
        return self.action_count
    
    
    def action_short(self,number):
        """ Perform an action using the short action list numbering.
        
        The value of number can be any value between 0 and (self.get_action_count()-1) inclusive.
        
        The actions corresponding to these numbers in this range will change as the band decomposition changes.
        
        """
        self.bd=len(self.band_decomposition)
        if number<0 or number>self.get_action_count()-1:
            #print("Action number ",number," outside range of valid actions.")
            return
        if self.bd>=self.max_num_bands-1:
            if number//(self.bd-1)==0:
                self.left_slide(1+number%(self.bd-1))
            elif number//(self.bd-1)==1:
                self.right_slide(number%(self.bd-1))
            elif number//(self.bd-1)==2:
                self.get_del_list()
                self.cancel_bands(self.del_list[(number%(self.bd-1))]) 
        else:
            if number//(self.bd-1)==0:
                self.left_slide(1+number%(self.bd-1))
            elif number//(self.bd-1)==1:
                self.right_slide(number%(self.bd-1))
            elif (number-2*(self.bd-1))//(2*(self.bd+1)*(self.braid_index-1))==0:
                loc=(number-2*(self.bd-1))//(2*self.braid_index-2)
                ind=(number-2*(self.bd-1))%(2*self.braid_index-2)-(self.braid_index-2)
                if ind<=0:
                    ind-=1
                self.create_bands(loc,ind)
            elif (number-2*(self.bd-1))//(2*(self.bd+1)*(self.braid_index-1))==1:
                self.get_del_list()
                self.cancel_bands(self.del_list[(number-2*(self.bd-1))%(2*(self.bd+1)*(self.braid_index-1))])
        self.simplify()
        return self.bd-len(self.band_decomposition),self.get_state()
    
    
    def action_full(self,number):
        """ Perform an action using the full action list numbering.
        
        The value of number can be any value between 0 and (self.max_num_actions-1) inclusive.
        
        The actions corresponding to these numbers in this range will not change as the band decomposition changes.
        
        """
        
        self.mnb=self.max_num_bands
        self.bd=len(self.band_decomposition)
        if number<0 or number>self.max_num_actions-1:
            #print("Action number ",number," outside range of valid actions.")
            return
        if number//(self.mnb-1)==0:
            self.left_slide(1+(number%(self.mnb-1)))
        elif number//(self.mnb-1)==1:
            self.right_slide(number%(self.mnb-1))
        elif (number-2*(self.mnb-1))//(2*(self.mnb+1)*(self.braid_index-1))==0:
            loc=(number-2*(self.mnb-1))//(2*self.braid_index-2)
            ind=(number-2*(self.mnb-1))%(2*self.braid_index-2)-(self.braid_index-2)
            if ind<=0:
                ind-=1
            self.create_bands(loc,ind)
        elif (number-2*(self.mnb-1))//(2*(self.mnb+1)*(self.braid_index-1))==1:
            self.cancel_bands((number-2*(self.mnb-1))%(2*(self.mnb+1)*(self.braid_index-1)))
        self.simplify()
        return self.bd-len(self.band_decomposition),self.get_state()
    
    
    def action_type_short(self,number):
        """ Prints the short action type corresponding to the value number. """
        self.bd=len(self.band_decomposition)
        if number>=self.get_action_count() or number<0:
            print("action_type_short error: Not a valid action type")
            return
        if self.bd>=self.max_num_bands-1:
            if number//(self.bd-1)==0:
                print("left slide at ",1+number)
            elif number//(self.bd-1)==1:
                print("right slide at ",number%(self.bd-1))
            else:
                self.get_del_list()
                print("cancel pair of bands at",self.del_list[(number%(self.bd-1))]) 
            return
        else:
            if number//(self.bd-1)==0:
                print("left slide at ",1+number%(self.bd-1))
            elif number//(self.bd-1)==1:
                print("right slide at ",number%(self.bd-1))
            elif (number-2*(self.bd-1))//(2*(self.bd+1)*(self.braid_index-1))==0:
                loc=(number-2*(self.bd-1))//(2*self.braid_index-2)
                ind=(number-2*(self.bd-1))%(2*self.braid_index-2)-(self.braid_index-2)
                if ind<=0:
                    ind-=1
                print("create pair of ",ind," bands at ",loc)
            elif (number-2*(self.bd-1))//(2*(self.bd+1)*(self.braid_index-1))==1:
                self.get_del_list()
                print("cancel pair of bands at ",self.del_list[(number-2*(self.bd-1))%(2*(self.bd+1)*(self.braid_index-1))])
        return 
        

    def action_type_full(self,number):
        """ Prints the full action type corresponding to the value number. """
        self.mnb=self.max_num_bands
        self.bd=len(self.band_decomposition)
        if number>=self.max_num_actions or number<0:
            print("action_type_full error: Not a valid action type")
            return
        if number//(self.mnb-1)==0:
            print("left slide at ",1+(number%(self.mnb-1)))
        elif number//(self.mnb-1)==1:
            print("right slide at ",number%(self.mnb-1))
        elif (number-2*(self.mnb-1))//(2*(self.mnb+1)*(self.braid_index-1))==0:
            loc=(number-2*(self.mnb-1))//(2*self.braid_index-2)
            ind=(number-2*(self.mnb-1))%(2*self.braid_index-2)-(self.braid_index-2)
            if ind<=0:
                ind-=1
            print("create pair of ",ind," bands at ",loc)
        elif (number-2*(self.mnb-1))//(2*(self.mnb+1)*(self.braid_index-1))==1:
            self.get_del_list()
            print("cancel pair of bands at ",(number-2*(self.mnb-1))%(2*(self.mnb+1)*(self.braid_index-1)))
        return     
            
        
    def get_state(self):
        """ Returns a vector of length self.output_size which represents the current band decomposition.  """
        c=np.array(self.bands_to_mat())
        ca=np.reshape(c,len(self.band_decomposition)*self.matrix_size*self.matrix_size)
        da=np.pad(ca,pad_width=(0,self.output_size-len(ca)),mode='constant', constant_values=0)
        return da

        
    def map_action(self,number):
        """ Maps number, thought of as a short action value, to the corresponding full action value. """
        self.bd=len(self.band_decomposition)
        if number<0 or number>self.get_action_count()-1:
            print("Action number ",number," outside range of valid actions.")
            return
        if self.bd>=self.max_num_bands-1:
            if number//(self.bd-1)==0:
                return number
            elif number//(self.bd-1)==1:
                return number-(self.bd-1)+(self.max_num_bands-1)
            elif (number-2*(self.bd-1))//(2*(self.bd+1)*(self.braid_index-1))==0:
                self.get_del_list()
                return self.del_list[(number%(self.bd-1))]+2*(self.max_num_bands-1)+(2*(self.max_num_bands+1)*(self.braid_index-1))
        else:
            if number//(self.bd-1)==0:
                return number
            elif number//(self.bd-1)==1:
                return number-(self.bd-1)+(self.max_num_bands-1)
            elif (number-2*(self.bd-1))//(2*(self.bd+1)*(self.braid_index-1))==0:
                return (number-2*(self.bd-1))+2*(self.max_num_bands-1)
            elif (number-2*(self.bd-1))//(2*(self.bd+1)*(self.braid_index-1))==1:
                self.get_del_list()
                return self.del_list[(number-2*(self.bd-1))-(2*(self.bd+1)*(self.braid_index-1))]+2*(self.max_num_bands-1)+(2*(self.max_num_bands+1)*(self.braid_index-1))
        return
    
    def valid_actions(self):
        """ Returns the list of full action values which change the band decomposition. """
        act_num=self.get_action_count()
        return [self.map_action(jjj) for jjj in range(act_num)]
    
    def full_braid_matrix(self):
        """ Returns a matrix that represents the full braid.  
        
        It doesn't contain any info about the band decomposition, and the matrix returned shouldn't change when actions are taken.
        
        """
        self.bands_to_mat()
        mat=np.identity(self.matrix_size)
        for jjj in range(len(self.band_decomposition)):
            mat=mat@self.mat_decomposition[jjj]
        return mat
        
    
    def info(self):
        """ Prints some information about the current state.  """
        print("band decomposition = ",self.band_decomposition,'\n')
        print("score = ",self.original_length-len(self.band_decomposition),'\n')
        print("action count = ",self.get_action_count(),'\n')
        print("valid actions = ",self.valid_actions(),'\n')
        return
    
    def algebraic_length(self):
        alg_len=0
        for iii in self.band_decomposition:
            for jjj in iii:
                alg_len+=np.sign(jjj)
        return alg_len
    
    def is_terminal(self):
        if np.abs(self.algebraic_length())==len(self.band_decomposition):
            return True
        else:
            return False
        
