library(igraph)
edgeinfo <- t(read.table("../commsim-test.txt")[,-3])
g <- graph(edgeinfo+1)
plot(g, edge.arrow.size=0.2)