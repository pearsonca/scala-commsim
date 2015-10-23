#!/bin/env Rscript

## invoke from command line, w/ rscripts as wd

args <- commandArgs(trailingOnly = T)

if (length(args) == 0) args <- c('1000', '6', 'mid', 'med', 'middle','.')

sets <- as.integer(args[1])
count <- as.integer(args[2])
basedir <- args[6]

suppressPackageStartupMessages({
  require(data.table)
  require(stats4)
  require(methods)
})

refusers <- readRDS("../input/user.RData")
user_rows <- readRDS("../input/userPrefs.RData")
censor.dt <- readRDS("../input/censor.RData")
locs <- readRDS("../input/locClusters.RData")

compute <- function(n, lcat, pcat, vcat, rep=F) sample(
  locs[lifetime_cat == lcat & pwr_clust == pcat & vMFcluster == vcat],
  n, replace = rep
)

anticompute <- function(n, lcat, pcat, vcat, rep=F) sample(
  locs[lifetime_cat != lcat | pwr_clust != pcat | vMFcluster != vcat],
  n, replace = rep
)

invlogit <- function(a) 1/(1+exp(-a))

allpotentialusers <- refusers[lifetime_main == args[3] & pwr_main == args[4] & peak_main == args[5], user_id]		
refbino <- censor.dt[user_id %in% allpotentialusers, .N, by=list(user_id, login_day)][,		
  list(pbin = mean(sapply(N, function(n) min(n-1, 9)))/9),		
  keyby=user_id		
]		

refgamma <- censor.dt[user_id %in% allpotentialusers, list(diffs = diff(unique(sort(login_day)))), by=list(user_id)][,		
  as.list(exp(mle(		
      function(logk, logmu, diffs) -sum(dgamma(diffs, shape=exp(logk), scale=exp(logmu-logk), log=T)),		
        start=list(logk=0, logmu=log(max(mean(diffs),1))),		
        fixed=list(diffs=diffs)		
      )@coef)),		
  keyby=user_id		
]		
setnames(refgamma, c("logk","logmu"),c("shape","mean"))		

ressrc = user_rows[user_id %in% allpotentialusers,		
  list(.N, p = list(pref)),		
  keyby=list(user_id, lifetime_cat, pwr_clust, vMFcluster)		
][refbino][refgamma]

## save matched
matched <- compute(sets, args[3], args[4], args[5], T)
unmatched <- anticompute(sets, args[3], args[4], args[5], T)

write(matched, file=paste0(basedir, "/matchedlocs.txt"), ncolumns = 1)
write(unmatched, file=paste0(basedir, "/unmatchedlocs.txt"), ncolumns = 1)

for (i in 1:sets) {
  nm <- sprintf("%s/covert-set-%d.csv",basedir,i)
  if (file.create(nm)) config <- file(nm, open = "w")
  users <- sample(allpotentialusers, count)
  ressrc[user_id %in% users][,{
    things <- Reduce(function(left, right) {
      list(lc = c(left$lc, right$lc), ps = c(left$ps, right$ps))
    }, apply(.SD, 1, function(row) {
      list(lc = locs[lifetime_cat == row$lifetime_cat & pwr_clust == row$pwr_clust & vMFcluster == row$vMFcluster, sample(location_id, row$N)], ps = unlist(row$p))
    }))
    cat(shape[1], mean[1], pbin[1], things$lc, things$ps,"\n", file = config)
  },by=user_id]
  flush(config)
  close(config)
}

# rest of args is user preferences categories to sample from
