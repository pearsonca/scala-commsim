library(igraph)
pree <- read.table("../1-EL.txt",col.names=c("sender_id","recipient_id","type"))
pree[which(pree[,1]<0),1] <- -pree[which(pree[,1]<0),1]
pree[which(pree[,2]<0),2] <- -pree[which(pree[,2]<0),2]
edgeinfo <- t(pree[,-3])
vertexinfo <- read.table("../1-VI.txt",sep=" ", col.names=c("id","type"))
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
png(file="4_1_500.png",width=1000,height=1000)
par(mar=c(0,0,0,0)+0.1)
plot(g, edge.arrow.size=0.5,
     vertex.label=NA, vertex.frame.color=NA, layout=l)
dev.off()

###

plotFiles <- sort(list.files(".","plot-\\d+\\.txt"))
backFiles <- sort(list.files(".","back-\\d+\\.txt"))
hubFiles <- sort(list.files(".","hub-\\d+\\.txt"))
viFiles <- sort(list.files(".","\\d+-VI\\.txt"))
elFiles <- sort(list.files(".","\\d+-EL\\.txt"))

# indegrees <- function(messages,maxid) {
#   sapply(1:maxid, function(id) { dim(unique(subset(allmessages, allmessages$recipient_id == id, select = "sender_id")))[1] })
# }
# 
# outdegrees <- function(messages,maxid) {
#   sapply(1:maxid, function(id) { dim(unique(subset(allmessages, allmessages$sender_id == id, select = "recipient_id")))[1] })
# }

bothdegrees <- function(messages,maxid) {
  sapply(1:maxid, function(id) { dim(unique(subset(messages, messages$recipient_id == id, select = "sender_id")))[1] + dim(unique(subset(messages, messages$sender_id == id, select = "recipient_id")))[1] })
}
hasbad <- Vectorize(function(id,badmessages) {
  any(badmessages$recipient_id == id | badmessages$sender_id == id)
}, vectorize.args = "id", SIMPLIFY="array")

cols <- c("recipient_id","sender_id","timestep","content")
mapply(function(p,b,h,vi,id) { 
  srcs<-lapply(list(plot=p,back=b,hub=h), read.table, sep=" ", 
               col.names=c("recipient_id","sender_id","channel_type","content","timestep"))
  vertexinfo <- read.table(vi, sep=" ", col.names=c("id","type"))
  people <- subset(vertexinfo, vertexinfo$type=="person",select="id")$id
  hubv <- subset(vertexinfo, vertexinfo$type=="hub",select="id")$id
  plotter_ids <- subset(vertexinfo, vertexinfo$type=="plotter", select="id")$id
  allplotters <- c(hubv, plotter_ids)
  max_id <- max(plotter_ids)
  hubData<-srcs$hub
  backData<-srcs$back
  plotData<-srcs$plot
  allData <- rbind( hubData[,cols], backData[,cols], plotData[,cols] )
  max_time <- max(allData$timestep)
  fnames <- sapply(c("structure","content","sandc","bad"),function(head) { paste(head, "_", id,".txt", sep="",collapse="") })
  files <- lapply(fnames, file, open="w")
  chuck<-sapply(1:max_time,function(t){
    submerge <- subset(allData, allData$timestep <= t)
    # calculate structure FPR / TPR
    bdegrees <- bothdegrees(submerge,max_id)
    qs<-quantile(bdegrees[which(bdegrees > 0)],probs=c(0,0.05,0.95,1))
    peopleDegrees <- (bdegrees[people] != 0) & ((bdegrees[people] < qs[2]) | (bdegrees[people] > qs[3]))
    sFPR <- sum(peopleDegrees)
    plotterDegrees <- (bdegrees[plotter_ids] != 0) & (bdegrees[plotter_ids] < qs[2])
    hubDegrees <- (bdegrees[hubv] > qs[3])
    sTPR <- sum(plotterDegrees) + hubDegrees
    # calculate content FPR / TPR
    subc <- unique( subset(submerge, submerge$content == 'Bad', select=c("recipient_id","sender_id")) )
    hit <- unique(c(subc$recipient_id,subc$sender_id)) %in% allplotters
    cTPR <- sum(hit)
    cFPR <- sum(!hit)
    # calculate structure and content FPR / TPR
    peopleBad <- hasbad(people,subc)
    plotterBad <- hasbad(plotter_ids,subc)
    hubBad <- hasbad(hubv,subc)
    sandcFPR <- sum(peopleDegrees & peopleBad)
    sandcTPR <- sum(plotterDegrees & plotterBad) + (hubBad & hubDegrees)
    # calculate total in-plot bads
    bads <- 
      dim(subset(submerge, 
        submerge$content == 'Bad' & 
        (submerge$sender_id %in% allplotters) & 
        (submerge$recipient_id %in% allplotters), 
        select="content")
      )[1]
    write(c(sFPR,sTPR),files$structure,ncol=2,append=T)
    write(c(cFPR,cTPR),files$content,ncol=2,append=T)
    write(c(sandcFPR,sandcTPR),files$sandc,ncol=2,append=T)
    write(bads,files$bad,ncol=1,append=T)
  })
  chuck<-lapply(files,flush)
  chuck<-lapply(files,close)
  print(id)
  T
}, plotFiles, backFiles, hubFiles, viFiles, 1:length(viFiles), USE.NAMES=F)

