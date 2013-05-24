library(igraph)
library(animation)
ani.options(ffmpeg="ffmpeg")
pree <- read.table("./1-EL.txt",col.names=c("sender_id","recipient_id","type"))
pree[which(pree[,1]<0),1] <- -pree[which(pree[,1]<0),1]
pree[which(pree[,2]<0),2] <- -pree[which(pree[,2]<0),2]
edgeinfo <- t(pree[,-3])
vertexinfo <- read.table("./1-VI.txt",sep=" ", col.names=c("id","type"))
g <- graph( edgeinfo )
#degree(g,v=which(vertexinfo[2,] == "hub"))
#mean(degree(g))

#g2 <- graph(as.numeric(edgeinfo[-3,])+1)
hubv <- which(vertexinfo[,"V2"]=="hub")
clustervs <- -vertexinfo[which(vertexinfo[,"V2"]=="bombers"),"V1"]
plotter_ids <- c(hubv, vertexinfo[which(vertexinfo[,"V2"]=="plotter"),"V1"])

V(g)$size <- 2
V(g)[1:(hubv-1)]$color <- "lightblue"
V(g)[clustervs]$color <- "khaki"
V(g)[hubv]$color <- "red"
# low <- 0.001
# E(g)[to(clustervs)]$weight <- low
# E(g)[from(clustervs)]$weight <- low
# E(g)[to(clustervs)]$color <- "red"
# E(g)[to(clustervs)]$lty <- 4
## http://lists.gnu.org/archive/html/igraph-help/2009-03/msg00003.html
# g2 <- g2 - E(g2)[!to(clustervs)]
# V(g2)[clustervs]$color <- "yellow"
# V(g2)[hubv]$color <- "red"
#l2 <- layout.auto(g2)
l<-layout.auto(g)
png("testoverlay.png")
plot(g, edge.arrow.size=0.1, vertex.size=2, vertex.label=NA, vertex.frame.color=NA, layout=l)
dev.off()
hubInc <- read.table("./4-1-hub-1.txt", sep=" ", col.names=c("recipient_id","sender_id","channel_type","content","timestep"))
backInc <- read.table("./4-1-back-1.txt", sep=" ", col.names=c("recipient_id","sender_id","channel_type","content","timestep"))
plotInc <- read.table("./4-1-plot-1.txt", sep=" ", col.names=c("recipient_id","sender_id","channel_type","content","timestep"))

allData <- rbind( 
  hubInc[,cols], 
  backInc[,cols], 
  plotInc[,cols]
)
par(mar=c(0,0,0,0)+0.1)

graphStep<-function(t) {
  gref <- graph(t(subset(allData, allData$timestep == t, select=c("sender_id","recipient_id"))[,c("sender_id","recipient_id")]))
  plot(gref, edge.arrow.size=0.1, vertex.size=2, vertex.label=NA, vertex.frame.color=NA, layout=l)
}

saveVideo({
  ani.options(interval=0.2)
  sapply(1:100,graphStep)
}, video.name = "test.mp4", clean=T)