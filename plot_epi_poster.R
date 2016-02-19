# plot results for epi paper

# setwd("~/git/scala-commsim")

require(data.table)
require(ggplot2)

things <- list.files("output/analyze-test/proc/", full.names = T)
combined <- rbindlist(lapply(things, readRDS))
class(combined$C) <- "integer"
class(combined$B) <- "integer"
class(combined$NB) <- "integer"
class(combined$t) <- "integer"

ggplot(combined) + aes(x=t) + theme_bw() +
  geom_line(aes(y=C/5, color="TPR")) + geom_line(aes(y=B/NB, color="FPR")) +
  labs(y="true/false positive rate", x="interval", color="rate")
