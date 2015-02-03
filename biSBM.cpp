// Daniel Larremore
// Harvard School of Public Health
// May 20, 2014
// v1.1
//
// http://danlarremore.com/biSBM
// daniel.larremore@gmail.com
//
// biSBM - a method for community detection in bipartite networks, based on the publication:
// Efficiently inferring community structure in bipartite networks
// Daniel B. Larremore, Aaron Clauset, Abigail Z. Jacobs.
// http://arxiv.org/abs/1403.2933
// Please do not distribute without contacting the author above at daniel.larremore@gmail.com
// If a bug is located within the code, please contact the author, to correct the official version!
//
// This code is based on code written by Brian Karrer for the stochastic block model, http://arxiv.org/abs/1104.3590
// You can download that code at http://www-personal.umich.edu/~mejn/dcbm/

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <iostream>
#include <math.h>
#include <fstream>
#include <limits>
#include <sys/stat.h>
#include <unistd.h>
#include <ctime>
#include <vector>
#include "mex.h"

using namespace std;


/********** FUNCTIONS **********/
void freegraph();  // gets rid of the graph pointers at end
void GetTheNetworkEdges(double[], mwSize);
void RunKL();  // runs Kernighan-Lin once.
void Initialize();  // initializes the data structures for KL

double ComputeScore(unsigned int MaxComms, unsigned int CommStubs[],
  unsigned int CommVertices[], unsigned int EdgeMatrix[][]);
  // computes the initial score after initialization

void ComputeNeighborSet(int vertex, int option);
  // computes the neighbor set for a vertex, using either best or currentstates

double ComputeProposal(int vertex, int from, int destination);
  // computes the value of the particular change
void UpdateMatrices(int vertex, int option, int from, int destination);
  // this updates either the best
  // or current matrices based on moving the vertex from from to destination

double LogFunction(double x);  // this returns x*log(x) and zero if x= 0
void PrintResults(string);  // prints out the resulting graph for now
double ComputeVI();
double ComputeNMI();
double Entropy(int entoption);
double JointEntropy();
void biSBM(void);

// nlhs = left hand (output) size
// plhs = left hand output
// nrhs = right hand (input) size
// prhs = right hand input

/********** biSBM **********/
void mexFunction(int nlhs, mxArray *plhs[], int nrhs, const mxArray *prhs[] ) {
    srandom(time(NULL));
    /*
     * 0 edgelist
     * 1 types
     * 2 KA
     * 3 KB
     * 4 DegreeCorrect
     * 5 KLSteps
     */
    double *g;
    int KA, KB;
    double *types;
    double *edges;
    double *e;
    Types.clear();
    Comms.clear();

    /**********  0 edgelist  **********/
    size_t mrows;
    mrows = mxGetM(prhs[0]);
    e = mxGetPr(prhs[0]);
    GetTheNetworkEdges(e, mrows);


    /**********  1 types  **********/
    types = mxGetPr(prhs[1]);
    Nodes = mxGetM(prhs[1]);  // GLOBAL - Number of nodes
    int maxTypes = 0;
    for (unsigned int i = 0; i < Nodes; ++i) {
        Types.push_back(types[i]-1);  // GLOBAL - Vertex types
        maxTypes = max(maxTypes, Types[i]+1);
    }
    vector<int> tally(maxTypes+1, 0);
    for (unsigned int i = 0; i < Types.size(); ++i) {
        tally[Types[i]]++;
    }
    /**********  2 KA  **********/
    KA = (int) *mxGetPr(prhs[2]);
    /**********  3 KB  **********/
    KB = (int) *mxGetPr(prhs[3]);
    /**********  4 isDegreeCorrect  **********/
    isDegreeCorrect = (bool) *mxGetPr(prhs[4]);
    /**********  5 KLPerNetwork  **********/
    KLPerNetwork = (int) *mxGetPr(prhs[5]);

    /**********  Output  **********/
    plhs[0] = mxCreateDoubleMatrix((mwSize)Nodes, (mwSize)1, mxREAL);
    g = mxGetPr(plhs[0]);

    int counter = 0;
    vector<int> commlist;
    for (unsigned int q = 0; q < KA; ++q) {
        commlist.push_back(counter);
        counter++;
    }
    Comms.push_back(commlist);
    commlist.clear();

    for (unsigned int q = 0; q < KB; ++q) {
        commlist.push_back(counter);
        counter++;
    }
    Comms.push_back(commlist);
    commlist.clear();
    MaxComms = counter;

    mexPrintf("\nCalling biSBM with the following parameters.\n");
    mexPrintf("KA:\t%i\n", KA);
    mexPrintf("KB:\t%i\n", KB);
    mexPrintf("NA:\t%i\n", tally[0]);
    mexPrintf("NB:\t%i\n", tally[1]);
    mexPrintf("Type A Communities: ");
    for (auto thing : Comms[0]) {
        mexPrintf("%i,", thing+1);
    }
    mexPrintf("\n");
    mexPrintf("Type B Communities: ");
    for (auto thing : Comms[1]) {
        mexPrintf("%i,", thing+1);
    }
    mexPrintf("\n");
    mexPrintf("DegreeCorrect:\t%i\n", isDegreeCorrect);
    mexPrintf("Edges:\t%i\n", mrows);
    mexEvalString("drawnow;");

    /**********  Call the biSBM subroutine.  **********/
    biSBM();

    /**********  Put the output into g.  **********/
    for (unsigned int i = 0; i < Nodes; ++i) {
        g[i] = BestState[i];
    }
}

