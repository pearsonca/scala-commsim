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

plotFiles <- sort(list.files(".","plot-\\d+\\.txt"))
backFiles <- sort(list.files(".","back-\\d+\\.txt"))
hubFiles <- sort(list.files(".","hub-\\d+\\.txt"))

mapply(function(p,b,h) { 
  srcs<-lapply(list(plot=p,back=b,hub=h), read.table, sep=" ", col.names=c("recipient_id","sender_id","channel_type","content","timestep"))  
  hubData<-srcs$hub
  backData<-srcs$back
  plotData<-srcs$plot
  urows <- row.names(unique(backData[,c("recipient_id","sender_id")])) ## get the rows from source data for those
  
  length(urows)
}, plotFiles, backFiles, hubFiles, USE.NAMES=F)

hubInc <- read.table("../test-hub-1.txt", sep=" ", col.names=c("recipient_id","sender_id","channel_type","content","timestep"))
backInc <- read.table("./test-back-98.txt", sep=" ", col.names=c("recipient_id","sender_id","channel_type","content","timestep"))
plotInc <- read.table("../test-plot-1.txt", sep=" ", col.names=c("recipient_id","sender_id","channel_type","content","timestep"))

urows <- row.names(unique(backInc[,c("recipient_id","sender_id")])) ## get the rows from source data for those

## make a matrix A = rows(recipients), cols(timestep)

## make a matrix B = rows(senders), cols(timestep) : NB, senders can include hub
subset <- backInc[urows,]
test_r<-Vectorize(function(recipient_id, timestep) {
  sum( (subset$recipient_id == (recipient_id-1)) & (subset$timestep <= timestep))
})
IN<-outer(1:max(backInc$recipient_id)+1,1:max(subset$timestep),test_r)
test_s<-Vectorize(function(sender_id, timestep) {
  sum( (subset$sender_id == (sender_id-1)) & (subset$timestep <= timestep))
})
OUT<-outer(1:max(backInc$recipient_id)+1,1:max(subset$timestep),test_s) # max sender = H
INOUT <- IN + OUT
# maxOUT <- apply(OUT,2,max)
# maxIN <- apply(IN,2,max)
maxINOUT <- apply(INOUT, 2, max)
Hid <- hubInc[1,"recipient_id"]+1
Hin <- hubInc[row.names(unique(hubInc[,c("recipient_id","sender_id")])),"timestep"] 
HinT <- sapply(1:max(Hin),function(t) { sum(Hin <= t) })
Hlast <- HinT[length(HinT)]
HinT <- c(HinT,rep.int(Hlast,length(OUT[1,])-length(HinT)))


ploturows <- row.names(unique(plotInc[,c("recipient_id","sender_id")])) ## get the rows from source data for those
plotsubset <- plotInc[ploturows,]
first_plot_id <- min(plotInc$recipient_id)
last_plot_id <- max(plotInc$recipient_id)
test_r_plot<-Vectorize(function(recipient_id, timestep) {
  sum( (plotsubset$recipient_id == recipient_id) & (plotsubset$timestep <= timestep))
})

test_s_plot<-Vectorize(function(sender_id, timestep) {
  sum( (plotsubset$sender_id == sender_id) & (plotsubset$timestep <= timestep))
})

Pin <- outer(first_plot_id:last_plot_id, 1:max(plotsubset$timestep), test_r_plot)
Pout <- outer(first_plot_id:last_plot_id, 1:max(plotsubset$timestep), test_s_plot)

Hout <- mapply(test_s, sender_id=rep.int(Hid,max(subset$timestep)), timestep=1:max(subset$timestep))
Hout <- c(Hout, rep.int(Hout[length(Hout)],max(plotsubset$timestep)-max(subset$timestep)))
Hout <- Hout+ mapply(test_s_plot, sender_id=rep.int(Hid-1,max(plotsubset$timestep)), timestep=1:max(plotsubset$timestep))

## baseline TPR / FPR based on H has highest in-out degree
TPR <- ifelse(maxINOUT < Hout + HinT,1,0)
FPR <- ifelse(TPR == 1, 0, 1)
