## plotting

require(stats4)
require(data.table)
require(ggplot2)

setwd("~/extract/")

memCounts <- seq(10, 20, 2)
locCounts <- 1:3
meetInterval <- seq(10, 30, 10)

##pop <- 209264

shift <- 30*24*60*60
end <- 365*24*60*60
pop <- array(,dim = 49)
for (p in 1:49) {
  pop[p] <- src.dt[(p-1)*shift < login & login <= ((p-1)*shift+end),length(unique(user.id))]
}

#quartz("wtf", width = 5.5, height = 9, dpi = 300)

nf <- layout(matrix(1:(length(memCounts)*length(locCounts)), ncol=length(locCounts)) )
old.par <- par( bty = "n", mar=c(0,0,0,0), mgp = c(0.5, 0.5, 0), cex.axis=3 )

loc.colors.red <- list()
loc.colors.blu <- list()
reds <- c("coral", "red", "darkred")
blues <- c("lightblue", "blue", "darkblue")
for (i in 1:length(meetInterval)) {
  loc.colors.red[[ meetInterval[i] ]] <- reds[i]
  loc.colors.blu[[ meetInterval[i] ]] <- blues[i]
}

plotres <- function(res, red, blu) {
  means <- apply(res[,,-1], 2:3, median) #-log(1-)
  maxes <- apply(res[,,-1], 2:3, quantile, probs=0.75, names=F) #-log(1-)
  mines <- apply(res[,,-1], 2:3, quantile, probs=0.25, names=F) #-log(1-)
  
  lines(res[1,,1], maxes[,1], col=red, pch=6, lty="dotted", lwd=0.5)
  lines(res[1,,1], means[,1], col=red)
  lines(res[1,,1], mines[,1], col=red, pch=2, lty="dashed", lwd=0.5)
  
  lines(res[1,,1], maxes[,2], col=blu, pch=6, lty="dotted", lwd=0.5)
  lines(res[1,,1], means[,2], col=blu)
  lines(res[1,,1], mines[,2], col=blu, pch=2, lty="dashed", lwd=0.5)
}

plot.count <- 1
axis.wid <- 3

day.ticks <- c(365, seq(400, 1800, 50))
day.labels <- c(365, rep(NA,length(day.ticks)-3),1750,NA)

day.axis <- function(.side, .padj = 0) {
  axis(.side, at = day.ticks, labels = day.labels, tcl = -0.2, line = -1, padj=.padj)
  mtext("...day from start...", side = .side, line = -0.5, cex = 0.75*par("cex.axis"), adj=0.25, padj=.padj)
  #title( xlab="day" )
  #mtext("day", side = .side, cex=0.5, adj=0.1, padj = ifelse(.side==3,0,1))
}

rate.ticks <- seq(0, 0.6, 0.1)
log.locs <- -log(1-rate.ticks)

rate.axis <- function(.side, .col, .name, .padj= 1.5, .adj=0.9) {
  mtext(.name, side=.side, col=.col, padj=.padj, cex = 0.75*par("cex.axis"), adj=.adj, line = -1)
  axis(.side, at = seq(0,1,0.1), labels=seq(0,1,0.1), col = .col, tcl = -0.2, las=2, cex = 0.5, line = -1)
}

.squeeze <- 0.2

setborders <- function(pcount) {
  if (pcount == 1) {
    par(mar = axis.wid*c(0,2-.squeeze,2-.squeeze,0))
    ## top left
  } else if (pcount == 2) {
    par(mar = axis.wid*c(.squeeze/2,2-.squeeze,.squeeze/2,0))
    ## left
  } else if (pcount == 6) {
    par(mar = axis.wid*c(2-.squeeze,2-.squeeze,0,0))
    ## bottom left
  } else if (pcount == 7) {
    par(mar = axis.wid*c(0,.squeeze/2,2-.squeeze,.squeeze/2))
    ## top mid
  } else if (pcount == 8) {
    par(mar = axis.wid*c(.squeeze/2,.squeeze/2,.squeeze/2,.squeeze/2))
    ## mid
  } else if (pcount == 12) {
    par(mar = axis.wid*c(2-.squeeze,.squeeze/2,0,.squeeze/2))
    ## bottom mid
  } else if (pcount == 13) {
    par(mar = axis.wid*c(0,0,2-.squeeze,2-.squeeze))
    ## top right
  } else if (pcount == 14) {
    par(mar = axis.wid*c(.squeeze/2,0,.squeeze/2,2-.squeeze))
    ## right
  } else if (pcount == 18) {
    par(mar = axis.wid*c(2-.squeeze,0,0,2-.squeeze))
    ## bottom right
  } else {
    par()
  }
}

