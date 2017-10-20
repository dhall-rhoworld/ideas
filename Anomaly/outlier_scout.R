findCorrelatedVariables <- function(x, threshold = 0.5, use.var.names = FALSE) {
  
  # Finds pairs of numeric variables in the given data frame that are correlated.
  #
  # Args:
  #         x   : A data frame
  #   threshold : Threshold correlation coefficient.  A pair of variables is
  #               considered correlated if the absolute value of their correlation coefficient is
  #               greater than or equal to threshold.
  #
  # Returns:
  #   A two column matrix of pairs of variable indices
  
  df <- data.frame()
  count <- 0
  col.names <- colnames(x)
  for (i in 2:ncol(x)) {
    for (j in 1:(i-1)) {
      if (isTrulyNumeric(x, i) && isTrulyNumeric(x, j)) {
        correlated = FALSE
        tryCatch(
          {
            correlated <- abs(cor(x[,i], x[,j], use="pairwise.complete.obs")) >= threshold
          }, warning = function(w) {
            
          }, error = function(e) {
            
          }
        )
        if (correlated) {
          count <- count + 1
          if (count == 1) {
            if (use.var.names) {
              df <- data.frame(col.names[i], col.names[j], stringsAsFactors = FALSE)
            }
            else {
              df <- data.frame(i, j)
            }
            colnames(df) = c("v1", "v2")
          }
          else {
            if (use.var.names) {
              df <- rbind(df, c(col.names[i], col.names[j]))
            }
            else {
              df <- rbind(df, c(i, j))
            }
          }
        }
      }
    }
  }
  return(df)
}

isTrulyNumeric <- function(x, colNum) {
  
  # Determines if given column in data frame is truly numeric and not a date
  #
  # Args:
  #        x : A data frame
  #   colNum : Column number
  #
  # Returns:
  #   TRUE/FALSE
  
  return(is.numeric(x[,colNum]) &&
  ! grepl("date|month[^s]|year[^s]", colnames(x)[colNum], ignore.case = TRUE))
}

findUnivariateOutliers <- function(x, colNum) {
  
  v = na.omit(x[,colNum])
  
}

findBivariateOutliers <- function(x, col1, col2, cutoff.residual = 2, cutoff.density = 8) {
  
  # Finds bivariate outliers in two columns of a data frame.  A data point is
  # considered an outlier if its residual under linear regression is
  # unusually large and it occurs in a low density region of the data space.
  #
  # Args:
  #   x : A data frame
  #   col1 : A column number
  #   col2 : A column number
  #
  # Returns:
  #   A vector of boolean values
  
  # Structure that is returned
  outlierData = list()
  
  # Generate a new data frame with no NaN
  df <- x[c(col1, col2)]
  colnames(df) <- c("X", "Y")
  df$origRowNum <- seq.int(nrow(df))
  df <- na.omit(df)
  
  # Fit a linear model
  fit.y <- lm(Y ~ X, data = df)
  
  # Test if data are hetereoschadistic
  is.het <- ncvTest(fit.y)$p <= .05
  
  # Generate initial outlier index
  if (is.het) {
    message("   Data are hetereoschedastic.  Performing BoxCox transformation.")
    fit.bct <- BoxCoxTrans(df$Y)
    df["Y2"] <- predict(fit.bct, df$Y)
    fit.y2 = lm(Y2 ~ X, data = df)
    res <- abs(residuals(fit.y2))
    outlierData$het.fit = fit.bct
  }
  else {
    res <- abs(residuals(fit.y))
  }
  cutoff.res <- mean(res) + cutoff.residual * sd(res)
  if (is.het) {
    cutoff.alt <- (max(df$Y2) - min(df$Y2)) * 0.1
  }
  else {
    cutoff.alt <- (max(df$Y) - min(df$Y)) * 0.1
  }
  if (cutoff.res < cutoff.alt) {
    message("   Low magnitude of variance.  Using alternate cutoff for first phase of outlier detection.")
    cutoff.res <- cutoff.alt
  }
  outlier.index <- res >= cutoff.res
  
  # Remove index of putative outliers that are in high-density regions
  matrix.dist <- nndist(df$X, df$Y, k=1:5)
  mean.dist <- rowMeans(matrix.dist)
  cutoff.dist <- mean(mean.dist) + cutoff.density * sd(mean.dist)
  outlier.index <- outlier.index & mean.dist >= cutoff.dist
  
  # Fit boundary curve for heteroschedastic data
  if (is.het) {
    
    # Pick 50 points on regression line
    p = data.frame(X = seq(from = min(df$X), to = max(df$X), length.out = 100))
    p$Y = predict(fit.y2, p)
    
    # Calculate y coordinates of the outlier boundary line above and below points
    p$YHi = p$Y + cutoff.res
    p$YLo = p$Y - cutoff.res
    
    # Scale boundary y coordinates back to the original data range
    lambda = fit.bct$lambda
    p$YHi = (p$YHi * lambda + 1)^(1.0 / lambda)
    p$YLo = (p$YLo * lambda + 1)^(1.0 / lambda)
    
    outlierData$points.hi = p[c("X", "YHi")]
    outlierData$points.lo = p[c("X", "YLo")]
    colnames(outlierData$points.hi) = c("X", "Y")
    colnames(outlierData$points.lo) = c("X", "Y")
  }
  
  # Generate return outlier index vector with a value for each row in the input data frame
  returnVect <- logical(nrow(x))
  returnVect[df[outlier.index, "origRowNum"]] <- TRUE
  
  # Generate output structure
  outlierData$outlierIndex = returnVect
  outlierData$lm.fit = fit.y
  outlierData$is.het = is.het
  
  return(outlierData)
}

plotBivariateOutliers <- function(x, col1, col2, outlierData, inlier.col="green", outlier.col="red") {
  
  # Generates a scatter plot of inliers and outliers with a regression line and
  # outlier thresholds.
  #
  # Args:
  #             x : A data frame
  #          col1 : X-axis column
  #          col2 : Y-axis column
  #      outliers : A logical vector
  #    inlier.col : Color of inlier data points
  #   outlier.col : Color of outlier data points
  
  allPoints <- (x[, c(col1, col2)])
  colnames(allPoints) <- c("X", "Y")
  inliers <- na.omit(allPoints[!outlierData$outlierIndex,])
  outliers <- na.omit(allPoints[outlierData$outlierIndex,])
  allPoints <- na.omit(allPoints)
  colname.x <- colnames(x)[col1]
  colname.y <- colnames(x)[col2]
  plot(allPoints$X, allPoints$Y, col="white", xlab=colname.x, ylab=colname.y)
  points(inliers$X, inliers$Y, col=inlier.col)
  points(outliers$X, outliers$Y, col=outlier.col)
  if (outlierData$is.het) {
    lines(outlierData$points.hi$X, outlierData$points.hi$Y)
    lines(outlierData$points.lo$X, outlierData$points.lo$Y)
  }
  else {
    abline(a = fit$coefficients[1] + cutoff.res, b = fit$coefficients[2])
    abline(a = fit$coefficients[1] - cutoff.res, b = fit$coefficients[2])
  }
}