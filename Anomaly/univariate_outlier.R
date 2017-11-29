args <- commandArgs(trailingOnly=TRUE)
infile <- args[1]
outfile <- args[2]
paramfile <- args[3]
fieldnum <- as.integer(args[4])
sdCutoff <- as.numeric(args[5])
# 
# infile <- "C:/RhoVer/Working/test-in.csv"
# outfile <- "C:/RhoVer/Working/test-out.csv"
# paramfile <- "C:/RhoVer/Working/test-param.csv"
# fieldnum <- 3
# sdCutoff <- 2
# 

df <- read.csv(infile)
field <- as.numeric(df[,fieldnum])
fieldMean <- mean(field, na.rm = TRUE)
deltas <- abs(field - fieldMean)
fieldSd <- sd(field, na.rm = TRUE)
cutoff <- fieldSd * sdCutoff
outliers <- deltas > cutoff
write.csv(outliers, file = outfile)
params <- data.frame(mean = c(fieldMean), sd = c(fieldSd))
write.csv(params, paramfile)