void biSBM(
  const vector<vector<unsigned int>> AdjList,
  const vector<unsigned int> Degree) {
  // TODO(cap10): const in args?

  const int constComms = 1000;
  const long int constNodes = 1000000;
  const int MAXEDGES = 10000000;  // this is the maximum number of edges

  /* empty global declarations */
  long int Nodes;
  int MaxComms;
  bool isDegreeCorrect;  // false/true - don't/do correct for the degrees
  vector<vector<int> > Comms;
  vector<int> Types;

  /* Number of random initializations (default) */
  int KLPerNetwork = 1;

  /********** GLOBAL VARIABLES **********/
  bool TrueCommsAvailable = 0;  // (default) set to 1 if passed in true comms.
  bool InitializationOption = 0;  // (default) May be changed to 1 by user.

  // FOR KL
  int CurrentState[constNodes];
  unsigned int BestState[constNodes];
  int ChangeSet[constNodes];
  int UpdateIndex[constNodes];
  int TrueState[constNodes]; // This records the true communities if they exist read in from the file

  double TwiceEdges = 0;
  double MaxScore = 0;

  int BestCommVertices[constComms];
  int BestCommStubs[constComms];
  int BestEdgeMatrix[constComms][constComms];

  int CurrentCommVertices[constComms];
  int CurrentCommStubs[constComms];
  int CurrentEdgeMatrix[constComms][constComms];

  int AdjustmentFrom[constComms];
  int AdjustmentDestination[constComms];
  int TempNeighborSet[2][constComms];  // the first entry lists the comm and the second entry lists the number of edges to that comm
  int NeighborSet[2][constComms]; // this is what we record and use
  int SelfEdgeCounter = 0; // this records self-edges to make sure that they are counted correctly
  int ActualDiffComms = 0; // this records the number of different comms in neighborhood

  // For reporting best state
  int SavedState[constNodes];
  int SavedCommVertices[constComms];
  int SavedCommStubs[constComms];
  int SavedEdgeMatrix[constComms][constComms];
  double NMIValue = 0;
  double VIValue = 0;
  double HighestScore = 0;




    HighestScore = -numeric_limits<double>::max();
    VIValue = 0;
    NMIValue = 0;
    time_t startTime = time(NULL);
    unsigned int i, j, k;
    for (j = 0; j < KLPerNetwork; j++) {
        RunKL();
        // KL,dt,L:
        mexPrintf(">%i,%f,%f\n",
          j+1, difftime(time(NULL), startTime), max(MaxScore, HighestScore));
        mexEvalString("drawnow;");
        if (MaxScore >= HighestScore) {
            HighestScore = MaxScore;
            if (TrueCommsAvailable == 1) {
                VIValue = ComputeVI();
                NMIValue = ComputeNMI();
                cout << "VI Value: " << VIValue << " NMI Value: " << NMIValue << endl;
            }
            for (i = 0; i < MaxComms; i++) {
                SavedCommVertices[i] = BestCommVertices[i];
                SavedCommStubs[i] = BestCommStubs[i];
                for (k = 0; k < MaxComms; k++) SavedEdgeMatrix[i][k] = BestEdgeMatrix[i][k];
            }
            for (i = 0; i < Nodes; i++) {
                SavedState[i] = BestState[i];
            }
        }
    }

    // because PrintResults are written for best values we copy them
    // back over from the saved values which are the best ones.
    for (i = 0; i < MaxComms; i++) {
        BestCommVertices[i] = SavedCommVertices[i];
        BestCommStubs[i] = SavedCommStubs[i];
        for (k= 0; k < MaxComms; k++)
            BestEdgeMatrix[i][k] = SavedEdgeMatrix[i][k];
    }
    for (i = 0; i < Nodes; i++) {
        BestState[i] = SavedState[i];
    }
    cout << "Final Score: " << ComputeScore(MaxComms,
      BestCommStubs, BestCommVertices, BestEdgeMatrix[][]) << endl;

    freegraph();
}

