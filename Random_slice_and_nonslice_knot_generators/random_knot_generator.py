#!/usr/bin/env python
# coding: utf-8

import numpy as np
import pandas as pd
import os
import csv
import random,re


def random_band(index,start,end,band_sign=0,conjugate_length_st_dev=0.8,sgr_st_dev=0.6):
    """
    Creates a random band for a braid of given index, where the band starts at strand number start, ends at band 
    number end, and has prescribed sign.  The conjugate_length_st_dev controls the distribution of the conjugate
    length, and sgr_st_dev controls the distribution of how many symmetric group relators of the form sigma^2=1
    are added into the conjugators.
    """
    # If the start strand number is higher than the end strand number, swap them.
    if start>end:     
        temp=start
        start=end
        end=temp
    # If no argumant is given for band sign, randomly select between -1 and 1.
    if band_sign==0:  
        band_sign=np.random.choice([-1,1])
    # Randomly select the center of the band (the braid letter for the central crossing).
    center=np.random.randint(1,index)
    # Create lists of crossings needed on the 'left' and 'right' of the central crossing to connect the start
    # and end strands to it.
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
    # Specify the directions we will be adding crossings in based on the central crossings location relative to the 
    # start and end strands.
    if center>=end:
        end_direction=1
    else:
        end_direction=-1
    if center<start:
        start_direction=-1
    else:
        start_direction=1
    # Build the conjugator by selecting crossings from the left and right crossing lists at random (when possible).
    while len(left_crossings)+len(right_crossings)>0:
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
    # Determine the number of symmetric group relators to include in the conjugator (words of the form sigma^2).
    sgr_count=int(np.floor(np.abs(np.random.normal(0,sgr_st_dev))))
    possible_crossings=[jjj for jjj in range(1,index)]+[-jjj for jjj in range(1,index)]
    # Add the symmetric group relators into the conjugator.
    for iii in range(sgr_count):
        sign=np.random.choice([-1,1])
        location=np.random.randint(0,len(band)+2)
        crossing=np.random.choice(possible_crossings)
        # Create a conjugator for the symmetric group relator that will be insterted into the main conjugator.
        conjugate_length=int(np.floor(np.abs(np.random.normal(0,conjugate_length_st_dev))))
        conjugator=list(np.random.choice(possible_crossings,conjugate_length))
        inverse_conjugator=[-jjj for jjj in conjugator]
        inverse_conjugator.reverse()
        relator=conjugator+[sign*crossing,sign*crossing]+inverse_conjugator
        band[location:location]=relator
    inverse_band=[-jjj for jjj in band]
    inverse_band.reverse()
    full_band=band+[band_sign*center]+inverse_band
    return full_band


def insert_strand(braid,index,location,sign):
    """
    Function for inserting an unknotted, unlinked strand into a braid with given index, at a strand number 
    determined by the argument location.  The argument sign determines whether this strand will pass on top 
    of, or below the existing braid.
    """
    if location>index+1 or location<1:
      print('Location of inserted strand out of range.')
      return
    # Adjust all crossings which are to the right of the inserted strand.
    for jjj in range(len(braid)):
        if braid[jjj]>=np.abs(location):
            braid[jjj]=braid[jjj]+1
        if -braid[jjj]>=np.abs(location):
            braid[jjj]=braid[jjj]-1
    # Modify all crossings which are located next to the inserted strand, by adding a pair of neighboring crossings
    # around the affected strand.
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
    """
    Returns the permutation associated to a given braid with specified index.  The optional stopping_index allows 
    you to determine the permutation up to a certain point in the braid word (the word index should probably not
    be used here, since it has nothing to do with the actual braid index.) The permutation is returned in one-line 
    notation (not cycle notation).
    """
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
    """
    Similar to the above function permutation_test, except that the answer is returned as a list of disjoint cycles
    representing the braid permutation.
    """
    # List of strands not checked.
    not_visited=[jjj for jjj in range(1,index+1)]
    # List of strands checked.
    visited=[]
    # List to collect disjoint cycles representing the braid permutation.
    permutation_cycle=[]
    # One line notation representing the braid permutation.
    permutation_list=permutation_test(braid,index,stopping_index)
    while len(not_visited)>0:
        # Pick a strand we haven't visited yet, and start building up it's cycle.
        starting_strand=not_visited[0]
        jjj=starting_strand
        permutation_sub_cycle=[jjj]
        next_strand=permutation_list.index(jjj)+1
        visited.append(jjj)
        not_visited.remove(jjj)
        # Cycle through the permutation, adding strands to the cycle until you reach the starting strand again.
        while next_strand!=starting_strand:
            jjj=next_strand
            next_strand=permutation_list.index(jjj)+1
            permutation_sub_cycle.append(jjj)
            visited.append(jjj)
            not_visited.remove(jjj)
        permutation_cycle.append(permutation_sub_cycle)
    return permutation_cycle  


