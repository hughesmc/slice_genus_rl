#!/bin/bash

#SBATCH --time=72:00:00   # walltime
#SBATCH --ntasks=16   # number of processor cores (i.e. tasks)
#SBATCH --gpus=1
#SBATCH --mem-per-cpu=1024M   # memory per CPU core
#SBATCH -J "Piccirillo_Manolescu_K4"   # job name
#SBATCH --mail-user=hughesmc@gmail.com   # email address
#SBATCH --mail-type=BEGIN
#SBATCH --mail-type=END
#SBATCH --mail-type=FAIL


# Set the max number of threads to use for programs using OpenMP. Should be <= ppn. Does nothing if the program doesn't use OpenMP.
export OMP_NUM_THREADS=$SLURM_CPUS_ON_NODE

# LOAD MODULES, INSERT CODE, AND RUN YOUR PROGRAMS HERE

module load miniconda3/4.9

source activate RL_env

module load python/3.7

module load python-tensorflow/2.0

python training_script.py K4