// MATLAB edges arrive as merged list
vector<vector<unsigned int>> parseEdgeList(double *e, mwSize mrows) {
  unsigned int Nodes = 5;  // TODO(cap10): should be able to max on *e
  // TODO(cap10): slice e into e[0:mrows-1], e[mrows:2*mrows-1]
  // TODO(cap10): bulk down cast to unsigned ints
  vector<vector<unsigned int>> res(2, vector<unsigned int>(Nodes));
  res[0] = e0;
  res[1] = e1;
  return res;
}

void GetTheNetworkEdges(double *e, mwSize mrows) {
    // e [edges for type 0, edges for type 1]
    unsigned int counter = 0;
    mwSize ii;
    for (ii = 0; ii < mrows; ++ii) {
        EdgeList[ii][0] = (long int)e[ii]-1;
        EdgeList[ii][1] = (long int)e[mrows+ii]-1;
        counter = counter+1;
        Nodes = max(Nodes,(long int)e[ii]);
        Nodes = max(Nodes,(long int)e[mrows+ii]);
    }
    TwiceEdges = 2*counter;  // GLOBAL

    // if (TrueCommsAvailable == 1) {
        // DBL
        //        InputFile2.open(trueCommsName.c_str());
        //        if (!InputFile2)
        //        {
        //            cout << "Error in opening file";
        //            cin.get();
        //            return;
        //        }
        //
        //        for (i = 0; i < Nodes; i++)
        //            TrueState[i] = -1;
        //
        //        while(std::getline(InputFile2, lineread)) // Read line by line
        //        {
        //            buffer = new char [lineread.size()+1];
        //            strcpy(buffer, lineread.c_str());
        //            //  cout << buffer << endl;
        //            sscanf(buffer, "%ld", &entry1);
        //            //  sscanf(buffer, "n%d,%*[^,],%d", &ignore, &entry1); //%*s
        //            // entry1 = entry1+1;
        //            //sscanf(buffer, "%d %d", &entry1, &entry2);
        //            // TrueState[entry1-1] = entry2;
        //            TrueState[counter2] = entry1;
        //
        //            counter2 = counter2+1;
        //            delete[] buffer;
        //        }
        //        InputFile2.close();
        //
        //        for (i = 0; i < Nodes; i++)
        //        {
        //            if ((TrueState[i] == -1) || (TrueState[i] >= MaxComms))
        //            {
        //                cout << "STOP A VERTEX WAS NOT LABELED OR WAS LABELED INCORRECTLY." << TrueState[i] << " "  << i << endl;
        //                cin.get();
        //            }
        //        }
    // }
    // We start the degree values and LastEmpty all at zero
    for (unsigned int i = 0; i < Nodes; ++i) {
        Degree[i] = 0;
        LastEmpty[i] = 0;
    }
    // First we count the degrees by scanning through the list once
    for (unsigned int i = 0; i < counter; ++i) {
        Degree[EdgeList[i][0]]++;
        Degree[EdgeList[i][1]]++;
    }
    // Now we make space in the adjacency lists
    for (unsigned int i = 0; i < Nodes; ++i) {
        AdjList[i] = new long int [Degree[i]];
    }
    // Now we read the edges into the adjacency lists utilizing
    // lastempty to put them into the proper spots
    for (unsigned int i = 0; i < counter; ++i) {
        AdjList[EdgeList[i][0]][LastEmpty[EdgeList[i][0]]] = EdgeList[i][1];
        LastEmpty[EdgeList[i][0]]++;

        AdjList[EdgeList[i][1]][LastEmpty[EdgeList[i][1]]] = EdgeList[i][0];
        LastEmpty[EdgeList[i][1]]++;
    }
    return;
}

