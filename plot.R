## plotting

require(stats4)

setwd("~/extract/")

memCounts <- seq(10, 20, 2)
locCounts <- 1:3
meetInterval <- seq(10, 30, 10)

pop <- 209264
nf <- layout(matrix(1:(length(memCounts)*length(locCounts)), ncol=length(locCounts)), widths = c(1.1,1,1.1), heights = c(1.1, rep.int(1, length(memCounts)-2), 1.1))
layout.show(nf)
old.par <- par( bty = "n", mar=c(0,0,0,0), mgp = c(0.5, 0.5, 0) )
def.par <- par()


loc.colors.red <- list()
loc.colors.blu <- list()
reds <- c("coral", "red", "darkred")
blues <- c("lightblue", "blue", "darkblue")
for (i in 1:length(meetInterval)) {
  loc.colors.red[[ meetInterval[i] ]] <- reds[i]
  loc.colors.blu[[ meetInterval[i] ]] <- blues[i]
}

#cnt <- memCounts[1]; interval <- meetInterval[1]; loc <- locCounts[1]

plotres <- function(res, red, blu) {
  means <- -log(1-apply(res[,,-1], 2:3, mean))
  maxes <- -log(1-apply(res[,,-1], 2:3, quantile, probs=0.75, names=F))
  mines <- -log(1-apply(res[,,-1], 2:3, quantile, probs=0.25, names=F))
  
  lines(res[1,,1], maxes[,1], col=red, pch=6, lty="dotted", lwd=0.5)
  lines(res[1,,1], means[,1], col=red)
  lines(res[1,,1], mines[,1], col=red, pch=2, lty="dashed", lwd=0.5)
  
  lines(res[1,,1], maxes[,2], col=blu, pch=6, lty="dotted", lwd=0.5)
  lines(res[1,,1], means[,2], col=blu)
  lines(res[1,,1], mines[,2], col=blu, pch=2, lty="dashed", lwd=0.5)
}

plot.count <- 1
axis.wid <- 4

day.ticks <- c(365, seq(400, 1800, 50))
day.labels <- c(365, rep(NA,length(day.ticks)-3),1750,NA)

day.axis <- function(.side) {
  axis(.side, at = day.ticks, labels = day.labels, tcl = -0.2)
  mtext("...day from start...", side = .side, line = 0.5, cex = 0.7, adj=0.25)
  #title( xlab="day" )
  #mtext("day", side = .side, cex=0.5, adj=0.1, padj = ifelse(.side==3,0,1))
}

rate.ticks <- seq(0, 0.6, 0.1)
log.locs <- -log(1-rate.ticks)

rate.axis <- function(.side, .col, .name, .padj= 1.5) {
  mtext(.name, side=.side, col=.col, padj=.padj, cex = 0.7, adj=0.9)
  axis(.side, at = log.locs, labels=rate.ticks, col = .col, tcl = -0.2)
}

for (loc in locCounts) {
  for (cnt in memCounts) {
    if (plot.count == 1) {
      par(mar = axis.wid*c(0,1,1,0))
      ## top left
    } else if (plot.count == 2) {
      par(mar = axis.wid*c(0,1,0,0))
      ## left
    } else if (plot.count == 6) {
      par(mar = axis.wid*c(1,1,0,0))
      ## bottom left
    } else if (plot.count == 7) {
      par(mar = axis.wid*c(0,0,1,0))
      ## top mid
    } else if (plot.count == 8) {
      par(mar = axis.wid*c(0,0,0,0))
      ## mid
    } else if (plot.count == 12) {
      par(mar = axis.wid*c(1,0,0,0))
      ## bottom mid
    } else if (plot.count == 13) {
      par(mar = axis.wid*c(0,0,1,1))
      ## top right
    } else if (plot.count == 14) {
      par(mar = axis.wid*c(0,0,0,1))
      ## right
    } else if (plot.count == 18) {
      par(mar = axis.wid*c(1,0,0,1))
      ## bottom right
    }
    plot(NULL, NULL, ylim = c(0, 1), xlim = c(365, 1800), ylab="", xlab="", xaxt="n", yaxt="n")
    
    if (plot.count == 1) {
      mtext("meeting locations = ", side = 3, cex = 0.7, padj = -2.5, adj=0.1)
    }
    if (plot.count %in% c(1,7,13)) {
      mtext(loc, side = 3, cex = 0.7, padj = -2.5)
      day.axis(3)
    }
    
    if (plot.count == 6) {
      mtext("# of members = ", side = 2, cex = 0.7, padj = -2.5, adj = -0.2)
    }
    
    if (plot.count %in% 1:6) {
      mtext(cnt, side = 2, cex = 0.7, padj = -2.5, adj = 0.7)
      rate.axis(2, "red", "TPR")
      #axis(2, at = seq(0,1,.1), col = "red" , line=0, tcl=0.2)
      #mtext("TPR", side=2, col="red")
    }
    if (plot.count %in% 13:18) {
      rate.axis(4, "blue", "FPR", -1.5)
#       axis(4, at = seq(0,1,.1), col = "blue", line=0, tcl=0.2)
#       mtext("FPR", side=4, col="blue")
    }
    if (plot.count %in% c(6,12,18)) {
      day.axis(1)
    }
    
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
        memtab <- as.matrix(read.table(membershipSamples[j], header = F))
        siztab <- as.matrix(read.table(sizeSamples[j], header = F))
        for (i in 1:dim(memtab)[1]) {
          rr <- rle(sort.int(memtab[i,-1], method = "quick"))
          tot <- cnt-1
          ps <- (rr$lengths - 1)/tot
          TPR <- mean(ps) + (1/tot)
          sizes <- siztab[i, names(rr$values)]
          FPR <- sum(ps * sizes / pop)
          res[j,i,1:3] <- c(memtab[i,1], ifelse(is.nan(TPR),0,TPR), FPR)
        }
      }
      
      plotres(res, red = loc.colors.red[[interval]], blu = loc.colors.blu[[interval]])
      
    }
    
  }
}

## for parameter combination
##  for each sample
##    convert membership + size information into TPR / FRP
##  consolidate samples into mean + uncertainty intervals
## small multiples the group size and meeting period, plot lines for each location count