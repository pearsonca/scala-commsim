#!/usr/bin/env Rscript
rm(list=ls())
## invoke from command line, w/ rscripts as wd

# input/user.RData
# input/censor.RData
# input/locClusters.RData
# input/userPrefs.RData
# input/loc_probs.csv

suppressPackageStartupMessages({
  require(data.table)
  require(stats4)
  require(methods)
  require(optparse)
})

getseed <- function(chr) set.seed(as.integer(chr))

parse_args <- function(argv = commandArgs(trailingOnly = T)) {
  parser <- optparse::OptionParser(
    usage = "usage: %prog [data srcs] (low|mid|high) (lo|med|hi) (early|middle|late) N I",
    description = "visualize persistence community results",
    option_list = list(
      optparse::make_option(
        c("--verbose","-v"),  action="store_true", default = FALSE,
        help="verbose?"
      ),
      optparse::make_option(
        c("--target","-t"), default = "input",
        help="target base directory; default input"
      )
    )
  )
  req_pos <- list(
    users.dt=readRDS, censor.dt=readRDS, locrefs.dt=readRDS,
    loc_probs = fread, userPrefs.dt = readRDS,
    lft_cat=function(ar){
      stopifnot(grepl("(low|mid|high)", ar))
      ar
    },
    pwr_cat=function(ar){
      stopifnot(grepl("(lo|med|hi)", ar))
      ar
    },
    tm_cat=function(ar){
      stopifnot(grepl("(early|middle|late)", ar))
      ar
    },
    count=as.integer, seed=getseed
  )
  parsed <- optparse::parse_args(parser, argv, positional_arguments = length(req_pos))
  parsed$options$help <- NULL
  result <- c(mapply(function(f,c) f(c), req_pos, parsed$args, SIMPLIFY = F), parsed$options)
  if(result$verbose) print(result)
  result
}

invlogit <- function(a) 1/(1+exp(-a))

cat(with(parse_args(
#  high hi late 10 016
# c("input/digest/clustering/userrefs.rds", "input/digest/filter/detail_input.rds", "input/digest/clustering/locrefs.rds", "input/digest/filter/location_pdf.csv", "input/digest/clustering/uprefs.rds", "high", "hi", "late", "10", "016")
# c("input/digest/clustering/userrefs.rds", "input/digest/filter/detail_input.rds", "input/digest/clustering/locrefs.rds", "input/digest/filter/location_pdf.csv", "input/digest/clustering/uprefs.rds", "high", "hi", "late", "20", "001")
), {
  template_user_ids <- users.dt[
    (lifetime_main == lft_cat & pwr_main == pwr_cat & peak_main == tm_cat),
    user_id
  ]
  if (verbose) print(template_user_ids)

  binomial_meetings_distro <- censor.dt[
    user_id %in% template_user_ids,
    .N,
    by=list(user_id, login_day)
  ][,
    list(pbin = mean(sapply(N, function(n) min(n-1, 9)))/9),
    keyby=user_id
  ]

  gamma_usage_waiting_distro <- censor.dt[
    user_id %in% template_user_ids,
    list(diffs = diff(unique(sort(login_day)))),
    by=list(user_id)
  ][,{
    res <- as.list(exp(mle(
      function(logk, logmu, diffs) -sum(dgamma(diffs, shape=exp(logk), scale=exp(logmu-logk), log=T)),
      start=list(logk=0, logmu=log(max(mean(diffs),1))),
      fixed=list(diffs=diffs)
    )@coef))
    names(res) <- c("shape","mean")
    res
    },
    keyby=user_id
  ]

  locs <- locrefs.dt[location_id %in% loc_probs$V1]

  ressrc = userPrefs.dt[
    user_id %in% template_user_ids,
    list(.N, p = list(pref)),
    keyby=list(user_id, lifetime_cat, pwr_clust, vMFcluster)
  ][binomial_meetings_distro][gamma_usage_waiting_distro]

#  src <- if(complement=="matched") {
   src <- locs[(lifetime_cat == lft_cat & pwr_clust == pwr_cat & vMFcluster == tm_cat), location_id]
#  } else {
#    locs[!(lifetime_cat == lft_cat & pwr_clust == pwr_cat & vMFcluster == tm_cat), location_id]
#  }
  covertLoc <- sample(src, 1, replace = T)
  repl <- (count > length(template_user_ids))

  users <- data.table(user_id=sample(template_user_ids, count, replace = repl), new_user_id = 1:count, key="user_id")
  pre<-users[ressrc, allow.cartesian=T]
  
  ret <- pre[,{
    things <- Reduce(function(left, right) rbind(left, right),
     apply(.SD, 1, function(dtrow) {
       locs[
         lifetime_cat == dtrow$lifetime_cat & pwr_clust == dtrow$pwr_clust & vMFcluster == dtrow$vMFcluster,
         list(lc=sample(location_id, dtrow$N), ps=unlist(dtrow$p))
         ]
     }))
    paste(shape[1], mean[1], pbin[1], paste(things$lc, collapse = " "), paste(things$ps, collapse = " "), collapse = " ")
  }, by=new_user_id]$V1
  #browser()
  paste(c(sprintf("%d", covertLoc), ret), collapse = "\n")
}))