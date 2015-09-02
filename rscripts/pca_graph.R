## PCA plots in ggplot

require(ggplot2); require(data.table); require(reshape2)

process_hour_rle <- function(prefix) {
  hrsind <- 0:23
  nms <- sapply(hrsind, function(h) sprintf("%s_hr_%.2d", prefix, h))
  function(hrrle) with(hrrle, {
    missing <- hrsind[!(hrsind %in% values)]
    res <- c(lengths, rep.int(0, length(missing)))[order(c(values, missing))] / sum(lengths)
    names(res) <- nms
    res
  })
}

refpredictors <- {
  weekday_zeroes <- data.table(
    weekday=factor(c("Sun", "Mon", "Tues", "Wed","Thurs","Fri","Sat"), ordered = T),
    usage = 0, key = "weekday"
  )
  login_parser <- process_hour_rle("login")
  logout_parser <- process_hour_rle("logout")
  function(SD) with(SD, {
    duration <- logout - login
    total_login_time <- sum(duration)
    usage_by_weekday <- SD[, list(usage = sum(login_day_secs)), keyby=weekday][weekday_zeroes][, list(usage = ifelse(is.na(usage), i.usage, usage)), keyby=weekday]
    temp <- SD[, sum(logout_day_secs), keyby=weekday][weekday_zeroes][, list(usage = ifelse(is.na(usage), i.usage, usage)), keyby=weekday]
    usage_by_weekday$usage <- usage_by_weekday$usage + c(temp$usage[7], temp$usage[-7])
    store <- as.list(usage_by_weekday$usage / total_login_time)
    names(store) <- usage_by_weekday$weekday
    user_count <- length(unique(user_id))
    life <- max(logout) - min(login)
    login_hour <- as.integer(login_time / 60 / 60)
    login_hours <- login_parser(rle(sort(login_hour)))
    logout_hour <- as.integer(logout_time / 60 / 60)
    logout_hours <- logout_parser(rle(sort(logout_hour)))
#     ifelse(login_hour == logout_hour,ifelse(login_day == logout_day,{
#       
#     },{
#       
#     }),{
#       
#     })
    # breakdown usage proportion by hour
    #  - foreach entry
    #   - find end hour, begin hour
    #   -  if end == begin & day == same, that hour gets (logout - login)/3600
    #   -   ... day different, that hour gets seconds in that hour from login + logout / 3600, all other hours get 1
    #      if end != begin, begin < end
    #       ...each hour between begin and end gets 1, begin hour get 3600 - login secs, end hour gets end hour secs
    #      if end < begin,
    #       ... each hour after begin up to 23 gets 1, each hour before end gets 1, same end / begin allocation
    # breakdown usage proportion by month
    return(c(list(
      log10_lifetime = log10(life),
      ave_duration = mean(duration),
      med_duration = median(duration),
      sd_duration = sd(duration),
      max_duration = max(duration),
      total_user_time = total_login_time,
      log10_user_per_life = log10(user_count/life),
      log10_time_per_user = log10(total_login_time/user_count),
      log10_time_per_life = log10(total_login_time/life),
      log10_unique_users = log10(user_count)
    ), store, as.list(login_hours), as.list(logout_hours)))
  })
}

advpredictors <- {
  weekday_zeroes <- data.table(
    weekday=factor(c("Sun", "Mon", "Tues", "Wed","Thurs","Fri","Sat"), ordered = T),
    usage = 0, key = "weekday"
  )
  login_parser <- process_hour_rle("login")
  logout_parser <- process_hour_rle("logout")
  function(SD) with(SD, {
    duration <- logout - login
    total_login_time <- sum(duration)
    usage_by_weekday <- SD[, list(usage = sum(login_day_secs)), keyby=weekday][weekday_zeroes][, list(usage = ifelse(is.na(usage), i.usage, usage)), keyby=weekday]
    temp <- SD[, sum(logout_day_secs), keyby=weekday][weekday_zeroes][, list(usage = ifelse(is.na(usage), i.usage, usage)), keyby=weekday]
    usage_by_weekday$usage <- usage_by_weekday$usage + c(temp$usage[7], temp$usage[-7])
    store <- as.list(usage_by_weekday$usage / total_login_time)
    names(store) <- usage_by_weekday$weekday
    reduced_store <- list(weekend=store$Fri+store$Sat, midweek=store$Tues+store$Wed+store$Thurs, weekbegin = store$Sun+store$Mon)
    user_count <- length(unique(user_id))
    life <- max(logout) - min(login)
    login_hours <- login_parser(rle(sort(as.integer(login_time / 60 / 60))))
    logout_hours <- logout_parser(rle(sort(as.integer(logout_time / 60 / 60))))
    # breakdown usage proportion by hour
    # breakdown usage proportion by month
    return(c(list(
      log10_lifetime = log10(life),
      ave_duration = mean(duration),
      med_duration = median(duration),
      sd_duration = sd(duration),
      max_duration = max(duration),
      total_user_time = total_login_time,
      log10_user_per_life = log10(user_count/life),
      log10_time_per_user = log10(total_login_time/user_count),
      log10_time_per_life = log10(total_login_time/life),
      log10_unique_users = log10(user_count)
    ), reduced_store, as.list(login_hours), as.list(logout_hours)))
  })
}

