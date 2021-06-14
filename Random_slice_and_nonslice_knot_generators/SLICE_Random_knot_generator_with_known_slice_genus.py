#!/usr/bin/env python
# coding: utf-8

import numpy as np
import pandas as pd
import os
import csv

def random_band(index,start,end,band_sign=0,conjugate_length_st_dev=0.8,sgr_st_dev=0.6):
    if start>end:
        temp=start
        start=end
        end=temp
    if band_sign==0:
        band_sign=np.random.choice([-1,1])
    center=np.random.randint(1,index)
    if start==center:
        left_crossings=[]
    elif start==center+1:
        left_crossings=[center]
    elif start<center:
        left_crossings=[jjj for jjj in range(start,center,1)]
    elif start>center+1:
        left_crossings=[jjj for jjj in range(start-1,center-1,-1)]
    if end==center+1:
        right_crossings=[]
    elif end==center:
        right_crossings=[center]
    elif end<center:
        right_crossings=[jjj for jjj in range(end,center+1,1)]
    elif end>center+1:
        right_crossings=[jjj for jjj in range(end-1,center,-1)]
    band=[]
    start_strand=start
    end_strand=end
    #print("left crossings: ",left_crossings)
    #print("right crossings: ",right_crossings)
    #print("center: ",center)
    if center>=end:
        end_direction=1
    else:
        end_direction=-1
    if center<start:
        start_direction=-1
    else:
        start_direction=1
    while len(left_crossings)+len(right_crossings)>0:
        #print("start strand: ",start_strand)
        #print("end strand: ",end_strand)
        #print("left crossings: ",left_crossings)
        #print("right crossings: ",right_crossings)
        #print("band: ",band)
        if start_strand+1==end_strand:
            if center>=end_strand:
                band.append(np.random.choice([-1,1])*right_crossings.pop(0))
                end_strand=end_strand+end_direction
            elif center<start_strand:
                band.append(np.random.choice([-1,1])*left_crossings.pop(0))
                start_strand=start_strand+start_direction
        else:
            coin=np.random.randint(0,2)
            if coin:
                if len(left_crossings)>0:
                    band.append(np.random.choice([-1,1])*left_crossings.pop(0))
                    start_strand=start_strand+start_direction
                else:
                    band.append(np.random.choice([-1,1])*right_crossings.pop(0))
                    end_strand=end_strand+end_direction
            else:
                if len(right_crossings)>0:
                    band.append(np.random.choice([-1,1])*right_crossings.pop(0))
                    end_strand=end_strand+end_direction
                else:
                    band.append(np.random.choice([-1,1])*left_crossings.pop(0))
                    start_strand=start_strand+start_direction
    sgr_count=int(np.floor(np.abs(np.random.normal(0,sgr_st_dev))))
    #print(sgr_count)
    possible_crossings=[jjj for jjj in range(1,index)]+[-jjj for jjj in range(1,index)]
    for iii in range(sgr_count):
        sign=np.random.choice([-1,1])
        location=np.random.randint(0,len(band)+2)
        crossing=np.random.choice([jjj for jjj in range(1,index)]+[-jjj for jjj in range(1,index)])
        conjugate_length=int(np.floor(np.abs(np.random.normal(0,conjugate_length_st_dev))))
        conjugator=list(np.random.choice(possible_crossings,conjugate_length))
        inverse_conjugator=[-jjj for jjj in conjugator]
        inverse_conjugator.reverse()
        relator=conjugator+[sign*crossing,sign*crossing]+inverse_conjugator
        #print(relator)
        band[location:location]=relator
    inverse_band=[-jjj for jjj in band]
    inverse_band.reverse()
    full_band=band+[band_sign*center]+inverse_band
    #print("length of full band = ",len(full_band))
    return full_band


