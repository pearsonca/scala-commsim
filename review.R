# aggregate

#setwd("~/git/scala-commsim/")
rm(list=ls())
require(data.table)
require(bit64)
require(igraph)
require(parallel)
args <- commandArgs(trailingOnly = T)
# args <- c("output/analyze-test", "-1-0-", "input")

agg.files <- list.files(args[1],paste0(args[2],"fgcomm-\\d+-agg.rds"), full.names = T)

disc <- 0.9

# relabel vertices

slice <- function(el.dt, cutoff) {
  res <- el.dt[score > disc]
  remap_ids <- res[,list(user_id=unique(c(user.a,user.b)))][, new_user_id := .I, keyby=user_id]
  relabelled <- data.table(
    user.a=remap_ids[res[,list(user_id=user.a)]]$new_user_id,
    user.b=remap_ids[res[,list(user_id=user.b)]]$new_user_id,
    score=res$score
  )
  list(res=relabelled, mp=remap_ids)
}

max.uid <- as.integer(readRDS(paste0(args[3],"/raw-input.rds"))[,max(user_id)]+1)
covert.ids <- max.uid + 1:5

emptygraph <- data.table(C=list(), B=list(), NB=list(), t=list())

detect <- function(fn, inc, covert.ids, disc) {
  el.dt <- readRDS(fn)
  rt <- slice(el.dt, disc)
  if (dim(rt$res)[1] != 0) {
    gg <- graph(t(rt$res[,list(user.a, user.b)]), directed=F)
    E(gg)$weight <- rt$res$score
    
    comps <- components(gg)
    dn <- which(comps$csize > 2 & comps$csize <= 10) # components to treat as their own (covert) communities
    littleverts <- rt$mp[which(comps$membership %in% dn)]$user_id
    mr <- which(comps$csize > 10) # components to break down into communities
    bigverts <- sapply(mr, function(tr) {
      ggs <- induced_subgraph(gg, which(comps$membership == tr))
      cs <- cluster_spinglass(ggs)
      targets <- which(sizes(cs) <= 10)
      rt$mp[which(cs$membership %in% targets)]$user_id
    })
    alltargets <- c(bigverts, littleverts)
    if (length(alltargets) != 0) {
      C <- sum(alltargets %in% covert.ids)
    } else {
      C <- 0
    }
    saveRDS(
      data.table(C=C, B=length(alltargets)-C, NB=rt$mp[!(user_id %in% covert.ids), .N], t=inc),
      gsub("-agg.rds","-proc.rds", fn)
    )
    # return(sort(c(bigverts, littleverts)))
  } else {
    saveRDS(
      emptygraph,
      gsub("-agg.rds","-proc.rds", fn)
    )
    # return(sort(c(bigverts, littleverts)))
  }
}

missing <- 44:48

mcmapply(detect, agg.files[missing], (1:length(agg.files))[missing], MoreArgs = list(covert.ids=covert.ids, disc=disc), mc.cores = detectCores()-1)
