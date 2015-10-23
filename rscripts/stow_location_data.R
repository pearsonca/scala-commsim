
source("munging.R")

max_hours <- 24
min_logins <- 2;    min_users <- 6 # 6 user min remove abberant location
min_lifetime <- 30; min_loc_lifetime <- 30 # days
censor.dt <- loadCensored(
  raw.dt = loadRaw(),
  max_hours = max_hours, min_logins = min_logins, min_lifetime = min_lifetime,
  min_users = min_users, min_loc_lifetime = min_loc_lifetime
)

require(stats4); require(reshape2)

# output <- censor.dt[, list(pois_mean = exp(mle(
#     function(logl, diffs) { 
#       -sum(dpois(diffs, exp(logl), log=T))
#     },
#     start=list(logl=0),
#     fixed=list(diffs=(logout-login))
#   )@coef), usage = sum(logout-login)), keyby=list(location_id, login_hour)]

output <- censor.dt[, c(as.list({
  if (.N > 5)
    res <- exp(mle(
      function(logk, logmu, diffs) { 
        -sum(dgamma(diffs, shape=exp(logk), scale=exp(logmu-logk), log=T))
      },
      start=list(logk=0, logmu=log(mean(logout-login))),
      fixed=list(diffs=(logout-login)
    ))@coef)
  else res <- c(NaN, mean(logout-login))
  names(res) <- c("shapes", "means")
  res
}), usage = sum(logout-login)), keyby=list(location_id, login_hour)]

# output[shapes == NaN] returns ~10% of rows, however only ~.1 of usage.  So: elimate.
# discard times w/ login durations cannot be determined

filled_pdf <- dcast.data.table(output[!is.nan(shapes),{
  res <- rep(0, length.out=24)
  res[login_hour+1] <- usage / sum(usage)
  list(hour=0:23, prop = res)
}, keyby=location_id], location_id ~ hour, value.var = "prop")

write.table(filled_pdf, file="../input/loc_probs.csv", sep=",", row.names = F, col.names = F)

filled_means <- dcast.data.table(output[!is.nan(shapes),{
  res <- rep(0, length.out=24)
  res[login_hour+1] <- means
  list(hour=0:23, means = res)
}, keyby=location_id], location_id ~ hour, value.var = "means")

write.table(filled_means, file="../input/loc_means.csv", sep=",", row.names = F, col.names = F)

filled_shapes <- dcast.data.table(output[!is.nan(shapes),{
  res <- rep(0, length.out=24)
  res[login_hour+1] <- shapes
  list(hour=0:23, shapes = res)
}, keyby=location_id], location_id ~ hour, value.var = "shapes")

write.table(filled_shapes, file="../input/loc_shapes.csv", sep=",", row.names = F, col.names = F)