def permutation_cycle_from_transpositions(transposition_list,index):
    """
    Takes a list of transpositions and returns the associated permutation written in dijoint cycle notation.
    """
    # Temporary braid to build up the permutation.
    permutation_braid=[]
    for jjj in range(len(transposition_list)):
        transposition_list[jjj].sort()
        transposition=transposition_list[jjj]
        # Create a band connecting the two strands involved in the permutation, and add it to the temporary braid.
        band=[jjj for jjj in range(transposition[0],transposition[1])]+[-jjj for jjj in range(transposition[1]-2,transposition[0]-1,-1)]
        permutation_braid=permutation_braid+band
    # Return the braid permutation of the temporary braid.
    return permutation_cycle_test(permutation_braid,index)


def insert_random_band(braid,index,start,end,band_sign,conjugate_length_st_dev=0.8,sgr_st_dev=0.6):
    """
    Insert a random band into a given braid, with a given sign, at a random point in the braid word, connecting 
    strand numbers start and end (labeled in relation to their positions at the beginning of the braid word).
    """
    # If start strand number is greater than the end strand number, swap the two values.
    if start>end:
        temp=start
        start=end
        end=temp
    # Select the position in the braid word to insert the new band.
    position=np.random.choice([jjj for jjj in range(len(braid)+1)])
    # Find the braid permutation up to the position where the band will be inserted.
    permutation=permutation_test(braid,index,position)
    # Determine the new start and end strand numbers (based on the partial permutation found above).
    new_start=permutation.index(start)+1
    new_end=permutation.index(end)+1
    # Construct a random band with specified start and end strands, and add it to the braid.
    band=random_band(index,new_start,new_end,band_sign,conjugate_length_st_dev,sgr_st_dev)
    braid[position:position]=band
    return braid


def band_permutation_connectivity(index,band_count,braid=[]):
    """
    Returns a random list of -1 and 1, which can be interpreted as a sequence of merging bands (-1) and splitting 
    bands (+1) to be added to a braid to ensure the closure of the final result is connected (a knot).  The argument
    band_count specifies how long this list is.
    """
    position=len(permutation_cycle_test(braid,index))
    # If the parity of band_cound is not correct (meaning the resulting braid will never yield a knot) add 1 to the
    # band_count.
    if (position-1)%2!=band_count%2:
        band_count=band_count+1
    ## If the number of bands specificed is not enough to yeild a braid with connected closure, return an error 
    ## message.
    #if (index-1)<band_count:
    #    print("Error: not enough bands specified to yeild a braid with connected closure.")
    #    return
    band_connection_list=[]
    # Determine the number of bands that will split a given component into two.
    split_bands=(band_count-(position-1))//2
    # Determine the number of bands that will merge two components into one.
    merge_bands=band_count-split_bands
    # Arrange a list of -1s and 1s in a way so that you never have more than index number of components, or less
    # than one component.
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


def simplify_R2(braid,starting_position=0,remove_all=False):
    """
    Scans the braid word, starting at the specified position, and removes neigboring pairs of canceling crossings.
    If the option remove_all is True it will remove all such pairs; if not, it only removes the first one it 
    encounters.
    """
    new_braid=braid.copy()
    old_braid=[]
    position=starting_position
    # Perform the search and remove as long as the previous step returned a different braid than it started with.
    while new_braid!=old_braid:
        old_braid=new_braid.copy()
        # Begin scanning the braid word, starting at the specified position.
        jjj=0
        while jjj<len(new_braid):
            # If a cancelling pair is located, remove it.
            if new_braid[(position+jjj)%len(new_braid)]==-new_braid[(position+jjj+1)%len(new_braid)]:
                smaller_index=min((position+jjj)%len(new_braid),(position+jjj+1)%len(new_braid))
                larger_index=max((position+jjj)%len(new_braid),(position+jjj+1)%len(new_braid))
                new_braid.pop(larger_index)
                new_braid.pop(smaller_index)
                # If remove_all is set to False stop after one such removal, otherwise continue until no such 
                # cancelling pairs remain.
                if not remove_all:
                    return new_braid
            jjj=jjj+1
    return new_braid



