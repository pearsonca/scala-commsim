cat <<EOF
#!/bin/bash
#PBS -r n
#PBS -N $1
#PBS -o $1.o
#PBS -e $1.err
#PBS -m a
#PBS -M cap10@ufl.edu
#PBS -l walltime=01:00:00
#PBS -l nodes=1:ppn=1
#PBS -l pmem=1gb
#PBS -t 1-$3

module load gcc/5.2.0 R/3.2.2 java/1.8.0_31 scala
cd /scratch/lfs/cap10/scala-commsim
tar=\$(printf 'input/simulate/covert/$2/%03d/cc.csv' \$PBS_ARRAYID)
make \$tar
EOF