def insert_strand(braid,index,location,sign):
    if location>index+1 or location<1:
      print('Location of inserted strand out of range.')
      return
    for jjj in range(len(braid)):
        if braid[jjj]>=np.abs(location):
            braid[jjj]=braid[jjj]+1
        if -braid[jjj]>=np.abs(location):
            braid[jjj]=braid[jjj]-1
    iii=0
    while iii<len(braid):
        if np.abs(braid[iii])==np.abs(location)-1:
            braid.insert(iii,sign*location)
            braid.insert(iii+2,-sign*location)
            iii=iii+3
        else:
            iii=iii+1
    return braid


def permutation_test(braid,index,stopping_index=-1):
    if stopping_index==-1:
        stopping_index=len(braid)
    permutation=[jjj+1 for jjj in range(index)]
    for jjj in range(stopping_index):
        crossing=np.abs(braid[jjj])
        temp=permutation[crossing-1]
        permutation[crossing-1]=permutation[crossing]
        permutation[crossing]=temp
    return permutation


def permutation_cycle_test(braid,index,stopping_index=-1):
    not_visited=[jjj for jjj in range(1,index+1)]
    visited=[]
    permutation_cycle=[]
    permutation_list=permutation_test(braid,index,stopping_index)
    while len(not_visited)>0:
        starting_strand=not_visited[0]
        jjj=starting_strand
        permutation_sub_cycle=[jjj]
        next_strand=permutation_list.index(jjj)+1
        visited.append(jjj)
        not_visited.remove(jjj)
        while next_strand!=starting_strand:
            jjj=next_strand
            next_strand=permutation_list.index(jjj)+1
            permutation_sub_cycle.append(jjj)
            visited.append(jjj)
            not_visited.remove(jjj)
        permutation_cycle.append(permutation_sub_cycle)
    return permutation_cycle  


def permutation_cycle_from_transpositions(transposition_list,index):
    permutation_braid=[]
    for jjj in range(len(transposition_list)):
        transposition_list[jjj].sort()
        transposition=transposition_list[jjj]
        band=[jjj for jjj in range(transposition[0],transposition[1])]+[-jjj for jjj in range(transposition[1]-2,transposition[0]-1,-1)]
        #print(band)
        permutation_braid=permutation_braid+band
    #return permutation_braid
    return permutation_cycle_test(permutation_braid,index)


def insert_random_band(braid,index,start,end,band_sign,conjugate_length_st_dev=0.8,sgr_st_dev=0.6):
    if start>end:
        temp=start
        start=end
        end=temp
    position=np.random.choice([jjj for jjj in range(len(braid)+1)])
    permutation=permutation_test(braid,index,position)
    new_start=permutation.index(start)+1
    new_end=permutation.index(end)+1
    band=random_band(index,new_start,new_end,band_sign,conjugate_length_st_dev,sgr_st_dev)
    braid[position:position]=band
    return braid


def band_permutation_connectivity(index,band_count):
    if (index-1)%2!=band_count%2:
        band_count=band_count+1
    band_connection_list=[]
    position=index
    split_bands=(band_count-(index-1))//2
    merge_bands=band_count-split_bands
    for jjj in range(band_count):
        if position==index or split_bands==0:
            band_connection_list.append(-1)
            position=position-1
            merge_bands=merge_bands-1
        elif position==1 or merge_bands==0:
            band_connection_list.append(1)
            position=position+1
            split_bands=split_bands-1
        elif np.random.choice([0,1])==0:
            band_connection_list.append(1)
            position=position+1
            split_bands=split_bands-1
        else:
            band_connection_list.append(-1)
            position=position-1
            merge_bands=merge_bands-1
    return band_connection_list