setaxes <- function(pcount, loc, cnt) {
  if (pcount == 1) {
    mtext("# of meeting locations = ", side = 3, cex = 0.75*par("cex.axis"), padj = -2.1, adj=0.1, line = -1.5)
  }
  if (pcount %in% c(1,7,13)) {
    mtext(loc, side = 3, cex = 0.75*par("cex.axis"), adj=0.75, padj = -2.1, line = -1.5)
    day.axis(3)
  }
  
  if (pcount == 6) {
    mtext("# of members = ", side = 2, cex = 0.75*par("cex.axis"), padj = -2.3, adj = 0, line = -1.5)
  }
  
  if (pcount %in% 1:6) {
    mtext(cnt, side = 2, cex = 0.75*par("cex.axis"), padj = -2.3, adj = 0.8, line = -1.5)
    rate.axis(2, "red", "TPR")
    #axis(2, at = seq(0,1,.1), col = "red" , line=0, tcl=0.2)
    #mtext("TPR", side=2, col="red")
  }
  if (pcount %in% 13:18) {
    rate.axis(4, "blue", "FPR", -1, 0.3)
    #       axis(4, at = seq(0,1,.1), col = "blue", line=0, tcl=0.2)
    #       mtext("FPR", side=4, col="blue")
  }
  if (pcount %in% c(6,12,18)) {
    day.axis(1, 1.5)
  }
}

#res.dt <- data.table()

plotter <- function() {
  for (loc in locCounts) {
    for (cnt in memCounts) {
      setborders(plot.count)
      
      plot(NULL, NULL, ylim = c(0, 1), xlim = c(365, 1800), ylab="", xlab="", xaxt="n", yaxt="n")
      
      setaxes(plot.count, loc, cnt)
      
      plot.count <- plot.count + 1
      
      for (interval in meetInterval) {
        membershipSamples <- list.files(pattern = paste(cnt, interval, loc,".*-members.csv", sep="-"))
        sizeSamples <- list.files(pattern = paste(cnt, interval, loc,".*-sizes.csv", sep="-"))
        
        res <- array(0,
            dim = c(length(membershipSamples), 49, 3),
            dimnames = list(
              sample=1:length(membershipSamples),
              obs=1:49,
              dat=c("day","TPR","FPR")
            )
        )
        for (j in 1:length(membershipSamples)) {
#           print(c(loc=loc, count=cnt, interval=interval, j=j))
          memtab <- as.matrix(read.table(membershipSamples[j], header = F))
          siztab <- as.matrix(read.table(sizeSamples[j], header = F))
          for (i in 1:dim(memtab)[1]) {
            rr <- rle(sort.int(memtab[i,-1], method = "quick"))
            tot <- cnt-1
            ps <- rr$lengths / cnt
            TPR <- sum(ps * (rr$lengths - 1)) / tot
            sizes <- siztab[i, names(rr$values)] - rr$lengths
            FPR <- sum(ps * sizes) / pop[i]
            res[j,i,1:3] <- c(memtab[i,1], ifelse(is.nan(TPR),0,TPR), FPR)
          }
        }
        
        plotres(res, red = loc.colors.red[[interval]], blu = loc.colors.blu[[interval]])
        
      }
      
    }
  }
}

parser <- function(dt = data.table()) {
  for (loc in locCounts) {
    for (cnt in memCounts) {
      for (interval in meetInterval) {
        membershipSamples <- list.files(pattern = paste(cnt, interval, loc,".*-members.csv", sep="-"))
        sizeSamples <- list.files(pattern = paste(cnt, interval, loc,".*-sizes.csv", sep="-"))
        
        res <- array(0,
                     dim = c(length(membershipSamples), 49, 3),
                     dimnames = list(
                       sample=1:length(membershipSamples),
                       obs=1:49,
                       dat=c("day","TPR","FPR")
                     )
        )
        for (j in 1:length(membershipSamples)) {
                     print(c(loc=loc, count=cnt, interval=interval, j=j))
          memtab <- as.matrix(read.table(membershipSamples[j], header = F))
          siztab <- as.matrix(read.table(sizeSamples[j], header = F))
          for (i in 1:dim(memtab)[1]) {
            rr <- rle(sort.int(memtab[i,-1], method = "quick"))
            tot <- cnt-1
            ps <- rr$lengths / cnt
            TPR <- sum(ps * (rr$lengths - 1)) / tot
            sizes <- siztab[i, names(rr$values)] - rr$lengths
            FPR <- sum(ps * sizes) / pop[i]
            dt <- rbindlist(list(dt, list(meeting.locations = loc, group.size = cnt, meeting.interval = interval, run=j, day=memtab[i,1], TPR=ifelse(is.nan(TPR),0,TPR), FPR=FPR)))
            #res[j,i,1:3] <- c(memtab[i,1], ifelse(is.nan(TPR),0,TPR), FPR)
          }
        }
        
        #plotres(res, red = loc.colors.red[[interval]], blu = loc.colors.blu[[interval]])
        
      }
      
    }
  }
  dt
}

