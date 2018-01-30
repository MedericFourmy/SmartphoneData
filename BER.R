ID1=read.csv(file="ID1.txt",header = TRUE, sep=",")

ID1$duree=ID1$time
ID1$duree[1]=0.
for (i in 2:length(ID1$duree)){
  ID1$duree[i]=ID1$time[i]-ID1$time[i-1]
}
View(ID1)
summary(ID1)

hist(x = longitude,breaks = 20)

1254560773-1254493883
# Question 1 ----
# Une attaque de type « BeginEnd » qui essaye d'identifier les points d'intérêts d'un individu
# en découvrant des "trous" dans les traces de mobilité et en considérant le point d'arrivée 
#et de départ avant ce trou comme des points d'intérêts possibles. Voir les heuristiques du 
#cours GeoPrivacy pour plus de détails. 

# On veut un dataframe repertoriant les trous de plus de 20 minutes (72 000 secondes).
# On considere que l'utilisateur ne bouge pas s