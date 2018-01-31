library(car)
library(caret)
library(spatstat)

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
firstDataCol <- as.integer(args[4])

# (Arg 5): Standard deviations from regression line
sdResidual <- as.numeric(args[5])

# (Arg 6): Number of nearest neighbors to compute
numNearestNeighbors <- as.integer(args[6])

# (Arg 7): Standard deviations below mean density
sdDensity <- as.numeric(args[7])

#
# Values use in testing
#
# infile <- "C:/RhoVer/Working/test-in.csv"
# outfile <- "C:/RhoVer/Working/test-out.csv"
# paramfile <- "C:/RhoVer/Working/test-param.csv"
# firstDataCol <- 4
# sdResidual <- 2
# numNearestNeighbors <- 5
# sdDensity <- 6

# Configure input data formats.  All columns to the left
# of the first one being checked will be considered ID fields
# and treated as CHARACTER.  Columns being checked will be
# treated as NUMERIC.
classes <- character()
for (i in 1:(firstDataCol - 1)) {
  classes <- c(classes, "character")
}
classes <- c(classes, "numeric")
classes <- c(classes, "numeric")

# Read in input
df <- read.csv(infile, colClasses = classes)

# Fit a linear model
X <- df[, firstDataCol]
Y <- df[, firstDataCol + 1]
fitY <- lm(Y ~ X)

# If data are heteroschedastic, perform Box-Cox transform
areHet <- ncvTest(fitY)$p <= .05
#areHet <- FALSE
lambda <- NaN
if (areHet) {
  fitBct <- BoxCoxTrans(Y)
  Y2 <- predict(fitBct, Y)
  fitY = lm(Y2 ~ X)
  lambda <- fitBct$lambda
}

# Compute residual cutoff value
res <- abs(residuals(fitY))
cutoffRes <- mean(res) + sdResidual * sd(res)

# Make initual outlier calls based on residual cutoff
outlierIndex <- res > cutoffRes

# Compute mean distances to K nearest neighbors
distMatrix <- nndist(X, Y, k=1:numNearestNeighbors)
meanDistances <- rowMeans(distMatrix)

# Compute density cutoff value
cutoffDensity <- mean(meanDistances) + sdDensity * sd(meanDistances)

# Refine outlier calls by applying density cutoff
outlierIndex <- outlierIndex & meanDistances > cutoffDensity

# Write outlier output file
df$Is_Outlier <- outlierIndex
write.csv(df, file = outfile, row.names = FALSE, quote = FALSE)

# Generate output for statistical properties file
statProps <- data.frame(heteroschedastic = c(areHet))
statProps$slope <- fitY$coefficients[2]
statProps$intercept <- fitY$coefficients[1]
statProps$cutoff_residual <- cutoffRes
if (!is.nan(lambda)) {
  statProps$lambda <- lambda
}
write.csv(statProps, file = paramfile, row.names = FALSE, quote = FALSE)