def simplify_R2(braid,starting_position=0,all=False):
  new_braid=braid.copy()
  old_braid=[]
  position=starting_position
  while new_braid!=old_braid:
    old_braid=new_braid.copy()
    jjj=0
    while jjj<len(new_braid):
      if new_braid[(position+jjj)%len(new_braid)]==-new_braid[(position+jjj+1)%len(new_braid)]:
        smaller_index=min((position+jjj)%len(new_braid),(position+jjj+1)%len(new_braid))
        larger_index=max((position+jjj)%len(new_braid),(position+jjj+1)%len(new_braid))
        new_braid.pop(larger_index)
        new_braid.pop(smaller_index)
        if not all:
          return new_braid
      jjj=jjj+1
  return new_braid


def add_random_R2(braid,index):
  new_braid=braid.copy()
  location=np.random.choice(len(braid)+2)
  crossing=np.random.choice([jjj for jjj in range(1,index+1)])
  sign=np.random.choice([-1,1])
  if location==len(braid)+1:
    new_braid[index:index]=[sign*crossing]
    new_braid[0:0]=[-sign*crossing]
  else:
    new_braid[location:location]=[sign*crossing,-sign*crossing]
  return new_braid

def random_cut(braid):
  new_braid=braid.copy()
  location=np.random.choice(len(braid))
  new_braid=new_braid[location:]+new_braid[:location]
  return new_braid


def apply_R3(braid,starting_position=0):
  new_braid=braid.copy()
  position=starting_position
  for iii in range(len(braid)):
    loc1=(iii+position)%len(new_braid)
    loc2=(iii+position+1)%len(new_braid)
    loc3=(iii+position+2)%len(new_braid)
    if new_braid[loc1]==new_braid[loc3] and np.abs(new_braid[loc1]-new_braid[loc2])==1:
      letter1=new_braid[loc1]
      letter2=new_braid[loc2]
      new_braid[loc1]=letter2
      new_braid[loc3]=letter2
      new_braid[loc2]=letter1
    elif new_braid[loc1]==-new_braid[loc3] and np.abs(np.abs(new_braid[loc1])-np.abs(new_braid[loc2]))==1:
      letter1=new_braid[loc1]
      letter2=new_braid[loc2]
      letter3=new_braid[loc3]
      sign=np.sign(letter1)*np.sign(letter2)
      new_braid[loc1]=np.abs(letter2)*np.sign(letter3)
      new_braid[loc2]=sign*letter1
      new_braid[loc3]=sign*letter2
  return new_braid


def braid_word_to_string(braid,index):
  capitals="ABCDEFGHIJKLMNOPQRSTUVWXYZ"
  lowercases="abcdefghijklmnopqrstuvwxyz"
  let={}
  for jjj in range(index):
    let[1+jjj]=capitals[jjj]
    let[-1-jjj]=lowercases[jjj]
  L=[let[jjj] for jjj in braid]
  braid_string=""
  for jjj in L:
    braid_string=braid_string+jjj
  return braid_string
  
  
def remove_min_R1(braid):
    b=np.copy(braid)
    oldLength=len(b)
    newLength=0
    while oldLength>newLength:
        oldLength=len(b)
        if len(b)==0:
        	return b
        if len(np.where(np.abs(b)==min(np.abs(b)))[0])==1:
            location=np.where(np.abs(b)==min(np.abs(b)))[0][0]
            b=np.delete(b,location)
            for jjj in range(len(b)):
                b[jjj]=b[jjj]-np.sign(b[jjj])
        newLength=len(b)
    return b
    
    
def remove_max_R1(braid):
    b=np.copy(braid)
    oldLength=len(b)
    newLength=0
    while oldLength>newLength:
        oldLength=len(b)
        if len(b)==0:
        	return b
        if len(np.where(np.abs(b)==max(np.abs(b)))[0])==1:
            location=np.where(np.abs(b)==max(np.abs(b)))[0][0]
            b=np.delete(b,location)
        newLength=len(b)
    return b
    
    
