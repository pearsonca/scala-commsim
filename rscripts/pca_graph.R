## PCA plots in ggplot

require(ggplot2)

pcaplot <- function(prcompres, limit=length(prcompres$sdev), validation.dt) {
  fcts <- paste0("PC", 1:limit)
  ref <- factor(fcts, levels=fcts, ordered=T)
  cols <- ref[seq(from=1, to=limit, by=2)]
  rows <- ref[seq(from=2, to=limit, by=2)]
  pc <- dim(prcompres$x)[1]
  (if(limit %% 2 == 0) {
    if (limit == 2) {
      data.table(col = cols, row = rows)
    } else data.table(col = cols[c(1, rep(2:length(cols), each=2))], row = rows[c(rep(1:(length(rows)-1), each=2), length(rows))])
  } else {
    if (limit == 3) {
      data.table(col = cols, row = rows)
    } else data.table(col = cols[c(1, rep(2:(length(cols)-1), each=2), length(cols))], row = rep(rows, each=2))
  }) -> template
  
  kcluster <- kmeans(prcompres$x[,1:limit], 5, nstart = 20)

  pred.src <- template[,list(x=prcompres$x[,col], y=prcompres$x[,row], cluster=factor(kcluster$cluster)), by=list(col, row)]
  ## on pca scale validation.dt[,-1,with=F][,(.SD-as.list(res$center))/as.list(res$scale)]
  valid.x <- as.matrix(validation.dt) %*% res$rotation
  pred.cluster <- apply(valid.x[,1:limit],1,function(row) {
    which.min(colSums(row - t(kcluster$centers))^2)
  })
  valid.src <- template[,list(x=valid.x[,col], y=valid.x[,row], cluster=factor(pred.cluster)), by=list(col, row)]
  ggplot(pred.src) + theme_bw() + facet_grid(row ~ col) + aes(x=x, y=y, color=cluster) + geom_point(aes(size="training", alpha="training")) + geom_point(aes(size="validation", alpha="validation"), data = valid.src) +
    scale_size_manual(limits=c("training","validation"), values=c(10,2)) + scale_alpha_manual(limits=c("training","validation"), values=c(0.2,1))
  
  ## first, long format, rows odd components, cols even components
}

prcompres <- prcomp(prediction.dt, scale. = F)
p <- pcaplot(prcompres, limit=6, validation.dt)
print(p)

