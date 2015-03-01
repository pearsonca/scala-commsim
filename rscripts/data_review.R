## analysis and plots of montreal data

require(data.table); require(ggplot2)

src.dt <- data.table(read.csv("~/Dropbox/montreal/merged.o", header = F))
setnames(src.dt, c("user_id","location_id","login","logout"))

max_real_duration <- 60*60*24 # at most, continuous login of a day

duration.censored.dt <- src.dt[(logout - login) <= max_real_duration,]

user.location.count.dt <- duration.censored.dt[,list(location_count = length(unique(location_id))), by="user_id"]
valid.users <- user.location.count.dt[location_count > 1,user_id]

user_and_duration.censored.dt <- duration.censored.dt[user_id %in% valid.users,]

locs.dt <- user_and_duration.censored.dt[, list(unique_users = length(unique(user_id)), total_login_time = sum(logout - login)), by="location_id"] # unique users per location

invalid.locs <- locs.dt[(unique_users == 1) | (total_login_time < 60*60), location_id] # locations with only one user, or total log in time < 1 hour

loc_user_duration.censored.dt <- user_and_duration.censored.dt[!(location_id %in% invalid.locs),]

loc_view.dt <- loc_user_duration.censored.dt[,
  list(
    unique_users = length(unique(user_id)),
    total_login_time = sum(logout - login),
    mean_login = mean(logout - login),
    sd_login = sd(logout - login)
  ),
  by="location_id"]
# sum(loc_user_duration.censored.dt[,length(unique(location_id)),by="user_id"]$V1 == 1) == 0 # => no more user ids to censor

p <- ggplot(loc_user_duration.censored.dt) + theme_bw()

p + aes(x=(logout - login)) + geom_bar() + geom_vline(x=150, color="red") + scale_x_log10()

#####
p + aes(x=unique_users) + geom_bar() + scale_x_log10()
p + aes(x=total_login_time) + geom_bar() + scale_x_log10()

src.dt[,length(unique(user_id)),by="location_id"] # unique users per location
src.dt[,length(unique(location_id)),by="user_id"] # unique locations by user

regular_users <- src.dt[,list(regular = length(location_id) > 1), by="user_id"] # users w/ more than one login
regular_users <- regular_users[regular == T, user_id, keyby="user_id"]$user_id