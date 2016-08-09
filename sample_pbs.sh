cat <<EOF
#!/bin/bash
#PBS -r n
#PBS -N $1
#PBS -o $1.o
#PBS -e $1.err
#PBS -m a
#PBS -M cap10@ufl.edu
#PBS -l walltime=00:30:00
#PBS -l nodes=1:ppn=1
#PBS -l pmem=1gb
#PBS -t 1-$3

module load gcc/5.2.0 R/3.2.2
cd /ufrc/singer/cap10/scala-commsim
tar=\$(printf 'input/digest/covert/$2/%03d.csv' \$PBS_ARRAYID)
make \$tar
EOF