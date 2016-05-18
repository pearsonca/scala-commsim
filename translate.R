# translate.R raw sim output => new user_ids (negatives), original location ids, correct times

args <- commandArgs(trailingOnly = T)
# args <- c("output/matched/mid/lo/late/10/001-covert-out.csv", "20649600")


require(data.table)

orig <- fread(args[1])

orig$V1 <- -orig$V1
offset <- as.integer(args[2])
orig$V3 <- orig$V3 + offset
orig$V4 <- orig$V4 + offset

write.table(orig, file = stdout(), row.names = F, col.names = F, sep = ",", quote = F)