def add_random_R2(braid,index):
    """
    Add a random pair of cancelling crossings somewhere in the braid word.
    """
    new_braid=braid.copy()
    # Specify the location and index of the new crossing to be added.
    location=np.random.choice(len(braid)+2)
    crossing=np.random.choice([jjj for jjj in range(1,index+1)])
    # Select a random sign for the pair of crossings.
    sign=np.random.choice([-1,1])
    # If the location is selected at the end of the word, add one of the crossings at the end and the other at the
    # beginning.
    if location==len(braid)+1:
        new_braid[index:index]=[sign*crossing]
        new_braid[0:0]=[-sign*crossing]
    # Otherwise, add both at the specified location.
    else:
        new_braid[location:location]=[sign*crossing,-sign*crossing]
    return new_braid



def random_cut(braid):
    """
    Split the braid word at a random point, and concatenate the two pieces in reverse order.
    """
    new_braid=braid.copy()
    location=np.random.choice(len(braid))
    new_braid=new_braid[location:]+new_braid[:location]
    return new_braid


def apply_R3(braid,starting_position=0):
    """
    Scan through the braid word, starting at the specified starting position, find the first place that an R3 braid
    relation can be applied, and apply it.
    """
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
    """
    Converts a braid word into a braid string, with crossing 1 corresponding to A, crossings -1 corresponding to a,
    etc.  
    
    Note: The maximal braid index allowable is 26.
    """
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
    """
    Check the braid word for any R1 moves that can be removed on the left of the braid, and if so, remove them. 
    
    Note: Removing such R1 'kinks' is necessary for producing correct answers in software like KnotJob and the 
    Mathematica KnotTheory package.
    """
    b=np.copy(braid)
    oldLength=len(b)
    newLength=0
    while oldLength>newLength:
        oldLength=len(b)
        if len(b)==0:
            return b
        # Check if the minimal crossing that appears is unique (i.e. if it only shows up once in the braid word).
        # If so, there is an R1 move that can be removed.
        if len(np.where(np.abs(b)==min(np.abs(b)))[0])==1:
            location=np.where(np.abs(b)==min(np.abs(b)))[0][0]
            b=np.delete(b,location)
            # For all remaining crossings in the braid word, adjust them to account for the deleted R1 move.
            for jjj in range(len(b)):
                b[jjj]=b[jjj]-np.sign(b[jjj])
        newLength=len(b)
    return b



def remove_max_R1(braid):
    """
    Similar to the function above, this searches for R1 moves (Markov stabilizations) that can be removed on the 
    right side of the braid.
    """
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
    """
    Scans through the braid and removes all pairs of cancelling adjacent crossings.  This should be equivalent to
    the simplify_R2 function with the remove_all option set to True.  Not sure why I coded it up twice.
    """
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
    """
    Applies the above functions repeatedly to try to remove all cancelling crossing pairs and R1 moves, to shorten
    the braid word as much as possible.
    """
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


def random_line(afile):
    line = next(afile)
    for num, aline in enumerate(afile, 2):
        if random.randrange(num):
            continue
        line = aline
    return line


SliceFile = "SliceSeedBraids.csv"
NonSliceFile = "NonSliceSeedBraids.csv"


def pull_seed_braid(slice_knot="maybe"):
    """
    Opens a file from KnotInfo corresponding to slice or nonslice knots, and return a braid representative for a 
    knot along with the lower and upper bounds on the slice Euler characteristic.
    """
    if slice_knot==True:
        file=SliceFile
    elif slice_knot==False:
        file=NonSliceFile
    else:
        file=np.random.choice([SliceFile,NonSliceFile])
    # Open the file.
    a=random.choice(open(file, mode='r', encoding='utf-8-sig').readlines())
    # Replace string separating characters with "W", which will be replaced at the next step.
    b=a.replace('],[','W').replace(']","','W').replace(']",','W').replace('[','').replace(']','').replace('\n','').replace('"','')
    b=b.split("W")
    # Converting all string integers to ints.
    c=[[int(iii) for iii in jjj.split(",")] for jjj in b]
    genus=c.pop()
    euler_char_lower=1-2*max(genus)
    euler_char_upper=1-2*min(genus)
    loc=np.random.choice(len(c))
    braid=c[loc]
    index=max(np.abs(braid))+1
    return braid,index,euler_char_lower,euler_char_upper


