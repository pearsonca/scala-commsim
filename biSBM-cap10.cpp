// COPYRIGHT 2014 - CABP

#include <vector>

double likelihood(vector<size_t> vertices);

// `vertices` provides type, count-of-type.
// number of types = size of `vertices`
// `vertices(i)` = count of type i

//sum over pairs of groups (a, b)
// count edges between groups a, b
// (known zero for bipartite networks if a, b groups in same type)
// time

void biSBM(vector<size_t> type_a_ends, vector<size_t> type_b_ends) {
  
}