def remove_R2(braid):
    b=np.copy(braid)
    oldLength=len(b)
    newLength=0
    while oldLength>newLength:
        oldLength=len(b)
        if b[oldLength-1]==-b[0]:
            b=np.delete(b,np.array([oldLength-1]))
            b=np.delete(b,np.array([0]))
        jjj=0
        while jjj<len(b)-1:
            if b[jjj]==-b[jjj+1]:
                b=np.delete(b,np.array([jjj,jjj+1]))
            else:
                jjj+=1
        newLength=len(b)
    return b
    
    
def simplify_braid(braid):
    b=np.copy(braid)
    oldLength=len(b)
    newLength=0
    while oldLength>newLength:
        oldLength=len(b)
        b=remove_R2(b)
        b=remove_min_R1(b)
        b=remove_max_R1(b)
        b=remove_R2(b)
        newLength=len(b)
    return list(b)
    

def random_braid(seed_braid=[],seed_slice_genus=0,max_index=7,initial_bands_std_dev=1.75,markov_bands_std_dev=1.5,conjugate_length_st_dev=1,sgr_st_dev=0.6,max_starting_bands=6,max_markov_bands=3,seed_sign=0,slice_knot=False):
    if len(seed_braid)==0:
      seed_slice_genus=0
    if seed_sign==0:
      seed_sign=np.random.choice([-1,1])
    braid=seed_braid.copy()
    slice_genus_lower_bound=seed_slice_genus
    slice_genus_upper_bound=seed_slice_genus
    index=np.random.choice([jjj for jjj in range(2,max_index+1)])
    sign_seed=np.random.choice([-1,1])
    #print("index = ",index)
    ###initial_bands=np.random.choice([jjj for jjj in range(index-1,index-1+max_starting_bands+1)])
    initial_bands=int(np.floor(np.abs(np.random.normal(0,initial_bands_std_dev))))+index-1
    ###markov_positive_bands=np.random.choice([jjj for jjj in range(max_markov_bands+1)])
    ###markov_negative_bands=np.random.choice([jjj for jjj in range(max_markov_bands+1-markov_positive_bands)])
    markov_positive_bands=int(np.floor(np.abs(np.random.normal(0,markov_bands_std_dev))))
    markov_negative_bands=int(np.floor(np.abs(np.random.normal(0,markov_bands_std_dev))))
    if initial_bands-index+1<=0:
      cobordism_band_count=0
    else:
      cobordism_band_count=np.random.choice(initial_bands-index+1)
    cobordism_positive_bands=np.random.choice([jjj for jjj in range(cobordism_band_count+1)])
    cobordism_negative_bands=np.random.choice([jjj for jjj in range(cobordism_band_count+1-cobordism_positive_bands)])
    if (initial_bands+cobordism_positive_bands+cobordism_negative_bands)%2!=(index-1)%2:
      #band_choice=np.random.choice([0,1,2])
      band_choice=0
      if band_choice==0:
        initial_bands=initial_bands+1
      if band_choice==1:
        cobordism_positive_bands=cobordism_positive_bands+1
      if band_choice==2:
        cobordism_negative_bands=cobordism_negative_bands+1 
    if slice_knot:
        cobordism_positive_bands=0
        cobordism_negative_bands=0
        initial_bands=index-1
    #print("initial bands = ",initial_bands) 
    #print("markov bands = ",markov_negative_bands+markov_positive_bands)
    #print("cobordism bands = ",cobordism_negative_bands+cobordism_positive_bands)
    total_bands=initial_bands+markov_positive_bands+markov_negative_bands+cobordism_positive_bands+cobordism_negative_bands
    band_connectivity_list=band_permutation_connectivity(index,initial_bands+cobordism_positive_bands+cobordism_negative_bands)
    total_band_connectivity_list=band_connectivity_list.copy()
    permutation=permutation_cycle_test(braid,index)
    cycle_count=index
    #print("initial permutation: ",permutation_cycle_test(braid,index))
    for jjj in range(initial_bands):
      if band_connectivity_list[jjj]==-1:
        m,n=np.random.choice(len(permutation),2,replace=False)
        start_strand=np.random.choice(permutation[m])
        end_strand=np.random.choice(permutation[n])
        braid=insert_random_band(braid,index,start_strand,end_strand,seed_sign,conjugate_length_st_dev,sgr_st_dev)
        #print("merge two components (initial band): ",permutation_cycle_test(braid,index))
      if band_connectivity_list[jjj]==1:
        long_cycles=permutation.copy()
        for jjj in permutation:
          if len(jjj)<2:
            long_cycles.remove(jjj)
        n=np.random.choice(len(long_cycles))
        start_strand,end_strand=np.random.choice(long_cycles[n],2,replace=False)
        braid=insert_random_band(braid,index,start_strand,end_strand,seed_sign,conjugate_length_st_dev,sgr_st_dev)
        #print("split a component (initial band): ",permutation_cycle_test(braid,index))
      permutation=permutation_cycle_test(braid,index)
    band_connectivity_list=band_connectivity_list[initial_bands:]
    markov_positions=np.sort(np.random.choice([jjj for jjj in range(1,index+1+markov_positive_bands+markov_negative_bands)],markov_positive_bands+markov_negative_bands,replace=False))
    #print("markov positions: ",markov_positions)
    for jjj in markov_positions:
      braid=insert_strand(braid,index,jjj,np.random.choice([-1,1]))
      index=index+1
    #print("after inserting new strands: ",permutation_cycle_test(braid,index))
    np.random.shuffle(markov_positions)
    markov_signs=[1 for jjj in range(markov_positive_bands)]+[-1 for jjj in range(markov_negative_bands)]
    np.random.shuffle(markov_signs)
    for jjj in range(len(markov_positions)):
      #print("Markov strand: ",markov_positions[jjj])
      permutation=permutation_cycle_test(braid,index)
      possible_end_components=[]
      for iii in permutation:
        if markov_positions[jjj] not in iii:
          possible_end_components.append(iii)
      #print("possible end components: ",possible_end_components)
      #possible_end_strands=np.random.choice(np.random.choice(possible_end_components))
      #possible_end_strands.remove(markov_positions[jjj])
      m=np.random.choice(len(possible_end_components))
      end_component=possible_end_components[m]
      #print("end component: ",end_component)
      end_strand=np.random.choice(end_component)
      #print("end strand: ",end_strand)
      braid=insert_random_band(braid,index,markov_positions[jjj],end_strand,markov_signs[jjj],conjugate_length_st_dev,sgr_st_dev)
      #print("kept number of components constant (markov band between ",end_strand,markov_positions[jjj],"): ",permutation_cycle_test(braid,index))
    cobordism_bands=cobordism_negative_bands+cobordism_positive_bands
    cobordism_signs=[1 for jjj in range(cobordism_positive_bands)]+[-1 for jjj in range(cobordism_negative_bands)]
    np.random.shuffle(cobordism_signs)
    permutation=permutation_cycle_test(braid,index)
    for jjj in range(cobordism_bands):
      if band_connectivity_list[jjj]==-1:
        m,n=np.random.choice(len(permutation),2,replace=False)
        start_strand=np.random.choice(permutation[m])
        end_strand=np.random.choice(permutation[n])
        braid=insert_random_band(braid,index,start_strand,end_strand,cobordism_signs[jjj],conjugate_length_st_dev,sgr_st_dev)
        #print("merge two components (cobordism band): ",permutation_cycle_test(braid,index))
      if band_connectivity_list[jjj]==1:
        long_cycles=permutation.copy()
        for iii in permutation:
          if len(iii)<2:
            long_cycles.remove(iii)
        n=np.random.choice(len(long_cycles))
        start_strand,end_strand=np.random.choice(long_cycles[n],2,replace=False)
        braid=insert_random_band(braid,index,start_strand,end_strand,cobordism_signs[jjj],conjugate_length_st_dev,sgr_st_dev)
      permutation=permutation_cycle_test(braid,index)
      #print("split a component (cobordism band): ",permutation_cycle_test(braid,index))
    #print(total_band_connectivity_list)
    #print(len(braid))
    for lll in range(np.random.choice(range(len(braid)))):
        k=np.random.choice(len(braid))
        braid=apply_R3(braid,k)
    braid=simplify_R2(braid,all=True)
    for lll in range(np.random.choice(range(len(braid)))):
        k=np.random.choice(len(braid))
        braid=apply_R3(braid,k)
    braid=simplify_R2(braid,all=True)
    braid=random_cut(braid)
    #print(braid)
    braid_string=braid_word_to_string(simplify_braid(braid),index)
    return braid,simplify_braid(braid),braid_string,index,index-initial_bands-markov_negative_bands-markov_positive_bands-cobordism_negative_bands-cobordism_positive_bands,min(1,index-initial_bands-markov_negative_bands-markov_positive_bands+cobordism_negative_bands+cobordism_positive_bands)