def add_cobordism_bands(input_braid,index,cobordism_negative_bands,cobordism_positive_bands,band_connectivity_list,conjugate_length_st_dev=1,sgr_st_dev=0.6):
    """
    Adds cobordism bands to a given braid (i.e. bands to the existing braid, without increasing the number of 
    braid strands).  The signs of the bands are determined by the values of cobordism_negative_bands, and 
    cobordism_positive_bands, while the connectivity is determined by the band_connectivity_list.
    """
    # Compile a list of cobordism bands, and randomize their signs (keeping the number of each sign fixed).
    cobordism_bands=cobordism_negative_bands+cobordism_positive_bands
    cobordism_signs=[1 for jjj in range(cobordism_positive_bands)]+[-1 for jjj in range(cobordism_negative_bands)]
    np.random.shuffle(cobordism_signs)
    braid=input_braid.copy()
    permutation=permutation_cycle_test(braid,index)
    for jjj in range(cobordism_bands):
        # If element of band_connectivity_list indicates a merging band should be added, select two cycles
        # of the braid permutation, and select one strand from each cycle, before inserting a band of the 
        # correct sign at the chosen endpoints.
        if band_connectivity_list[jjj]==-1:
            m,n=np.random.choice(len(permutation),2,replace=False)
            start_strand=np.random.choice(permutation[m])
            end_strand=np.random.choice(permutation[n])
            braid=insert_random_band(braid,index,start_strand,end_strand,cobordism_signs[jjj],conjugate_length_st_dev,sgr_st_dev)
        # If element of band_connectivity_list indicates a splitting band should be added, create a list of 
        # all cycles in the braid permutation which involve more than one strand, then select one such cycle
        # and two strands from it.  Finally, insert a band of the correct sign at the chosen endpoints.
        if band_connectivity_list[jjj]==1:
            long_cycles=permutation.copy()
            for iii in permutation:
                if len(iii)<2:
                    long_cycles.remove(iii)
            n=np.random.choice(len(long_cycles))
            start_strand,end_strand=np.random.choice(long_cycles[n],2,replace=False)
            braid=insert_random_band(braid,index,start_strand,end_strand,cobordism_signs[jjj],conjugate_length_st_dev,sgr_st_dev)
        permutation=permutation_cycle_test(braid,index)
    return braid,index,band_connectivity_list[cobordism_bands:]


def add_markov_bands(input_braid,index,markov_negative_bands,markov_positive_bands,conjugate_length_st_dev=1,sgr_st_dev=0.6):
    """
    Adds Markov bands to a given braid (i.e. bands connecting the existing braid to a new unknotted strand.  The 
    signs of the bands are determined by the values of markov_negative_bands, and markov_positive_bands.
    """
    braid=input_braid.copy()
    # Select the locations of where to insert the strands that will be used for the Markov bands.
    markov_positions=np.sort(np.random.choice([jjj for jjj in range(1,index+1+markov_positive_bands+markov_negative_bands)],markov_positive_bands+markov_negative_bands,replace=False))
    # Insert the new strands for the Markov bands.
    for jjj in markov_positions:
        braid=insert_strand(braid,index,jjj,np.random.choice([-1,1]))
        index=index+1
    np.random.shuffle(markov_positions)
    # Randomize the signs of the Markov bands (keeping the number of each sign fixed).
    markov_signs=[1 for jjj in range(markov_positive_bands)]+[-1 for jjj in range(markov_negative_bands)]
    np.random.shuffle(markov_signs)
    # Insert bands to complete Markov bands.
    for jjj in range(len(markov_positions)):
        permutation=permutation_cycle_test(braid,index)
        possible_end_components=[]
        # Cycling through each new Markov strand, look at each cycle in the braid permutation and check 
        # whether it contains the Markov strand of interest.  If not, then add it to the list of possible
        # end components for a band connecting to the given Markov strand.
        for iii in permutation:
            if markov_positions[jjj] not in iii:
                possible_end_components.append(iii)
        # At random select one of the possible end components from the list compiled above.
        m=np.random.choice(len(possible_end_components))
        end_component=possible_end_components[m]
        # Select a strand from the end component selected above, and insert a random band with the sign 
        # determined above.
        end_strand=np.random.choice(end_component)
        braid=insert_random_band(braid,index,markov_positions[jjj],end_strand,markov_signs[jjj],conjugate_length_st_dev,sgr_st_dev)
    return braid,index


