## data munging utils
require(data.table); require(lubridate)
breakoutDays <- function(dt, ...) {
  secs_per_day <- 24*60*60
  setkey(dt[,
    user_id     := .GRP, by=user_id
  ][,
    location_id := .GRP, by=location_id
  ][,
    login_day   := login %/% secs_per_day
  ][,
    logout_day  := logout %/% secs_per_day
  ][,
    login_time  := login - login_day*secs_per_day
  ][,
    logout_time := logout - logout_day*secs_per_day
  ][,
    login_day_secs := ifelse(login_day == logout_day, logout_time - login_time, secs_per_day - login_time)
  ][,
    logout_day_secs := ifelse(login_day == logout_day, 0, logout_time)
  ], ...)
}

zeroize <- function(dt, z = dt[1, login-login_time], dz = dt[1, login_day], ks=key(dt), ref.yr=2004) {
  b <- ymd(paste(ref.yr, 01, 01))
  mul <- 24*3600
  dt[,
    weekday := wday(b+mul*(login_day-1), label = T)
  ][,
    week    := week(b+mul*(login_day-1))
  ][,
    month   := month(b+mul*(login_day-1), label = T)
  ]
  setkeyv(dt[,
    login := login - z
  ][,
    logout := logout - z
  ][,
    login_day := login_day - dz
  ][,
    logout_day := logout_day - dz
  ], ks)
}

trimLimits <- function(tar.dt, lim.dt) {
  res.dt <- merge(tar.dt, lim.dt, by = "location_id")[(login >= first) & (logout <= last),]
  res.dt$first <- res.dt$last <- NULL
  setkeyv(res.dt, key(tar.dt))
  res.dt
}

kmParse <- function(src, k, nstart = k, key, refevent = "login") {
  castdata <- reshape2::acast(src, eval(parse(text=paste0(key,"~hour+event"))), value.var = "norm", fill = 0)
  km <- stats::kmeans(castdata, k, nstart = nstart)
  cluster.dt <- data.table(cluster = km$cluster)
  cluster.dt[[key]] = as.integer(names(km$cluster))
  setkeyv(cluster.dt, key)
  peakhours <- setkey(src[cluster.dt][,
    list(peak=sum(hour*norm)/sum(norm)),
    by=list(event, cluster)
  ], event, peak)
  reorder <- peakhours[event == refevent, order(cluster)]
  peakhours[, cluster := reorder[cluster]]
  cluster.dt[,cluster := reorder[cluster]]
  return(list(clustering = cluster.dt, peaks = peakhours))
}

kmParse2 <- function(src, k, nstart = k, key, category) {
  castdata <- reshape2::acast(src, eval(parse(text=paste0(key,"~",category))), value.var = "distribution", fill = 0)
  km <- stats::kmeans(castdata, k, nstart = nstart)
  cluster.dt <- data.table(cluster = km$cluster)
  cluster.dt[[key]] = as.integer(names(km$cluster))
  setkeyv(cluster.dt, key)
  return(list(clustering = cluster.dt))
}