// This function deletes the graph from memory.
void freegraph() {
    long int i;//-Wunused,j;

    for (i = 0; i < Nodes; i++)
        delete [] AdjList[i];

    return;
}

void RunKL() {
    int i, j, k;
    int MaxIndex = 1;
    double CurrentScore;  // records the current log-likelihood
    int MaxVertex;  // this records the index of the largest vertex ratio found so far
    double MaxRatio;  // records the value of the ratio, actually it's the log of the ratio
    int MaxPriority; // records the community that the vertex wants to go to
    long int tempvertex = 0;

    double prevMaxScore = -numeric_limits<double>::max();
    long double tolerance = 0.00000001;
    // this prevents loops due to numerical errors.

    double ProposalRatio;
    double value;
    int Priority;

    Initialize();

    // This returns the log of the initial score
    MaxScore = ComputeScore(MaxComms, BestCommStubs, BestCommVertices, BestEdgeMatrix);
    int iter = 0;
    int maxIter = 100;
    while (MaxScore >= prevMaxScore + tolerance && iter < maxIter) {
        iter++;
        // cout << "MAX SCORE IS: " << MaxScore << endl;
        // we start with everything equal to the best values
        CurrentScore = MaxScore;
        prevMaxScore = MaxScore;
        MaxIndex = -1;

        // ChangeSet records which vertices are able to move, in that they haven't
        // already moved during this KL step.  Update index will tell when the vertex
        // was chosen to move.
        for (i = 0; i < Nodes; i++) {
            CurrentState[i] = BestState[i];
            ChangeSet[i] = i;
            UpdateIndex[i] = -1;
        }

        for (i = 0; i < MaxComms; i++) {
            CurrentCommVertices[i] = BestCommVertices[i];
            CurrentCommStubs[i] = BestCommStubs[i];
            for (j= 0; j < MaxComms; j++)
                CurrentEdgeMatrix[i][j] = BestEdgeMatrix[i][j];
        }

        // This loop moves each vertex once
        // Note that we DONT reinitialize changeset as this is unnecessary
        // This would make it a factor of 2 slower.
        for (i = 0; i < Nodes; i++)
        {
            MaxVertex = 0;
            MaxRatio = -numeric_limits<double>::max( );
            MaxPriority = 0;
            // This loop selects which vertex to move
            for (j= 0; j < Nodes-i; j++)
            {
                // get proposal and proposal ratio for ChangeSet[j]
                Priority = 0;
                ProposalRatio = -numeric_limits<double>::max( );
                // we first compute the neighbor set of the vertex, this is fixed
                // and the same for every change,
                // computing this first makes this more efficient
                // zero indicates run with current communities
                ComputeNeighborSet(ChangeSet[j], CurrentState);

                // DanLarremore Modification:
                // We actually don't want to try all the comms, because some of them are forbidden.
                // We only get to choose from the entries of Comms[Types[j]].

                for (unsigned int q= 0; q < Comms[Types[ChangeSet[j]]].size(); ++q)
                {
                    k = Comms[Types[ChangeSet[j]]][q];
                    // we compute the value of vertex ChangeSet[j] going to k
                    // we DONT allow a vertex to remain where it was
                    // This is essential to enforce so that it will go downhill and not be greedy
                    if (k != CurrentState[ChangeSet[j]])
                    {
                        value = ComputeProposal(ChangeSet[j], CurrentState[ChangeSet[j]], k);
                        if (value > ProposalRatio)
                        {
                            Priority = k;
                            ProposalRatio = value;
                        }
                    }
                }

                // check whether its higher than what you already have as the max KL move
                if (ProposalRatio > MaxRatio)
                {
                    MaxVertex = j;  // Note this is not the vertex j, but the vertex given by ChangeSet[j]
                    MaxRatio = ProposalRatio;
                    MaxPriority = Priority;
                }
            }
            // now we move it, first recording the current neighbors so that
            // we can update the matrices properly
            ComputeNeighborSet(ChangeSet[MaxVertex], CurrentState);
            // This updates the matrices to represent the vertices new state
            UpdateMatrices(ChangeSet[MaxVertex], 0, CurrentState[ChangeSet[MaxVertex]], MaxPriority);
            CurrentState[ChangeSet[MaxVertex]] = MaxPriority;
            // we are using logs so we add the maxratio to the current score for the new score
            CurrentScore = CurrentScore + MaxRatio;
            UpdateIndex[ChangeSet[MaxVertex]] = i;
            // we switch it with the last element of changeset, removing it from further consideration
            // until we have moved the other vertices
            tempvertex = ChangeSet[MaxVertex];
            ChangeSet[MaxVertex] = ChangeSet[Nodes-i-1];
            ChangeSet[Nodes-i-1] = tempvertex;

            // now if the new state is better than the previous best state we record this
            // MaxIndex in combination with UpdateIndex
            // telling us where we are in the movement of vertices
            if (CurrentScore > MaxScore) {
                MaxScore = CurrentScore;
                MaxIndex = i;
            }
        }


        // now we update BestState if a change resulted in a higher maximum
        // by implementing all the changes found above

        // There is a potential for speeding this up here.
        if (MaxIndex != -1) {
            for (i = 0; i < Nodes; i++) {
                // we only make the changes to beststate that happened before
                // or equal to maxindex no other vertex is updated
                // fortunately the update order is irrelevant to the final result so
                // we can just do it this way
                // Because we force all moves to be different, these updates are all a switch of community
                if (UpdateIndex[i] <= MaxIndex) {
                    // the option 1 does update corresponding to the best states / matrices
                    ComputeNeighborSet(i, BestState);
                    UpdateMatrices(i, 1, BestState[i], CurrentState[i]);
                    // 1 does best matrix update
                    BestState[i] = CurrentState[i];
                }
            }
        }

    }
    if (iter == maxIter) {
        cout << "...maxIterations on this KL run." << endl;
    }

    return;
}

