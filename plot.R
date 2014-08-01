## plotting

require(stats4)

setwd("~/extract/")

memCounts <- seq(10, 20, 2)
locCounts <- 1:3
meetInterval <- seq(10, 30, 10)

pop <- 200000

cnt <- memCounts[1]
loc <- locCounts[1]
interval <- meetInterval[1]

membershipSamples <- list.files(pattern = paste(cnt, interval, loc,".*-members.csv", sep="-"))
sizeSamples <- list.files(pattern = paste(cnt, interval, loc,".*-sizes.csv", sep="-"))

res <- array(0, dim = c(length(membershipSamples), 49, 3),
             dimnames=list(
               sample=1:length(membershipSamples),
               obs=1:dim(memtab)[1],
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

means <- apply(res[,,-1], 2:3, mean)
maxes <- apply(res[,,-1], 2:3, quantile, probs=0.75, names=F)
mines <- apply(res[,,-1], 2:3, quantile, probs=0.25, names=F)

plot(res[1,,1], maxes[,1], ylim=c(0, max(maxes)), col="red", pch=6, xlab="day", ylab="TPR, FPR")
points(res[1,,1], means[,1], col="red")
points(res[1,,1], mines[,1], col="red", pch=2)

points(res[1,,1], maxes[,2], col="blue", pch=6)
points(res[1,,1], means[,2], col="blue")
points(res[1,,1], mines[,2], col="blue", pch=2)

## for parameter combination
##  for each sample
##    convert membership + size information into TPR / FRP
##  consolidate samples into mean + uncertainty intervals
## small multiples the group size and meeting period, plot lines for each location count