column_names=["Braid word","Simplified braid word","Braid string","Braid index","Euler characteristic lower bound","Euler characteristic upper bound","Rasmussen s-invariant","Arf invariant","Signature","Determinant","Alexander Polynomial"]


identifier=str(np.random.choice(999999999))

print("Identifier string = ",identifier)

#non_slice_knots=pd.DataFrame(columns=column_names)

max_braid_length=45
number_of_braids=10000

with open("output/sliceWithInvariants"+identifier+".csv", "w", newline='') as csv_file:
	writer = csv.writer(csv_file, delimiter=',')
	writer.writerow(column_names)

	jjj=1

	while jjj<=number_of_braids:
		braid=random_braid(slice_knot=True,conjugate_length_st_dev=1.35,sgr_st_dev=1)
		braid_list=list(braid)
		if len(braid[1])<=max_braid_length:
			if len(braid[2])>2:
				input_file=open("tempfiles/slicebraidword"+identifier+".brd", "w")
				input_file.write(braid[2])
				input_file.close()
				os.system("java -jar KnotJob/KnotJob_j8.jar tempfiles/slicebraidword"+identifier+".brd -s0")
				output_file=open("tempfiles/slicebraidword"+identifier+".brd_s0")
				lines=output_file.readlines()
				string=lines[1]
				s_invariant_string=string.split(":")[-1]
				s_invariant=int(s_invariant_string.replace(" ","").replace("\n",""))
				braid_list.append(s_invariant)
				if len(braid_list[1])>2:
					os.system("./Arf_and_signature_calculator.wls "+str(braid_list[1]).replace(" ","")+" "+identifier)
					output_file_arf_sig=open("tempfiles/ArfSignature"+identifier+".txt")
					lines_arf_sig=output_file_arf_sig.readlines()
					arf=int(lines_arf_sig[0].replace(" ","").replace("\n",""))
					signature=int(lines_arf_sig[1].replace(" ","").replace("\n",""))
					determinant=int(lines_arf_sig[2].replace(" ","").replace("\n",""))
					alexander=str(lines_arf_sig[3].replace(" ","").replace("\n","").replace("{","[").replace("}","]"))
					braid_list.append(arf)
					braid_list.append(signature)
					braid_list.append(determinant)
					braid_list.append(alexander)
				else:
					braid_list.append(0)
					braid_list.append(0)
					braid_list.append(0)
					braid_list.append(1)
					braid_list.append("[[0,1]]")
			else:
				braid_list.append(0)
				braid_list.append(0)
				braid_list.append(0)
				braid_list.append(1)
				braid_list.append("[[0,1]]")
			#non_slice_knots.loc[len(non_slice_knots)]=braid_list
			writer.writerow(braid_list)
			if jjj%10==0:
				print("jjj = ",jjj)
				csv_file.flush()
			jjj=jjj+1
	csv_file.flush()
        
#non_slice_knots.to_csv("output/nonslice"+identifier+".csv",index=False)