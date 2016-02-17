#!/usr/bin/env Rscript

#setwd("~/git/scala-commsim/")
rm(list=ls())
require(data.table)
require(bit64)
require(igraph)
require(parallel)
args <- commandArgs(trailingOnly = T)
# args <- c("output/process-test", "output/analyze-test", "input")

# locLimits <- readRDS("input/location-lifetimes.rds")

getSimRes <- function(wh="cc") list.files(args[1], pattern = paste0(wh,".rds"), full.names = T)
src.dt <- readRDS(paste0(args[3],"/raw-input.rds"))
max.uid <- as.integer(src.dt[,max(user_id)]+1)

readELtable <- function(fname, offsetid) {
  res <- readRDS(fname)
  res[user.a < 0, user.a := -user.a + offsetid]
  res[user.b < 0, user.b := -user.b + offsetid]
  res[user.b < user.a, `:=`(user.b = user.a, user.a = user.b)]
  res[,list(user.a, user.b, location_id, arrive, depart), keyby=list(login, logout)]
}

base.dt <- readRDS(paste0(args[3],"/raw-pairs.rds"))
base.dt[
  user.b < user.a,
  `:=`(user.b = user.a, user.a = user.b)
]

base.dt[,
  `:=`(login = start, logout = end)
]

base.dt <- setcolorder(base.dt[,list(user.a=user.a+1, user.b=user.b+1),keyby=list(login,logout)], c("user.a","user.b","login","logout"))

slice <- function(el.dt, start.day, end.day) {
  res <- el.dt[(logout < end.day*24*3600) & (login > start.day*24*3600), .N, keyby=list(user.a, user.b)]
  remap_ids <- res[,list(user_id=unique(c(user.a,user.b)))][, new_user_id := .I, keyby=user_id]
  relabelled <- data.table(user.a=remap_ids[res[,list(user_id=user.a)]]$new_user_id, user.b=remap_ids[res[,list(user_id=user.b)]]$new_user_id)
  list(res=relabelled, mp=remap_ids)
}

emptygraph <- data.table(user.a=list(), user.b=list(), t=list())

cc.pairs <- getSimRes()
cu.pairs <- getSimRes("cu")

# ccfile <- cc.pairs[1]; cufile <- cu.pairs[1]
# increment=30; window=increment; st=365; mxinc=48

resolve <- function(cc.pairs, cu.pairs, increment=30, window=increment, st=365, mxinc=48) mapply(function(ccfile, cufile) {
  cc.EL <- readELtable(ccfile, max.uid)
  cu.EL <- readELtable(cufile, max.uid)
  count <- cc.EL[,length(unique(c(user.a,user.b)))]
  covert.ids <- data.table(user_id=max.uid+ 1:count, key = "user_id")
  covs <- rbind(cc.EL, cu.EL)[logout > arrive & login < depart][,list(user.a,user.b,login,logout)]
  combo.EL <- rbind(base.dt, covs)
  n <- min(round(max(combo.EL$logout)/60/60/24/increment), mxinc)
#  system.time(
  mclapply(1:n, function(inc) {
    sl = slice(combo.EL, st + (inc-1)*increment, st + (inc-1)*increment + window)
    if (dim(sl$res)[1] != 0) {
      gg <- graph(t(sl$res), directed=F)
      cs <- fastgreedy.community(gg)
      tmp <- rbindlist(lapply(communities(cs), function(comm) {
        data.table(t(combn(sort(comm), 2)))
      }))
      saveRDS(
        data.table(user.a = sl$mp[tmp$V1, user_id], user.b = sl$mp[tmp$V2, user_id]),
        gsub("process", "analyze", gsub("cc", sprintf("fgcomm-%02d",inc), ccfile))
      )
    } else {
      saveRDS(
        emptygraph,
        gsub("process", "analyze", gsub("cc", sprintf("fgcomm-%02d",inc), ccfile))
      )
    }
    
  }, mc.cores = detectCores()-1)
#  )
}, ccfile=cc.pairs, cufile=cu.pairs)

resolve(cc.pairs, cu.pairs)
