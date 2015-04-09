## detection schemes
require(argparser)
a.parser <- arg.parser("Run Detection approaches against synthetic data", "detector")
a.parser <- add.argument(a.parser, "--src", "source data", default = "../input/censored.Rdata")
a.parser <- add.argument(a.parser, "target", "target synthetic data", type="character")
a.parser <- add.argument(a.parser, "--out", "the output file; will default to out-$tar", type="character")
argv <- parse.args(a.parser, c("../output/test-1.csv"))

load(argv$src)
samples.dt <- fread(argv$target, colClasses = "integer")
setnames(samples.dt, c("run_id", "sample_id", "user_id", "location_id", "login", "logout"))
breakoutDays(samples.dt[,
  user_id := user_id + max(censor.dt$user_id)+1L
][,
  target := TRUE
])
setkeyv(samples.dt, key(censor.dt))

meld.dt <- rbind(censor.dt, samples.dt, use.names = TRUE)

synthesize <- function(rid, sid, combo.dt = meld.dt) {
  res.dt <- combo.dt[
    ((run_id == 0) & (sample_id == 0)) | ((run_id == rid) & (sample_id == sid)),
    list(user_id, location_id, login_day, login_time, logout_day, logout_time, target)
  ]
  setkey(res.dt, login_day, login_time, logout_day, logout_time, user_id, location_id)
  
  res.dt[,
    user_id := .GRP, by=user_id
  ][,
    location_id := .GRP, by=location_id
  ]
}

view <- function(synthesized.dt) {
  synthesized.dt[,list(user_id, location_id, login_day, login_time, logout_day, logout_time)]
}

asynth.dt <- synthesize(1, 1)
aview.dt <- view(asynth.dt)

method_1_slice <- function(dt, start, width) {
  dt[(start <= login_day) & (login_day < start + width)]
}

method_1_target <- function(slice.dt)
  slice.dt[,
    list(login_count = .N),
    by=list(user_id, login_day)
  ][,
    list(only_one = all(login_count == 1)),
    by=user_id
  ][only_one == T,
    user_id
  ]

method_1 <- function(dt, width, start = min(dt$login_day)+4*365, max_increments = 6, target_pop = 25) {
  i <- 0
  target_users <- method_1_target(method_1_slice(dt, start+i*width, width))
  while((i < max_increments) & (length(target_users) > target_pop)) {
    i <- i+1
    target_users <- intersect(target_users, method_1_target(method_1_slice(dt, start+i*width, width)))
  }
  list(targets=target_users, inc=i)
}

method_1a <- function(dt, width, start = min(dt$login_day)+4*365, max_increments = 6, target_pop = 25) {
  i <- 0
  target_users <- method_1_target(method_1_slice(dt, start+i*width, width))
  while((i < max_increments) & (length(target_users) > target_pop)) {
    i <- i+1
    target_users <- intersect(target_users, method_1_target(method_1_slice(dt, start+i*width, width)))
  }
  list(targets=target_users, inc=i)
}

for (rid in meld.dt[run_id != 0, unique(run_id)]) for (sid in meld.dt[run_id == rid, unique(sample_id)]) {
  synth.dt <- synthesize(rid, sid, meld.dt)
  targets <- synth.dt[target == T,]
  view.dt <- view(synth.dt)
  target_14 <- method_1(view.dt, 14)
  covert_targets <- target_14$targets
#  target_21 <- method_1(view.dt, 21)
#  target_28 <- method_1(view.dt, 28)
  misses_ref <- targets[,length(unique(user_id))]
  hits <- targets[user_id %in% covert_targets, length(unique(user_id))]
  misses <- misses_ref - hits
  collateral <- length(covert_targets)-hits
  print("14:")
  print(c(
    steps = target_14$inc,
    run_id = rid,
    sample_id = sid,
    hits=hits,
    collateral=collateral,
    misses = misses
  ))
}