dopcadecomp <- function(dt, sample_id = "location_id", training_portion = .8, predictors = refpredictors) {
  dtq <- parse(text=sprintf("unique(%s)",sample_id))
  unique_tars <- dt[,eval(dtq)]
  training <- sort(sample(unique_tars, size = length(unique_tars)*training_portion, replace = F))
  digested <- censor.dt[, predictors(.SD), by=location_id]
  centered <- digested[,lapply(.SD[,-1,with=F],function(col) col-mean(col)),]
  scaled <- centered[,lapply(.SD, function(col) col/sd(col))]
  return(within(list(training = scaled[training], validation = scaled[!training]),{
    pca <- prcomp(training, center=F, scale. = F)
    val <- predict(pca, validation)
  }))
}

pcaplot <- function(prcompres, limit = with(prcompres$pca, which.max(cumsum(sdev/sum(sdev)) > .8))) with(prcompres, {
  fcts <- paste0("PC", 1:limit)
  ref <- factor(fcts, levels=fcts, ordered=T)
  cols <- ref[seq(from=1, to=limit, by=2)]
  rows <- ref[seq(from=2, to=limit, by=2)]
  pc <- dim(pca$x)[1]
  (if(limit %% 2 == 0) {
    if (limit == 2) {
      data.table(col = cols, row = rows)
    } else data.table(col = cols[c(1, rep(2:length(cols), each=2))], row = rows[c(rep(1:(length(rows)-1), each=2), length(rows))])
  } else {
    if (limit == 3) {
      data.table(col = cols, row = rows)
    } else data.table(col = cols[c(1, rep(2:(length(cols)-1), each=2), length(cols))], row = rep(rows, each=2))
  }) -> template
  
  kcluster <- kmeans(pca$x[,1:limit], 5, nstart = 20)

  pred.src <- template[,list(x=pca$x[,col], y=pca$x[,row], cluster=factor(kcluster$cluster)), by=list(col, row)]
  ## on pca scale validation.dt[,-1,with=F][,(.SD-as.list(res$center))/as.list(res$scale)]
  pred.cluster <- apply(val[,1:limit],1,function(row) {
    which.min(colSums(row - t(kcluster$centers))^2)
  })
  valid.src <- template[,list(x=val[,col], y=val[,row], cluster=factor(pred.cluster)), by=list(col, row)]
  p <- ggplot(pred.src) + theme_bw() + facet_grid(row ~ col) + aes(x=x, y=y, color=cluster) + geom_point(aes(size="training", alpha="training")) + geom_point(aes(size="validation", alpha="validation"), data = valid.src) +
    scale_size_manual(limits=c("training","validation"), values=c(10,2)) + scale_alpha_manual(limits=c("training","validation"), values=c(0.05,1))
  
  vec.src <- template[,list(x=0, y=0,
    xend=pca$rotation[,col], yend=pca$rotation[,row],
    measure=dimnames(pca$rotation)[[1]]), by=list(col, row)]
  q <- ggplot(vec.src[abs(xend) > 0.2 | abs(yend) > 0.2]) + theme_bw() + facet_grid(row ~ col) + aes(x=x, y=y, xend = xend, yend = yend, label=measure) + geom_segment() + geom_text(aes(x=xend, y=yend))
  
  rot.melt <- data.table(melt(pca$rotation))
  b <- ggplot(
    merge(rot.melt[Var2 == "PC1", list(y=value), by=Var1], rot.melt[Var2 == "PC2", list(fill=value), by=Var1], by="Var1")
  ) + theme_bw() + theme(
    axis.text.x = element_text(angle=90)
  ) + aes(x=Var1, y=y, fill=fill) + geom_bar(stat="identity") 
  
  return(list(points = p, loadings = q))
  ## first, long format, rows odd components, cols even components
})

# prcompres <- dopcadecomp(censor.dt)
# p <- pcaplot(prcompres, limit=10)
# print(p$points)

pca_bars <- function(pca) with(pca, {
  molten <- data.table(melt(rotation))
  ggplot(
    merge(molten[Var2 == "PC1", list(y=value), by=Var1], molten[Var2 == "PC2", list(fill=value), by=Var1], by="Var1")
  ) + theme_bw() + theme(
    axis.text.x = element_text(angle=90)
  ) + aes(x=Var1, y=y, fill=fill) + geom_bar(stat="identity")
})