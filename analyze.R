#!/usr/bin/env Rscript

#setwd("~/git/scala-commsim/")
rm(list=ls())
require(data.table)
require(igraph)
require(parallel)
args <- commandArgs(trailingOnly = T)
args <- c("output/process-test", "output/analyze-test", "input")

getSimRes <- function(tar="cc") list.files(args[1], pattern = paste0(tar,".csv"), full.names = T)

# cnt <- args[1]
# inter <- args[2]
# locs <- args[3]

# getCCPairs <- function(count, intervals, locations) list.files(path = "./cc_files", pattern=paste(count, intervals, locations, ".+cc", sep = "-"))
# getCUPairs <- function(count, intervals, locations) list.files(path = "./cu_files", pattern=paste(count, intervals, locations, ".+cu", sep = "-"))

src.dt <- readRDS(paste0(args[3],"/raw-input.rds"))

#src.dt <- src.dt[delta != 0,]
max.uid <- src.dt[,max(user_id)]+1
min.login <- src.dt[,min(login)]
oldorig <- trunc(as.POSIXlt("2005-01-01", tz="EST"), "days")
neworig <- trunc(as.POSIXlt(min.login, origin = "2005-01-01", tz = "EST"), "days")
del <- as.numeric(neworig)-as.numeric(as.POSIXlt("2005-01-01"))

readELtable <- function(tar, offsetid) {
  res <- data.table(fread(tar,
                          header = F, col.names = c("user.a", "user.b", "location_id", "login", "logout", "type")),
                    key = c("login", "logout", "location_id", "user.a", "user.b")
  )
  res[user.a < 0, user.a := -user.a + offsetid]
  res[user.b < 0, user.b := -user.b + offsetid]
  res[user.b < user.a, `:=`(user.b = user.a, user.a = user.b)]
  res[,list(user.a, user.b),keyby=list(login=login-del,logout=logout-del)]
}

base.dt <- readRDS(paste0(args[3],"/raw-pairs.rds"))
base.dt[
  user.b < user.a,
  `:=`(user.b = user.a, user.a = user.b)
]

base.dt <- setcolorder(base.dt[,list(user.a=user.a+1, user.b=user.b+1),keyby=list(login=start-del,logout=end-del)], c("user.a","user.b","login","logout"))

slice <- function(el.dt, start.day, end.day) {
  res <- el.dt[(logout < end.day*24*3600) & (login > start.day*24*3600), .N, keyby=list(user.a, user.b)]
  remap_ids <- res[,list(user_id=unique(c(user.a,user.b)))][, new_user_id := .I, keyby=user_id]
  relabelled <- data.table(user.a=remap_ids[res[,list(user_id=user.a)]]$new_user_id, user.b=remap_ids[res[,list(user_id=user.b)]]$new_user_id)
  list(res=relabelled, mp=remap_ids)
}

resolve <- function(cc.pairs, cu.pairs, increment=30, st=365) mapply(function(cc.file, cu.file) {
  cc.EL <- readELtable(cc.file, max.uid)[,list(user.a,user.b,login,logout)]
  cu.EL <- readELtable(cu.file, max.uid)[,list(user.a,user.b,login,logout)]
  count <- cc.EL[,length(unique(c(user.a,user.b)))]
  covert.ids <- max.uid + 1:count
  combo.EL <- rbindlist(list(base.dt, cc.EL, cu.EL))
  days <- seq(st,5*365,by=increment)
  res <- mclapply(days, function(day) {
    sl = slice(combo.EL, day-increment, day)
    gg <- graph(t(sl$res), directed=F)
    cs <- fastgreedy.community(gg)
    members <- membership(cs)[sl$mp[user_id %in% covert.ids]$new_user_id]
    comm.counts <- sizes(cs)[members]
    cat(day, "\n")
    list(m=members,co=comm.counts,d=day)
  })
  members.wh <- file(gsub("cc\\.csv", "members.csv", cc.file), open = "a")
  sizes.wh <- file(gsub("cc\\.csv", "sizes.csv", cc.file), open = "a")
  lapply(res, function(l) with(l,{
    write(c(d,m), members.wh, ncolumns = count+1, append = T)
    write(c(d,co), sizes.wh, ncolumns = count+1, append = T)
  }))
  flush(members.wh)
  flush(sizes.wh)
  close(members.wh)
  close(sizes.wh)
}, cc.file=cc.pairs, cu.file=cu.pairs)

cc.pairs <- getSimRes()
cu.pairs <- getSimRes("cu")

# cc.file <- cc.pairs[1]
# cu.file <- cu.pairs[1]
# day <- 2*365

resolve(cc.pairs, cu.pairs)