// This starts off from a random initial condition
void Initialize() {
    int i, j;
    int neighbor;
    int sum;

    for (i = 0; i < MaxComms; i++) {
        BestCommVertices[i] = 0;
        BestCommStubs[i] = 0;
        for (j = 0; j < MaxComms; j++) {
            BestEdgeMatrix[i][j] = 0;
        }
    }

    for (i = 0; i < Nodes; i++) {
        // BestState[i] = int(numgen.nextDouble(MaxComms));
        // REPLACERNG, should return 0 to MaxComms-1 in integer
        // DanLarremore Modification:
        // The initialized communities must be constrained to respect types.
        // cout << i << "," << Types[i] << endl;
        BestState[i] = Comms[Types[i]][0] + (random() % Comms[Types[i]].size() );

        if (InitializationOption == 1)
            BestState[i] = TrueState[i];
        BestCommVertices[BestState[i]]++;
        BestCommStubs[BestState[i]] += Degree[i];
    }

    // We are going to double count all edges and then divide two
    for (i = 0; i < Nodes; i++) {
        for (j = 0; j < Degree[i]; j++) {
            neighbor = AdjList[i][j];
            BestEdgeMatrix[BestState[i]][BestState[neighbor]]++;
            // the following statement prevents us from quadruple counting same comm edges.
            if (BestState[neighbor] != BestState[i])
                BestEdgeMatrix[BestState[neighbor]][BestState[i]]++;
        }
    }

    sum = 0;
    // we get rid of the double-counting
    for (i = 0; i < MaxComms; i++)
    {
        for (j= 0; j < MaxComms; j++)
        {
            BestEdgeMatrix[i][j] = BestEdgeMatrix[i][j]/2;
            if (i != j)
                sum += BestEdgeMatrix[i][j];
            if (i == j)
                sum += 2*BestEdgeMatrix[i][i];
        }
    }

    return;
}

