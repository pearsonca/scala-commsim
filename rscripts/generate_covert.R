require(data.table); require(stats4)
censor.dt <- readRDS("../input/censor.RData")

censor.dt <- censor.dt[!(user_id == 2470 & logout == 139740291)]

## for these fits, require that relevant users have more than ten unique login days,
# and more than two unique login locations

remove_users <- censor.dt[,list(
  udays = length(unique(login_day)),
  ulocs = length(unique(location_id))
), by=user_id][(udays <= 10) | (ulocs <= 2), user_id]
subset.censor.dt <- censor.dt[!(user_id %in% remove_users)]

pre_same_day <- subset.censor.dt[,
  list(del=diff(login_time)), keyby=list(user_id, login_day)
][,
  list(del=c(del), len=length(c(del))),
  by=user_id
]

## only calculate same day waiting times for users that at least ten instances of same day log-ins

same_day_waiting_times <- pre_same_day[len > 10, as.list({
  mu <- mean(del)
  res <- try(exp(mle(
    function(logk, logmu, diffs) -sum(dgamma(diffs, shape=exp(logk), scale=exp(logmu-logk), log=T)),
    start=list(logk=0, logmu=log(mu)),
    fixed=list(diffs=del)
  )@coef))
  if (class(res) == "try-error") res <- c(NaN, mu)
  names(res) <- c("shape", "mean")
  res
}), by=user_id]

pre_between_day <- subset.censor.dt[,
                                    list(diffs=diff(sort(unique(login_day)))),
                                    by=list(user_id)
                                    ]

between_day_waiting_times <- pre_between_day[, as.list({
  mu <- mean(diffs)
  if (.N > 1) {
    res <- try(exp(mle(
      function(logk, logmu, diffs) -sum(dgamma(diffs, shape=exp(logk), scale=exp(logmu-logk), log=T)),
      start=list(logk=0, logmu=log(mu)),
      fixed=list(diffs=diffs)
    )@coef))
    if (class(res) == "try-error") res <- c(NaN, mu)
  } else res <- c(NaN, mu)
  names(res) <- c("shape", "mean")
  res
}), by=user_id]

## TODO cache this; also, alt model?
waiting_times <- censor.dt[,list(dd=diff(login)), keyby=user_id][, as.list({
  if (.N > 5) {
    res <- try(exp(mle(
      function(logk, logmu, diffs) -sum(dgamma(diffs, shape=exp(logk), scale=exp(logmu-logk), log=T)),
      start=list(logk=0, logmu=log(mean(dd))),
      fixed=list(diffs=dd)
    )@coef))
    if (class(res) == "try-error") res <- c(NaN, mean(dd))
  } else res <- c(NaN, mean(dd))
  names(res) <- c("shape", "mean")
  res
}), keyby=user_id]

plot(NULL, NULL, xlim=c(3,8), ylim=c(0,1), xlog=T, ylab="CDF", xlab="log10(waiting times)")

thing[, {
  x <- 10^seq(3,8,length.out = 100)
  y <- pgamma(x, shape, scale=mean/shape)
  lines(log10(x), y, col=rgb(0,0,0,0.1))
}, by=location_id]

#require(ggplot2)
ggplot(waiting_times[user_src.dt]) + facet_grid(pwr_main ~ lifetime_main, labeller = function(var, val){
  if (var == "pwr_main") {
    ifelse(val == "lo", "smooth usage", ifelse(val=="med","some usage variability","highly variable usage"))
  } else {
    ifelse(val == "low", "low usage per time", ifelse(val=="mid","typical usage per time","high usage per time"))
  }
}) + aes(x=log10(pois_rate/3600/24), fill=peak_main) + geom_bar() + theme_bw() + labs(x="log10(inter-event days)",color="usage\npeak")