res.dt <- parser()

proc.dt <- res.dt[,list(FPR.med=median(FPR),
                        TPR.med=median(TPR),
                        TPR.high=quantile(TPR,probs=.75),
                        TPR.low=quantile(TPR,probs=.25),
                        FPR.high=quantile(FPR,probs=.75),
                        FPR.low=quantile(FPR,probs=.25)
                        ),by=list(meeting.locations, group.size, meeting.interval, day)]

melt.dt <- melt(proc.dt,value.name = "value",id.vars = c("meeting.locations","group.size","meeting.interval","day"))
melt.dt[,measure:=factor(sub('\\.\\w+','',as.character(variable)))]
melt.dt[,quantile:=factor(sub('\\w+\\.','',as.character(variable)))]
melt.dt$meeting.interval <- factor(melt.dt$meeting.interval)
melt.dt$group.size <- factor(melt.dt$group.size)
melt.dt$meeting.locations <- factor(melt.dt$meeting.locations)

tiff("~/Desktop/results_hires_color.tiff", width = 6, height = 9, units = "in", res = 300)
#p <- ggplot(melt.dt) + aes(x=day, y=value, linetype=meeting.interval, size=measure, alpha=quantile) + facet_grid(group.size ~ meeting.locations) + geom_line()
p <- ggplot(melt.dt) + aes(x=day, y=value, linetype=meeting.interval, color=measure, alpha=quantile) + facet_grid(group.size ~ meeting.locations) + geom_line()
p <- p + theme_bw() + theme(axis.text.y = element_text(angle = 90, hjust = 0.5))
#p + scale_size_manual(values=c(0.5,1)) + scale_linetype(name="Meeting\nInterval") + scale_y_continuous(name="")
p + scale_color_manual(values=c("blue","red")) + scale_linetype(name="Meeting\nInterval") + scale_y_continuous(name="")
dev.off()

#plotter()

#dev.off()
# 
# stop()
# 
# histres <- function(res, red, blu) {
#   thing <- sort(apply(res[,,2], 1, function(x) which(x > 0.5, arr.ind = T)[1] ), na.last = T)
#   print(thing)
#   thing[is.na(thing)] <- 50
#   thingrle <- rle(thing)
#   print(thingrle)
#   lines((thingrle$values-1)*30+365, cumsum(thingrle$lengths), col=red, pch=6, lty="solid")
# }
# 
# old.par <- par( bty = "n", mar=c(2,2,0,0), cex.axis=1 )
# 
# arrivalsplot <- function() {
#   plot.count <- 1
#   for (loc in locCounts) {
#     for (cnt in memCounts) {
#       #setborders(plot.count)
#       
#       plot(NULL, NULL, ylim = c(0, 100), xlim = c(0, 49)*30+365, ylab="", xlab="")
#       
#       #setaxes(plot.count, loc, cnt)
#       
#       plot.count <- plot.count + 1
#       
#       for (interval in meetInterval) {
#         membershipSamples <- list.files(pattern = paste(cnt, interval, loc,".*-members.csv", sep="-"))
#         sizeSamples <- list.files(pattern = paste(cnt, interval, loc,".*-sizes.csv", sep="-"))
#         
#         res <- array(0,
#                      dim = c(length(membershipSamples), 49, 3),
#                      dimnames = list(
#                        sample=1:length(membershipSamples),
#                        obs=1:49,
#                        dat=c("day","TPR","FPR")
#                      )
#         )
#         for (j in 1:length(membershipSamples)) {
#           memtab <- as.matrix(read.table(membershipSamples[j], header = F))
#           siztab <- as.matrix(read.table(sizeSamples[j], header = F))
#           for (i in 1:dim(memtab)[1]) {
#             rr <- rle(sort.int(memtab[i,-1], method = "quick"))
#             tot <- cnt-1
#             ps <- rr$lengths / cnt
#             TPR <- sum(ps * (rr$lengths - 1)) / tot
#             sizes <- siztab[i, names(rr$values)] - rr$lengths
#             FPR <- sum(ps * sizes) / pop[i]
#             res[j,i,1:3] <- c(memtab[i,1], ifelse(is.nan(TPR),0,TPR), FPR)
#           }
#         }
#         
#         histres(res, red = loc.colors.red[[interval]], blu = loc.colors.blu[[interval]])
#         
#       }
#       
#     }
#   }
# }
# 
# arrivalsplot()
# ## for parameter combination
# ##  for each sample
# ##    convert membership + size information into TPR / FRP
# ##  consolidate samples into mean + uncertainty intervals
# ## small multiples the group size and meeting period, plot lines for each location count