double ComputeScore(
  unsigned int MaxComms,
  unsigned int CommStubs[],
  unsigned int CommVertices[],
  unsigned int EdgeMatrix[][]) {
    // For the running of the KL algorithm itself this does not matter as all
    // we use are whether the score increases
    // We will want this when we compare different initializations

    // this actually returns 1/2 the unnormalized log-likelihood listed in the paper

    double sum = -(isDegreeCorrect ?
      accumulate(CommStubs, CommStubs + MaxComms, 0, accLogFunction) :
      inner_product(
        CommStubs, CommStubs + MaxComms,
        CommVertices, CommVertices + MaxComms,
        std::plus(), [] (stub, v) { return stub*log(v) }));

    for (unsigned int i = 0; i < MaxComms-1; i++) {
        sum += .5*LogFunction(2*EdgeMatrix[i][i]);
        sum += accumulate(
          EdgeMatrix[i] + i + 1,
          EdgeMatrix[i] + MaxComms, 0, accLogFunction);
    }
    sum += .5*LogFunction(2*EdgeMatrix[MaxComms-1][MaxComms-1])

    return sum;
}

// We compute this using the current comm matrices
// We avoid the potential pitfalls of huge intermediate numbers by adding logs together.  So we treat 0 log 0 as 0.
// We return 0 for degree zero vertices (which really shouldn't be sent into the program
// in the first place.)
// We also return 0 for from = destination cause there is no change then.
// Here we use base e.  It returns the log of the actual value.
// Again this is half of the change in the unnormalized log-likelihood listed in the paper
double ComputeProposal(int degree, int from, int destination) {
    int i;  // -Wunused, j, k;
    double ratiovalue = 0;
    int fromcount = 0;
    int destcount = 0;

    double help1;
    double help2;
    double help3;

    if (from == destination)
        return 0;

    // if the degree of the vertex is zero we know nothing about it
    // in this case we don't ever change its community
    // at the end we put all degree zeroes into their own group
    if (isDegreeCorrect && degree == 0) {
        return 0;
    }

    // we first add up all the cross-terms (between communities that are not from / destination)
    for (i = 0; i < ActualDiffComms; i++) {
        // we lost NeighborSet[1][i] edges to NeighborSet[0][i] from the from comm
        // we gain the same amount in the destination comm
        // IFF the comms were not from and destination
        if ((NeighborSet[0][i] != from) && (NeighborSet[0][i] != destination)) {
            // do update NOTE: each community mcc' gets updated once if it had edges switch out
            // which is correct, remembering that mcc' is symmetric and we only count c < c' here

            help1 = static_cast<double>(CurrentEdgeMatrix[from][NeighborSet[0][i]]);
            help2 = static_cast<double>(CurrentEdgeMatrix[destination][NeighborSet[0][i]]);
            help3 = static_cast<double>(NeighborSet[1][i]);

            ratiovalue += LogFunction(help1-help3) - LogFunction(help1);
            ratiovalue += LogFunction(help2+help3) - LogFunction(help2);
        }

        if (NeighborSet[0][i] == from)
            fromcount = NeighborSet[1][i];

        if (NeighborSet[0][i] == destination)
            destcount = NeighborSet[1][i];
    }

    // now we add in the term corresponding to from / dest
    help1 = static_cast<double>(CurrentEdgeMatrix[from][destination]);
    help2 = static_cast<double>(fromcount-destcount);
    ratiovalue += LogFunction(help1 + help2) - LogFunction(help1);

    // now we add in the terms corresponding to from
    help1 = static_cast<double>(CurrentCommStubs[from]);
    help2 = static_cast<double>(degree);
    if (isDegreeCorrect) {
        ratiovalue += -LogFunction(help1 - help2) + LogFunction(help1);
    } else {
        if (help1 - help2 != 0)
            ratiovalue += -(help1-help2)*log(static_cast<double>(CurrentCommVertices[from]-1));
        if (help1 != 0)
            ratiovalue += help1*log(static_cast<double>(CurrentCommVertices[from]));
    }

    // now we do from/from
    help1 = static_cast<double>(2*CurrentEdgeMatrix[from][from]);
    help2 = static_cast<double>(2*SelfEdgeCounter + 2*fromcount);
    ratiovalue += .5*LogFunction(help1 - help2) - .5*LogFunction(help1);

    // now we add in the terms corresponding to dest
    help1 = static_cast<double>(CurrentCommStubs[destination]);
    help2 = static_cast<double>(degree);
    if (isDegreeCorrect) {
        ratiovalue += -LogFunction(help1 + help2) + LogFunction(help1);
    } else {
        if (help1 + help2 != 0)
            ratiovalue += -(help1+help2)*log(static_cast<double>(CurrentCommVertices[destination]+1));
        if (help1 != 0)
            ratiovalue += help1*log(static_cast<double>(CurrentCommVertices[destination]));
    }

    // and now dest/dest
    help1 = static_cast<double>(2*CurrentEdgeMatrix[destination][destination]);
    help2 = static_cast<double>(2*SelfEdgeCounter + 2*destcount);
    ratiovalue += .5*LogFunction(help1 + help2) - .5*LogFunction(help1);

    return ratiovalue;
}

