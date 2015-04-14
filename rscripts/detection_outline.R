## detection schemes
req.packages <- c("data.table", "argparser", "ggplot2")
sapply(req.packages, require, character.only = T)

a.parser <- arg.parser("Run Detection approaches against synthetic data", "detector")
a.parser <- add.argument(a.parser, "--src", "source data", default = "../input/censored.Rdata")
a.parser <- add.argument(a.parser, "target", "target synthetic data", type="character")
a.parser <- add.argument(a.parser, "--out", "the output file; will default to out-$tar", type="character")
argv <- parse.args(a.parser, c("../output/runs-1.csv"))

load(argv$src)
samples.dt <- fread(argv$target, colClasses = "integer")
setnames(samples.dt, c("run_id", "sample_id", "user_id", "location_id", "login", "logout"))
breakoutDays(samples.dt[,
  user_id := user_id + max(censor.dt$user_id)+1L
][,
  target := TRUE
])[, login_day := login_day + min(censor.dt$login_day)
  ][,
     logout_day := logout_day + min(censor.dt$login_day)
]
setkeyv(samples.dt, key(censor.dt))

limits <- censor.dt[,list(first=min(login), last=max(logout)), keyby=location_id]
samp.dt <- merge(samples.dt, limits, by = "location_id")
samp.dt <- samp.dt[(login >= first) & (logout <= last),]
samp.dt$first <- samp.dt$last <- NULL
samp.dt[, user_id := user_id + max(censor.dt$user_id)]
setkeyv(samp.dt, key(samples.dt))

stopifnot(
  samples.dt[,
    list(login_count = .N),
    by=list(sample_id, user_id, login_day)
  ][, max(login_count) ] == 1
) ## assert that the simple outputs have at most one login per day

method_1_eval <- function(dt, start, width)
  dt[(start <= login_day) & (login_day < start + width),
    list(login_count = .N),
    by=list(user_id, login_day)
  ][,
    list(atmost_one = all(login_count <= 1), seen = any(login_count > 0)),
    by=user_id
  ]

method_1 <- function(dt, width = 14, start = 4*365, max_increments = 6) {
  init.dt <- dt[,list(atmost_one = T, seen = F), by=user_id]
  slices <- Map(function(i) method_1_eval(dt, start+i*width, width), 0:max_increments)
  reduction <- Reduce(
    function(l.dt, r.dt) {
      m <- merge(l.dt, r.dt, by="user_id", all.x = T)
      m[is.na(atmost_one.y), atmost_one.y := TRUE][is.na(seen.y), seen.y := FALSE]
      m[, list(atmost_one = atmost_one.x & atmost_one.y, seen = seen.x | seen.y), by=user_id]
    },
    x = slices,
    init = init.dt, 
    accumulate = T
  )
  reduction[[1]] <- NULL
  res.dt <- data.table(hits = sapply(reduction, function(dt) dt[,sum(atmost_one & seen)]))
  res.dt[, inc := .I-1]
}

# method_1a <- function(dt, width, start = min(dt$login_day)+4*365, max_increments = 6, target_pop = 25) {
#   i <- 0
#   target_users <- method_1_target(method_1_slice(dt, start+i*width, width))
#   while((i < max_increments) & (length(target_users) > target_pop)) {
#     i <- i+1
#     target_users <- intersect(target_users, method_1_target(method_1_slice(dt, start+i*width, width)))
#   }
#   list(targets=target_users, inc=i)
# }

## detection should not be applied to multiple run configurations at once, only multiple samples

analyzer <- function(period) Reduce(rbind, lapply(samp.dt[,unique(sample_id)], function(sid) {
  res <- method_1(samp.dt[sample_id == sid], period)
  res[, sample_id := sid ][, period := period ]
}))

analyzed <- Reduce(rbind, Map(analyzer, seq(from=7,to=28,by=7)))

p <- ggplot(analyzed) + aes(x = inc*period, y = hits, group=sample_id) + facet_grid(. ~ period, scales = "free_x", space = "free_x") + geom_line(alpha=0.5)
p + labs(x="day from start") + theme_bw()
