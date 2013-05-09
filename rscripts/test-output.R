library(igraph)
edgeinfo <- t(read.table("../commsim-test.txt"))
g <- graph(as.numeric(edgeinfo[-3,])+1)
png(file="example_background.png",width=1000,height=1000)
par(mar=c(0,0,0,0)+0.1)
plot(g, edge.arrow.size=0.5,
     vertex.label=NA, vertex.size=2, vertex.frame.color=NA)
dev.off()