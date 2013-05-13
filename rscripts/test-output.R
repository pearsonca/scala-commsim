library(igraph)
edgeinfo <- t(read.table("../commsim-test.txt"))
vertexinfo <- t(read.table("../commsim-test-vertex-info.txt"))
g <- graph(as.numeric(edgeinfo[-3,])+1)
g2 <- graph(as.numeric(edgeinfo[-3,])+1)
hubv <- as.numeric(vertexinfo[1,which(vertexinfo[2,]=="hub")])+1
clustervs <- as.numeric(vertexinfo[1,which(vertexinfo[2,]=="bombers")])+1
V(g)[1:(hubv-1)]$color <- "lightblue"
V(g)[clustervs]$color <- "yellow"
V(g)[hubv]$color <- "red"
E(g)$color <- "lightgrey"
E(g)[to(clustervs)]$color <- "red"
## http://lists.gnu.org/archive/html/igraph-help/2009-03/msg00003.html
g2 <- g2 - E(g2)[!to(clustervs)]
V(g2)[clustervs]$color <- "yellow"
V(g2)[hubv]$color <- "red"
l2 <- layout.auto(g2)
l<-layout.drl(g)
png(file="example_background.png",width=1000,height=1000)
par(mar=c(0,0,0,0)+0.1)
plot(g2, edge.arrow.size=0.5,
     vertex.label=NA, vertex.size=2, vertex.frame.color=NA, layout=l2)
dev.off()

###

hubInc <- read.table("../test-hub-1.txt", sep=" ", col.names=c("recipient_id","sender_id","channel_type","content","timestep"))
backInc <- read.table("../test-back-1.txt", sep=" ", col.names=c("recipient_id","sender_id","channel_type","content","timestep"))
plotInc <- read.table("../test-plot-1.txt", sep=" ", col.names=c("recipient_id","sender_id","channel_type","content","timestep"))

edges <- unique(backInc[,c("recipient_id","sender_id")])

unique(hubInc[which(hubInc$timestep < 5),"sender_id"])

sapply(1:max(hubInc$timestep),function(t) {
  length(unique(hubInc[which(hubInc$timestep < t),"sender_id"]))
})