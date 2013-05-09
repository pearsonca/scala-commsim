library(igraph)
edgeinfo <- t(read.table("../commsim-test.txt"))
vertexinfo <- t(read.table("../commsim-test-vertex-info.txt"))
g <- graph(as.numeric(edgeinfo[-3,])+1)
V(g)[as.numeric(vertexinfo[1,which(vertexinfo[2,]=="plotter")])+1]$color <- "yellow"
V(g)[as.numeric(vertexinfo[1,which(vertexinfo[2,]=="hub")])+1]$color <- "red"
png(file="example_background.png",width=1000,height=1000)
par(mar=c(0,0,0,0)+0.1)
plot(g, edge.arrow.size=0.5,
     vertex.label=NA, vertex.size=2, vertex.frame.color=NA)
dev.off()