def initial_quasipositive_braid(initial_bands,index,seed_sign,band_connectivity_list,conjugate_length_st_dev=1,sgr_st_dev=0.6):
    """
    Produce a random quasipositive or quasinegative braid with the number of initial bands determined by 
    the value of initial_bands.
    """
    braid=[]
    permutation=permutation_cycle_test(braid,index)
    # Build the initial braid by adding initial_bands number of bands to the empty braid.
    for jjj in range(initial_bands):
        # If the entry in the band_connectivity_list is -1 add a band which merges two components.
        if band_connectivity_list[jjj]==-1:
            # Select two disjoint subcycles to merge from the permutation cycle description.
            m,n=np.random.choice(len(permutation),2,replace=False)
            # From each of the two subcycles selected, pick a strand.  One will be the start strand for the 
            # band while the other will be the end strand.
            start_strand=np.random.choice(permutation[m])
            end_strand=np.random.choice(permutation[n])
            braid=insert_random_band(braid,index,start_strand,end_strand,seed_sign,conjugate_length_st_dev,sgr_st_dev)
        # If the entry in the band_connectivity_list is 1 add a band which splits one component into two.
        if band_connectivity_list[jjj]==1:
            # Create a list of subcycles of length at least two which can be split.
            long_cycles=permutation.copy()
            for jjj in permutation:
                if len(jjj)<2:
                    long_cycles.remove(jjj)
            # Select a cycle to split, and a start strand and end strand from that cycle where the band will 
            # be placed.
            n=np.random.choice(len(long_cycles))
            start_strand,end_strand=np.random.choice(long_cycles[n],2,replace=False)
            braid=insert_random_band(braid,index,start_strand,end_strand,seed_sign,conjugate_length_st_dev,sgr_st_dev)
        # Rebuild the braid permutation cycle list.
        permutation=permutation_cycle_test(braid,index)
    return braid,index,band_connectivity_list[initial_bands:]



