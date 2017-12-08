library(sas7bdat)

file1 <- "S:/RhoFED/CTOT-SACCC/CTOT/CTOT-08-Abecassis/Stats/Data/Clinical/vitlmstr.sas7bdat"
file2 <- "S:/RhoFED/CTOT-SACCC/CTOT/CTOT-08-Abecassis/Stats/Data/Clinical/chemmstr.sas7bdat"
mergeCol <- "ID"

df1 <- read.sas7bdat(file1)
df2 <- read.sas7bdat(file2)

df3 <- merge(df1, df2, by=mergeCol)

cor(df3)
