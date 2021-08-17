#!/bin/bash
#***** NOTE: run this using: sg fslg_knot_ml "sbatch thefilename"

#SBATCH --time=72:00:00   # walltime
#SBATCH --ntasks=6   # number of processor cores (i.e. tasks)
#SBATCH --nodes=1   # number of nodes
#SBATCH --mem-per-cpu=4096M   # memory per CPU core
#SBATCH -J "Knot Table Builder"   # job name
#SBATCH --mail-user=hughesmc@gmail.com   # email address
#SBATCH --mail-type=BEGIN
#SBATCH --mail-type=END
#SBATCH --mail-type=FAIL

if [ "$(id -gn)" != "fslg_knot_ml" ]; then
    echo '*!*!*' This job is not running as the intended group. If you want to run it as fslg_knot_ml, run sbatch as follows:  sg fslg_knot_ml '"'sbatch thefilename'"'
    exit 1
fi


# Set the max number of threads to use for programs using OpenMP. Should be <= ppn. Does nothing if the program doesn't use OpenMP.
export OMP_NUM_THREADS=$SLURM_CPUS_ON_NODE

# LOAD MODULES, INSERT CODE, AND RUN YOUR PROGRAMS HERE

module load python
module load mathematica

python random_knot_generator.py
