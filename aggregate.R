# aggregate

#setwd("~/git/scala-commsim/")
rm(list=ls())
require(data.table)
require(bit64)
require(igraph)
require(parallel)
args <- commandArgs(trailingOnly = T)
# args <- c("output/analyze-test", "-1-0-", "input")

snapshot.files <- list.files(args[1],paste0(args[2],"fgcomm-\\d+.rds"), full.names = T)

# read in and score snapshot.files[1]

scorefile <- function(fn, discount) {
  res <- readRDS(fn)[, score := discount ]
  res[user.b < user.a, `:=`(user.b = user.a, user.a = user.b)]
  res[, score, keyby=list(user.a, user.b)]
}

storeres <- function(res, fn) {
  saveRDS(res, gsub(".rds","-agg.rds", fn))
  res
}

disc <- 0.9
censor <- disc^6 # i.e., no activity in six months

Reduce(function(prev, cur.filename) {
  newres <- rbind(scorefile(cur.filename, disc), prev[, score := score*disc ])
  cat(cur.filename,"\n")
  storeres(newres[,list(score = sum(score)), keyby=list(user.a, user.b)][score > censor], cur.filename)
}, snapshot.files[-1], storeres(scorefile(snapshot.files[1], disc), snapshot.files[1]))

## for each snapshot file, in order
#   read in file
#   score file
#   add previous score, discounted
#   store