def random_braid(seed_braid="maybe",slice_knot="maybe",max_initial_index=5,max_total_bands=8,conjugate_length_st_dev=0.5,sgr_st_dev=0.5,seed_sign=0):
    """
    Creates a random braid. 
    """
    assert max_initial_index-1 <= max_total_bands, "The max initial index must be no greater than the max number of total bands plus 1." 
    if seed_braid=="maybe":
        seed_braid=np.random.choice([True,False])
    if slice_knot=="maybe":
        slice_knot=np.random.choice([True,False])
    if seed_sign==0:
        seed_sign=np.random.choice([-1,1])
    if not seed_braid:
        ######print("A")
        ######print(slice_knot)
        # Select an initial index.
        index=np.random.choice(np.arange(2,max_initial_index+1))
        # Must be enough total bands to achieve a knot.
        total_bands=np.random.choice(np.arange(index-1,max_total_bands+1))
        if slice_knot:
            initial_bands=index-1
            markov_bands=total_bands-initial_bands
            cobordism_bands=0
        else:
            initial_bands=np.random.choice(total_bands+1)
            cobordism_bands=np.random.choice(np.arange(max(0,index-1-initial_bands),total_bands-initial_bands+1))
            markov_bands=total_bands-initial_bands-cobordism_bands
            ######print("initial bands = ",initial_bands,",   index = ",index)
        # Check to ensure the parity of the total number of bands is correct to achieve a knot. Should never 
        # be triggered if slice_knot is true.
        if (cobordism_bands+initial_bands)%2==(index)%2:
            if np.random.choice([True,False]):
                initial_bands=initial_bands+np.random.choice([-1,1])
                if initial_bands==-1:
                    initial_bands=1
            else:
                cobordism_bands=cobordism_bands+np.random.choice([-1,1])
                if cobordism_bands==-1:
                    cobordism_bands=1
        markov_positive_bands=np.random.choice(markov_bands+1)
        markov_negative_bands=markov_bands-markov_positive_bands   
        cobordism_positive_bands=np.random.choice(cobordism_bands+1)
        cobordism_negative_bands=cobordism_bands-cobordism_positive_bands
        initial_band_connectivity_list=band_permutation_connectivity(index,initial_bands+cobordism_bands)
        ######print("PRE",initial_bands,cobordism_bands,initial_band_connectivity_list,permutation_cycle_test([],index))
        ######print("INPUT DATA = ",initial_bands,index,seed_sign,initial_band_connectivity_list,conjugate_length_st_dev,sgr_st_dev)
        # Build an initial quasipositive (or quasinegative) braid.
        braid,index,band_connectivity_list=initial_quasipositive_braid(initial_bands,index,seed_sign,initial_band_connectivity_list,conjugate_length_st_dev,sgr_st_dev)
        ######print("POST",initial_bands,cobordism_bands,band_connectivity_list,permutation_cycle_test(braid,index))
        euler_char_lower_bound=index-initial_bands-cobordism_bands
        euler_char_upper_bound=index-initial_bands+cobordism_bands
    if seed_braid:
        ######print("B")
        # Pull a seed braid from one of the lists.
        braid,index,euler_char_lower_bound,euler_char_upper_bound=pull_seed_braid(slice_knot)
        # This gives an estimate of the number of initial bands needed to construct the seed braid.  
        ######initial_bands=index-euler_char_upper_bound
        total_bands=np.random.choice(np.arange(index-1,max_total_bands+1))
        initial_bands=np.random.choice(total_bands+1)
        if slice_knot:
            markov_bands=total_bands-initial_bands
        else:
            markov_bands=np.random.choice(total_bands-initial_bands+1)
        # The following cobordism band count will be zero is slice_knot is True.
        cobordism_bands=total_bands-initial_bands-markov_bands
        # Check to ensure the parity of the total number of bands is correct to achieve a knot. Should never 
        # be triggered if slice_knot is true.
        if cobordism_bands%2==1:
            cobordism_bands=cobordism_bands+np.random.choice([-1,1]) 
        markov_positive_bands=np.random.choice(markov_bands+1)
        markov_negative_bands=markov_bands-markov_positive_bands   
        cobordism_positive_bands=np.random.choice(cobordism_bands+1)
        cobordism_negative_bands=cobordism_bands-cobordism_positive_bands
        band_connectivity_list=band_permutation_connectivity(index,cobordism_bands,braid)
        euler_char_lower_bound=euler_char_lower_bound-cobordism_bands
        euler_char_upper_bound=euler_char_upper_bound+cobordism_bands
    permutation=permutation_cycle_test(braid,index)
    # Add Markov bands to the braid.
    braid,index=add_markov_bands(braid,index,markov_negative_bands,markov_positive_bands,conjugate_length_st_dev,sgr_st_dev)
    # Add cobordism bands to the braid.
    braid,index,band_connectivity_list=add_cobordism_bands(braid,index,cobordism_negative_bands,cobordism_positive_bands,band_connectivity_list,conjugate_length_st_dev,sgr_st_dev)
    # Select a random number of R3 moves to apply to the resulting braid, then for each one select a random
    # location in the braid and apply the move.
    for lll in range(np.random.choice(range(len(braid)))):
        k=np.random.choice(len(braid))
        braid=apply_R3(braid,k)
    # Remove all cancelling pairs via R2 moves.
    braid=simplify_R2(braid,remove_all=True)
    # Repeat the proceedure of applying R3 moves randomly.
    for lll in range(np.random.choice(range(len(braid)//2+1))):
        k=np.random.choice(len(braid))
        braid=apply_R3(braid,k)
    # Remove any new R2 moves that were introduced.
    braid=simplify_R2(braid,remove_all=True)
    # Randomly cut the braid.
    braid=random_cut(braid)
    # Convert a simplified version of the braid to a string for use in KnotJob.
    braid_string=braid_word_to_string(simplify_braid(braid),index)
    return braid,simplify_braid(braid),braid_string,index,euler_char_lower_bound,euler_char_upper_bound

def bounded_random_braid(bound=30,seed_braid="maybe",slice_knot="maybe",max_initial_index=5,max_total_bands=8,conjugate_length_st_dev=0.5,sgr_st_dev=0.5,seed_sign=0):
    current_length=bound+1
    while current_length>bound:
        braid,simplified_braid,braid_string,index,euler_char_lower_bound,euler_char_upper_bound=random_braid(seed_braid="maybe",slice_knot="maybe",max_initial_index=5,max_total_bands=8,conjugate_length_st_dev=0.5,sgr_st_dev=0.5,seed_sign=0)
        current_length=len(braid)
    return braid,simplified_braid,braid_string,index,euler_char_lower_bound,euler_char_upper_bound 

column_names=["Braid word","Simplified braid word","Braid string","Braid index","Euler characteristic lower bound","Euler characteristic upper bound","Rasmussen s-invariant","Arf invariant","Signature","Determinant","Alexander Polynomial","Jones Polynomial","Khovanov Polynomial"]


identifier=str(np.random.choice(999999999))

print("Identifier string = ",identifier)

#non_slice_knots=pd.DataFrame(columns=column_names)

max_braid_length=25
number_of_braids=5000

with open("output/BraidsInvariants"+identifier+".csv", "w", newline='') as csv_file:
	writer = csv.writer(csv_file, delimiter=',')
	writer.writerow(column_names)

	jjj=1

	while jjj<=number_of_braids:
		braid=random_braid()
		braid_list=list(braid)
		if len(braid[1])<=max_braid_length:
			if len(braid[2])>2:
				input_file=open("tempfiles/braidword"+identifier+".brd", "w")
				input_file.write(braid[2])
				input_file.close()
				os.system("java -jar KnotJob/KnotJob_j8.jar tempfiles/braidword"+identifier+".brd -s0")
				if os.path.isfile("tempfiles/braidword"+identifier+".brd_s0"):
					output_file=open("tempfiles/braidword"+identifier+".brd_s0")
					lines=output_file.readlines()
					string=lines[1]
					s_invariant_string=string.split(":")[-1]
					s_invariant=int(s_invariant_string.replace(" ","").replace("\n",""))
					braid_list.append(s_invariant)
					os.remove("tempfiles/braidword"+identifier+".brd_s0")
					if len(braid_list[1])>2:
						os.system("./invariant_calculator.wls "+str(braid_list[1]).replace(" ","")+" "+identifier)
						if os.path.isfile("tempfiles/invariants"+identifier+".txt"):
							output_file_arf_sig=open("tempfiles/invariants"+identifier+".txt")
							lines_arf_sig=output_file_arf_sig.readlines()
							arf=int(lines_arf_sig[0].replace(" ","").replace("\n",""))
							signature=int(lines_arf_sig[1].replace(" ","").replace("\n",""))
							determinant=int(lines_arf_sig[2].replace(" ","").replace("\n",""))
							alexander=str(lines_arf_sig[3].replace(" ","").replace("\n","").replace("{","[").replace("}","]"))
							jones=str(lines_arf_sig[4].replace(" ","").replace("\n","").replace("{","[").replace("}","]"))
							khovanov=str(lines_arf_sig[5].replace(" ","").replace("\n","").replace("{","[").replace("}","]"))
							braid_list.append(arf)
							braid_list.append(signature)
							braid_list.append(determinant)
							braid_list.append(alexander)
							braid_list.append(jones)
							braid_list.append(khovanov)
							os.remove("tempfiles/invariants"+identifier+".txt")
					else:
						braid_list.append(0)
						braid_list.append(0)
						braid_list.append(0)
						braid_list.append(1)
						braid_list.append("[[0,1]]")
						braid_list.append("[[0, 1]]")
						braid_list.append("[[-1, 0, 1], [1, 0, 1]]")
			else:
				braid_list.append(0)
				braid_list.append(0)
				braid_list.append(0)
				braid_list.append(1)
				braid_list.append("[[0,1]]")
				braid_list.append("[[0, 1]]")
				braid_list.append("[[-1, 0, 1], [1, 0, 1]]")
			#non_slice_knots.loc[len(non_slice_knots)]=braid_list
			writer.writerow(braid_list)
			if jjj%10==0:
				print("jjj = ",jjj)
				csv_file.flush()
			jjj=jjj+1
	csv_file.flush()
        
#non_slice_knots.to_csv("output/nonslice"+identifier+".csv",index=False)