unsigned int ComputePotentialNeighboors(
  unsigned int VertexAdjList[], unsigned int vertex, unsigned int RefState[],
  unsigned int TempNeighborSet[][]
) {
  unsigned int SelfEdgeCounter = 0;

  for (unsigned int i = 0; i < MaxComms; i++) {
      TempNeighborSet[1][i] = 0;
  }

  // NOTE SINCE A SELF-EDGE SHOWS UP TWICE IN THE ADJLIST THIS DOUBLE
  // COUNTS THESE EDGES, WE RECORD THE NUMBER OF TIMES THIS HAPPENS
  // IN A SEPARATE VARIABLE AND THEN DIVIDE BY TWO
  for (auto neighbor : VertexAdjList) {
      if (neighbor != vertex) {
        TempNeighborSet[1][RefState[neighbor]]++;
      } else {
        SelfEdgeCounter++;
      }
  }

  SelfEdgeCounter = SelfEdgeCounter/2;

  return SelfEdgeCounter;
}

void ComputeNeighborSet(int vertex, unsigned int RefState[]) {
  // option = 0, current state, 1 = BestState
    int i;  // -Wunused,j;
    int neighbor;

    for (i = 0; i < MaxComms; i++) {
        NeighborSet[0][i] = i;
        NeighborSet[1][i] = 0;
    }

    TempNeighborSet = NeighborSet.clone();  // TODO(cap10): actually implement

    SelfEdgeCounter =
      ComputePotentialNeighboors(AdjList[vertex], vertex, RefState[], TempNeighborSet)

    ActualDiffComms = 0;
    for (i = 0; i < MaxComms; i++) {
        if (TempNeighborSet[1][i] != 0) {
            NeighborSet[1][ActualDiffComms] = TempNeighborSet[1][i];
            ActualDiffComms++;
        }
    }

    return;
}

