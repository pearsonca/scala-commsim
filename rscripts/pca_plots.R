## PCA plots in ggplot

require(ggplot2); require(data.table); require(reshape2)

make_bars <- function(pca, target = 1:2) with(pca, {
  setnames(data.table(dcast(melt(rotation[, target]), Var1 ~ Var2)), "Var1", "measure")
})

plot_pca_bars <- function(pca_bars) {
  limY <- max(abs(pca_bars[,PC1]))
  limC <- max(abs(pca_bars[,PC2]))
  
  ggplot(pca_bars) + theme_bw() + theme(
    axis.text.x = element_text(angle=90, size = rel(0.75))
  ) + aes(x=measure, y=PC1, fill=PC2) + geom_bar(stat="identity", position = "identity") +
  labs(x="Predictor", y="PC 1 Loading", fill="PC 2 Loading") + ylim(-limY, limY) +
  scale_fill_gradient2(limits=c(-limC, limC), low="red", mid = "grey", high="blue")
}