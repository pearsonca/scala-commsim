## data munging utils
require(data.table); require(lubridate)

loadRaw <- function(raw.src = "../input/merged.o", cache.src = "../input/raw.RData") {
  if (!file.exists(cache.src) || (file.mtime(raw.src) > file.mtime(cache.src))) {
    res <- setkey(
      setnames(
        fread(raw.src, header = F, sep=" ", colClasses = list(integer=1.4)),
        c("user_id", "location_id", "login", "logout")
      ),
      login, logout, user_id, location_id
    )
    saveRDS(res, cache.src)
    res
  } else readRDS(cache.src)
}

processRaw <- function(raw.dt,
  max_hours, min_logins, min_lifetime,
  min_users, min_loc_lifetime) {
  censor.dt <- raw.dt[(logout != login) & ((logout - login) <= max_hours*60*60), ]
  invalid.users <- censor.dt[,list(.N, lifetime = (max(logout) - min(login))/60/60/24), by=user_id ][N < min_logins | lifetime < min_lifetime, user_id]
  while(length(invalid.users) > 0) {
    censor.dt     <- censor.dt[!user_id %in% invalid.users]
    invalid.locs  <- censor.dt[,list(.N, uc=length(unique(user_id)), lifetime=(max(logout)-min(login))/60/60/24), by=location_id ][
      N < min_logins | uc < min_users | lifetime < min_loc_lifetime,
      location_id
    ]
    censor.dt     <- censor.dt[!location_id %in% invalid.locs]
    invalid.users <- censor.dt[,list(.N, lifetime = (max(logout) - min(login))/60/60/24), by=user_id ][N < min_logins | lifetime < min_lifetime, user_id]
  }
  censor.dt
}

loadCensored <- function(
  cache.src = "../input/raw.RData",
  cache.censor = "../input/censor.RData", ...) {
  if (!file.exists(cache.censor) || (file.mtime(cache.src) > file.mtime(cache.censor))) {
    res <- zeroize(breakoutDays(processRaw(...)[,
      user_id := .GRP, by=user_id
    ][,
      location_id := .GRP, by=location_id
    ], login_day, login_time, logout_day, logout_time, user_id, location_id))
    saveRDS(res, cache.censor)
    res
  } else readRDS(cache.censor)
}

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
  ][,
    login_hour := as.integer(login_time / 60 / 60)
  ][,
    logout_hour := as.integer(logout_time / 60 / 60)
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
  ][,
    year    := year(b+mul*(login_day-1))
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