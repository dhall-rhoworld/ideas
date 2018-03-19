#
# Read in arguments from command line
#
args <- commandArgs(trailingOnly=TRUE)

# (Arg 1): Input file path
infile <- args[1]

# (Arg 2): Path to main output file
outfile <- args[2]

# (Arg 3): Path to secondary output file that will contain
#          computed statistical parameters
paramfile <- args[3]

# (Arg 4): Column number of input file that contains values to check
fieldnum <- as.integer(args[4])

# (Arg 5): SD boundary between inliers and outliers
sdCutoff <- as.numeric(args[5])

#
# Values use in testing
#
# infile <- "C:/RhoVer/Working/test-in.csv"
# outfile <- "C:/RhoVer/Working/test-out.csv"
# paramfile <- "C:/RhoVer/Working/test-param.csv"
# fieldnum <- 5
# sdCutoff <- 2

# Configure input data formats.  All columns to the left
# of the one being checked will be considered ID fields
# and treated as CHARACTER.  Column being checked will be
# treated as NUMERIC.
classes <- character()
for (i in 1:(fieldnum - 1)) {
  classes <- c(classes, "character")
}
classes <- c(classes, "numeric")

# Read in input
df <- read.csv(infile, colClasses = classes)

# Compute mean of checked field
field <- as.numeric(df[,fieldnum])
fieldMean <- mean(field, na.rm = TRUE)

# Compute distance of each data point from mean
deltas <- abs(field - fieldMean)

# Compute SD of distances
fieldSd <- sd(field, na.rm = TRUE)

# Identify outliers and add additional logical column to
# right of the checked column
cutoff <- fieldSd * sdCutoff
df$outliers <- deltas > cutoff

# Write main output
write.csv(df, file = outfile, row.names = FALSE, quote = FALSE)

# Write mean and SD values to secondary output file
params <- data.frame(mean = c(fieldMean), sd = c(fieldSd))
write.csv(params, paramfile, row.names = FALSE, quote = FALSE)