# hubInc <- read.table("./4-1-hub-1.txt", sep=" ", col.names=c("recipient_id","sender_id","channel_type","content","timestep"))
# backInc <- read.table("./4-1-back-1.txt", sep=" ", col.names=c("recipient_id","sender_id","channel_type","content","timestep"))
# plotInc <- read.table("./4-1-plot-1.txt", sep=" ", col.names=c("recipient_id","sender_id","channel_type","content","timestep"))
# 
# allData <- rbind( 
#   hubInc[,cols], 
#   backInc[,cols], 
#   plotInc[,cols]
# )
# submerge <- subset(allInc, allInc$timestep <= 10)
# unique(subset(submerge, submerge$content == 'Bad',select=c("recipient_id","sender_id") ))
# 
# test<-sapply(1:max(merge[,"timestep"]),indegreer(all_ids),simplify="matrix")
# 
# urows <- row.names(unique(backInc[,c("recipient_id","sender_id")])) ## get the rows from source data for those

structureFiles <- sort(list.files(".","structure_\\d+\\.txt"))
contentFiles <- sort(list.files(".","content_\\d+\\.txt"))
sandcFiles <- sort(list.files(".","sandc_\\d+\\.txt"))
badFiles <- sort(list.files(".","bad_\\d+\\.txt"))

popsize <- 500
plotsize <- 16

structureFPR<-sapply(structureFiles,function(fname) {
  read.table(fname,header=F,sep=" ",col.names=c("FPR","TPR"))$FPR
},simplify="matrix") / popsize
sFPRq <- apply(structureFPR,1,quantile)

structureTPR<-sapply(structureFiles,function(fname) {
  read.table(fname,header=F,sep=" ",col.names=c("FPR","TPR"))$TPR
},simplify="matrix") / plotsize
sTPRq <- apply(structureTPR,1,quantile)

contentFPR<-sapply(contentFiles,function(fname) {
  read.table(fname,header=F,sep=" ",col.names=c("FPR","TPR"))$FPR
},simplify="matrix") / popsize
cFPRq <- apply(contentFPR,1,quantile)

contentTPR<-sapply(contentFiles,function(fname) {
  read.table(fname,header=F,sep=" ",col.names=c("FPR","TPR"))$TPR
},simplify="matrix") / plotsize
cTPRq <- apply(contentTPR,1,quantile)

sandcFPR<-sapply(sandcFiles,function(fname) {
  read.table(fname,header=F,sep=" ",col.names=c("FPR","TPR"))$FPR
},simplify="matrix") / popsize
sandcFPRq <- apply(sandcFPR,1,quantile)

sandcTPR<-sapply(sandcFiles,function(fname) {
  read.table(fname,header=F,sep=" ",col.names=c("FPR","TPR"))$TPR
},simplify="matrix") / plotsize
sandcTPRq <- apply(sandcTPR,1,quantile)


badMR <- sapply(badFiles,function(fname) {
  read.table(fname,header=F,col.names=c("badMR"))$badMR
},simplify="matrix")
badMR <- badMR / max(badMR)
badMRq <- apply(badMR,1,quantile)

timesteps <- 100
samples <- 100
t<-1:timesteps
tm <- matrix(t,nrow=timesteps,ncol=samples)
backpoly<-function(t,x,color,lb=0) {
  polygon( c(min(t), t, max(t)), c( lb, x, lb), col=color, border=NA )
}
plot(t,badMRq[5,],type="l",col="grey93",ylab="",xlab="",yaxt="n",xaxt="n",bty="n")
backpoly(t,badMRq[5,],col="grey93")
backpoly(t,badMRq[4,],col="grey85")
backpoly(t,badMRq[2,],col="grey93")
backpoly(t,badMRq[1,],col="white",lb=-1)
axis(side=1,at=c(min(t),max(t)),labels=c(expression(t[min]),expression(t[max])),tick=F,line=-1)
text(75,0.5,"MR")

liner<-function(t,TPRq,FPRq,TPRxy,FPRxy) {
  lines(x=t,y=TPRq[3,],col="green3",type="l",xlab="iterate",ylab="FPR",ylim=c(0,1))
  lines(x=t,y=TPRq[4,],col="darkolivegreen1")
  lines(x=t,y=TPRq[2,],col="darkolivegreen1")
  lines(x=t,y=FPRq[3,],col="red")
  lines(x=t,y=FPRq[4,],col="coral")
  lines(x=t,y=FPRq[2,],col="coral")
  text(TPRxy[1],TPRxy[2],"TPR",col="green3")
  text(FPRxy[1],FPRxy[2],"FPR",col="red")
}

plotter<-function(t,badMRq,TPRq,FPRq,TPRxy,FPRxy) {
  plot(t,badMRq[5,],type="l",col="grey93",ylab="",xlab="",yaxt="n",xaxt="n",bty="n")
  backpoly(t,badMRq[5,],col="grey93")
  backpoly(t,badMRq[4,],col="grey85")
  backpoly(t,badMRq[2,],col="grey93")
  backpoly(t,badMRq[1,],col="white",lb=-1)
  axis(side=1,at=c(min(t),max(t)),labels=c(expression(t[min]),expression(t[max])),tick=F,line=-1)
  text(75,0.5,"MR")
  liner(t,TPRq,FPRq,TPRxy,FPRxy)
}
scale <- 500
png(file="4_1_500_s.png",width=scale,height=scale)
plotter(t,badMRq,sTPRq,sFPRq,c(20,0.5),c(80,.15))
dev.off()

png(file="4_1_500_c.png",width=scale,height=scale)
plotter(t,badMRq,cTPRq,cFPRq,c(20,0.5),c(80,.15))
dev.off()

png(file="4_1_500_sandc.png",width=scale,height=scale)
plotter(t,badMRq,sandcTPRq,sandcFPRq,c(20,0.5),c(80,.15))
dev.off()