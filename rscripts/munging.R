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