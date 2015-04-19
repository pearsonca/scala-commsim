## data munging utils
require(data.table)
breakoutDays <- function(dt) {
  dt[,
    user_id     := .GRP, by=user_id
  ][,
    location_id := .GRP, by=location_id
  ][,
    login_day   := login %/% (24*60*60)
  ][,
    logout_day  := logout %/% (24*60*60)
  ][,
    login_time  := login - login_day*24*60*60
  ][,
    logout_time := logout - logout_day*24*60*60
  ]
}

trimLimits <- function(tar.dt, lim.dt) {
  res.dt <- merge(tar.dt, lim.dt, by = "location_id")[(login >= first) & (logout <= last),]
  res.dt$first <- res.dt$last <- NULL
  setkeyv(res.dt, key(tar.dt))
  res.dt
}