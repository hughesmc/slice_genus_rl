
# coding: utf-8

# In[2]:

from BandEnvironment import BandEnv
import numpy as np


def BandRandomBraid(band_decomp_stdev=2,braid_index_stdev=5,max_braid_index=12,set_braid_index=12,max_num_bands=20,fixed_braid_index=False):
	# Select the length of the band decomposition, by taking the smaller of the following two quantities:  the max_num_bands, 
	# and a random normal variable, with mean 0 and standard deviation band_decomp_stdev, which we take the absolute 
	# value of, followed by the ceiling of.  
    braid_word_length=np.minimum(np.ceil(np.abs(np.random.normal(0,band_decomp_stdev))).astype(int),max_num_bands)
    # Select the braid index (not the actual braid index, but the range of crossings we can select), by taking the 
    # minimum of the following two quantities:  max_braid_index-1, and a random normal variable, with mean 0 and 
    # standard deviation braid_index_stdev, which we take the absolute value of, then the ceiling of.  The quantity 
    # braid_index is then chosen to be the resulting maximum, or 2, whichever is smaller.
    #
    # Notice this is less than max_braid_index, because this is meant to be used to pick the possible crossings; so it
    # for example max_braid_index is 12, then this will not be larger than 11, so that no crossings higher than 11 will
    # show up.  It shouldn't be called braid_index I guess, so I'll change it at some point.
    if fixed_braid_index:
        braid_index=set_braid_index
    else:
        braid_index=np.maximum(np.minimum(np.ceil(np.abs(np.random.normal(0,braid_index_stdev))).astype(int),max_braid_index-1),2)
    # Create a list of all of the possible crossings given our choice of braid index.
    possible_crossings=np.delete(np.arange(-braid_index,braid_index+1,1),braid_index)
    # Select the braid word using uniform probabilities.
    word=np.random.choice(possible_crossings,braid_word_length-1)
    # Create the braid using the parameters computed above.
    braid=BandEnv(word,braid_index,max_num_bands)
    return braid