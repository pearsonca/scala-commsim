for lf in low mid high
do
 for pw in lo med hi
 do
  for vm in early middle late
  do
   echo "./mk_user.R $1 $2 $lf $pw $vm"
   echo "mkdir ../input/$lf-$pw-$vm-$2"
   echo "mkdir ../output/$lf-$pw-$vm-$2"
   echo "mv covert-set-*.csv ../input/$lf-$pw-$vm-$2/"
   echo "Processing $lf-$pw-$vm"
  done
 done
done
