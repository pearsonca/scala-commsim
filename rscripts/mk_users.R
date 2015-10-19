#!/usr/bin/Rscript

## invoke from command line, w/ rscripts as wd
## follow invocation w/
## mv covert-set-*.csv ../input/$SUBDIRFORTHISSET/

args <- commandArgs(trailingOnly = T)

if (length(args) == 0) args <- c('1000', '6', 'mid', 'med', 'middle')

sets <- as.integer(args[1])
count <- as.integer(args[2])

#suppressPackageStartupMessages({
  require(data.table)
  require(stats4)
  require(methods)
#})

refusers <- readRDS("../input/user.RData")
user_rows <- readRDS("../input/userPrefs.RData")
censor.dt <- readRDS("../input/censor.RData")
locs <- readRDS("../input/locClusters.RData")

compute <- function(n, lcat, pcat, vcat) sample(locs[lifetime_cat == lcat & pwr_clust == pcat & vMFcluster == vcat], n)

invlogit <- function(a) 1/(1+exp(-a))

for (i in 1:sets) {
  if (file.create(sprintf("covert-set-%d.csv",i))) config <- file(sprintf("covert-set-%d.csv",i), open = "w")
  users <- refusers[lifetime_main == args[3] & pwr_main == args[4] & peak_main == args[5], sample(user_id, count)]
  refbino <- censor.dt[user_id %in% users, .N, by=list(user_id, login_day)][,
    list(pbin = mean(sapply(N, function(n) min(n-1, 9)))/9),
    keyby=user_id
  ]
  refgamma <- censor.dt[user_id %in% users, list(diffs = diff(unique(sort(login_day)))), by=list(user_id)][,
   as.list(exp(mle(
     function(logk, logmu, diffs) -sum(dgamma(diffs, shape=exp(logk), scale=exp(logmu-logk), log=T)),
     start=list(logk=0, logmu=log(max(mean(diffs),1))),
     fixed=list(diffs=diffs)
    )@coef)),
    keyby=user_id
  ]
  setnames(refgamma, c("logk","logmu"),c("shape","mean"))
  res <- user_rows[user_id %in% users,
    list(.N, p = list(pref)),
    keyby=list(user_id, lifetime_cat, pwr_clust, vMFcluster)
  ][refbino][refgamma]
  res[,{
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