void UpdateMatrices(
    unsigned int vertex, unsigned int from, unsigned int destination, unsigned int ActualDiffComms,
    unsigned int Degree[], unsigned int NeighborSet[][], unsigned int SelfEdgeCounter,
    unsigned int TargetEdgeMatrix[][], unsigned int TargetCommVertices[], unsigned int TargetCommStubs[])
{
    // opt 0 = current, 1 = best
    unsigned int fromcount = 0;
    unsigned int destcount = 0;

    TargetCommVertices[from]--;
    TargetCommVertices[destination]++;
    TargetCommStubs[from] -= Degree[vertex];
    TargetCommStubs[destination] += Degree[vertex];

    for (unsigned int i = 0; i < ActualDiffComms; i++) {
        if ((NeighborSet[0][i] != from) && (NeighborSet[0][i] != destination)) {
            // do update NOTE: each community mcc' gets updated once if it had edges switch out
            // which is correct, remembering that mcc' is symmetric and we only count c < c' here
            TargetEdgeMatrix[from][NeighborSet[0][i]] -= NeighborSet[1][i];
            TargetEdgeMatrix[NeighborSet[0][i]][from] -= NeighborSet[1][i];

            TargetEdgeMatrix[destination][NeighborSet[0][i]] += NeighborSet[1][i];
            TargetEdgeMatrix[NeighborSet[0][i]][destination] += NeighborSet[1][i];
        }

        if (NeighborSet[0][i] == from)
            fromcount = NeighborSet[1][i];

        if (NeighborSet[0][i] == destination)
            destcount = NeighborSet[1][i];
    }

    TargetEdgeMatrix[from][from] -= (SelfEdgeCounter + fromcount);
    TargetEdgeMatrix[destination][destination] += (SelfEdgeCounter + destcount);
    TargetEdgeMatrix[from][destination] += (fromcount - destcount);
    TargetEdgeMatrix[destination][from] += (fromcount - destcount);

    return;
}

// This function returns zero if x = 0, otherwise it returns x*log(x)
double LogFunction(double x) {
    assert(x >= 0);
    return (x == 0) ? 0 : x*log(x);
}

double accLogFunction(double acc, double x) {
  return acc + LogFunction(x)
}

// We do not normalize VI here.
double ComputeVI(unsigned int MaxComms, unsigned int Nodes, unsigned int BestState[], unsigned int TrueState[])
{
    double EntropyA = Entropy(MaxComms, Nodes, BestState);
    double EntropyB = Entropy(MaxComms, Nodes, TrueState);
    double EntropyAB = JointEntropy(MaxComms, Nodes, BestState, TrueState);

    return 2*EntropyAB-EntropyA-EntropyB;
}

double ComputeNMI(unsigned int MaxComms, unsigned int Nodes, unsigned int BestState[], unsigned int TrueState[])
{
    double EntropyA = Entropy(MaxComms, Nodes, BestState);
    double EntropyB = Entropy(MaxComms, Nodes, TrueState);
    double EntropyAB = JointEntropy(MaxComms, Nodes, BestState, TrueState);

    return 2*(EntropyA+EntropyB-EntropyAB)/(EntropyA+EntropyB);
}

double Entropy(unsigned int MaxComms, unsigned int Nodes, unsigned int RefState[])
{
    double Ent = 0;

    int i, j;//-Wunused, k;
    double Ni[MaxComms] = { 0 };

    // RefState should be BestState or TrueState
    for (j= 0; j < Nodes; j++) Ni[RefState[j]]++;

    for (i = 0; i < MaxComms; i++)
    {
        if (Ni[i] != 0)
        {
            Ent = Ent - LogFunction(Ni[i]/double(Nodes));
        }
    }

    // NOTE WE RETURN THE ENTROPY IN LOG BASE 2
    Ent = Ent / log(2)

    return Ent;
}

// Calculates the joint entropy
double JointEntropy(unsigned int MaxComms, unsigned int Nodes, unsigned int BestState[], unsigned int TrueState[])
{
    unsigned int i, j, l;
    double Nij[MaxComms][MaxComms] = { 0 };
    double JointEnt = 0;

    for (l= 0; l < Nodes; l++)
    {
        Nij[BestState[l]][TrueState[l]]++;
    }

    for (i = 0; i < MaxComms; i++)
    {
        for (j = 0; j < MaxComms; j++)
        {
            if (Nij[i][j] != 0)
            {
                JointEnt = JointEnt - LogFunction(Nij[i][j]/double(Nodes));
            }
        }
    }

    // divide by log 2 to convert to base 2.
    JointEnt = JointEnt / log(2);

    return JointEnt;
}
