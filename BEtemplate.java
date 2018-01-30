/*******************************************************/
/* Ce fichier donne les bases pour le BE de Privacy    */
/* Auteur: M.O. Killijian                              */
/* Source: http://homepages.laas.FR/mkilliji/BEPrivacy */
/* Fichiers de traces : même adresse                   */
/*******************************************************/

import java.lang.Math.*;
import java.lang.Math;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

/* La classe principale utilise des inner classes pour ce prototype */
public class BEtemplate {

	/* LatLon: une localisation et son timestamp ainsi que des opérations dessus */
	class LatLon implements Comparable<LatLon> {
		public Integer time;
		public double lat;
		public double lon;
		public LatLon(Integer t, double x, double y) {
			time=t;
			lat=x;
			lon=y;
		}
		public int compareTo(LatLon o) {
			return time.compareTo(o.time);
		}
		double toRadians(double a) {
			return a*Math.PI/180;
		}
		double distanceTo(LatLon o) {
			if (lat==o.lat && lon==o.lon) 
				return 0.0;
			double er = 6366.707;
			double latFrom = toRadians(lat);
			double latTo = toRadians(o.lat);
			double lngFrom = toRadians(lon);
			double lngTo = toRadians(o.lon);
			return Math.acos(Math.sin(latFrom) * Math.sin(latTo) + Math.cos(latFrom) * Math.cos(latTo) * Math.cos(lngTo - lngFrom))* er;
		}
		int timeDiff(LatLon o) {
			return time-o.time;
		}
		double speedFrom(LatLon o) {
			return distanceTo(o)/timeDiff(o);
		}
	}
	
	/* Un cluster est un ensemble de LatLon et un mediod */
	class Cluster {
		
		public LatLon medioid;
		
		TreeSet<LatLon> set;
		Cluster(Cluster c) {
			set=(TreeSet<LatLon>)c.set.clone();
		}
		Cluster() {
			set=new TreeSet<LatLon>();
		}
		
		boolean isJoinable(Cluster o) {
			for(LatLon l : set) {
				// This option is a bit optimistic but rather quick
				if(o.set.contains(l)) {
					return true;
				}
				// for(LatLon k : o.set ) {
					// 	if(l.distanceTo(k)==0.0)
					// 		return true;
					// }
				}
				return false;
			}
			void join(Cluster o) {

				for(LatLon l : o.set) {
					set.add(l);
				}
			
			}
		
			void print() {
				String s;
				s="Cluster=[";
				for(LatLon l : set) {
					s+="( ";
					s+=l.time+" , ";
					s+=l.lat+" , ";
					s+=l.lon+" ) ";
				}
				s+="]";
				System.out.println(s);
			}
		
			void computeMedioid(){
				// The cases with 0, 1 or 2 nodes in the cluster are easy
				if(set.isEmpty())
					return;
				int size=set.size();
				if(size==1||size==2) {
					medioid=set.first();
					return;
				}
		   
				// We compute for each node, the sum of the distances to the other nodes
				// And at the same time we keep the node with the smallest total distance
				double[] distances = new double[size];
				int index=0;
				Iterator it=set.iterator();
				LatLon i=null;
				double bestDistance=Double.MAX_VALUE;
		   
				while(it.hasNext()) {
					i=(LatLon)it.next();
					distances[index]=0;
					for(LatLon j : set) {
						distances[index]+=i.distanceTo(j);
					}
					if(distances[index]<bestDistance) {
						bestDistance=distances[index];
						medioid=i;
					}
					index++;
				}
			}
		}
	
		// Clusters est un ensemble de Cluster */
		class Clusters {
			Set<Cluster> set=new HashSet<Cluster>();
			void computeMedioids() {
				for(Cluster c : set) {
					c.computeMedioid();
				}
			}
		
			BEexample toBEexample() {
				BEexample res=new BEexample();
				for(Cluster c : set) {
					res.set.add(c.medioid);
				}
				return res;
			}
		
		}
	
		/* On traite des ensemble de données géolocalisées */
		TreeSet<LatLon> set=new TreeSet<LatLon>();
	
		/* Importation d'un fichier csv, donner le nom du fichier, le séparateur et les indices dans le fichier du timestamp, de la latitude et de la longitude */
		void readCSV(String fileName, String sep, int indexTime, int indexLat, int indexLon) {
			try {   
				BufferedReader reader = new BufferedReader(new FileReader(fileName));
				// We don't want the first line
				String line = reader.readLine();
				while ((line = reader.readLine()) != null) {
					String[] parts = line.split(sep);
					Integer t=Integer.valueOf(parts[indexTime]);
					double lat=Double.valueOf(parts[indexLat]);
					double lon=Double.valueOf(parts[indexLon]);
					set.add(new LatLon(t,lat,lon));
				}
			} catch (Exception e) {
				System.out.println ("Erreur " + e);
		
			}
		}
		
		/* Export d'un csv au format (time,lat,lon) */
		void writeCSV(String fileName) {
			try {   
				PrintWriter fichier = new PrintWriter(new FileWriter(new File(fileName)));
				fichier.println("name,latitude,longitude");
				// if you want to have tracks in gpsvisualizer, don't use "name" but "time"
				// fichier.println("time,latitude,longitude");
				for(LatLon l : set) {
					fichier.print(l.time);
					fichier.print(",");
					fichier.print(l.lat);
					fichier.print(",");
					fichier.println(l.lon);
				}
				fichier.close();
			} catch (Exception e) {
				System.out.println ("Erreur " + e);
			
			}
		}
	

		/* Filtrage des traces dynamiques pour DJClustering */
		/* On enlève les traces qui bougent au delà de la vitesse epsilon */
		/* Ensuite on enlève les doublons */
		BEexample preprocessDJ(double epsilon){
			BEexample res=new BEexample();
			Iterator it=set.iterator();
			LatLon prev=(LatLon)it.next();
			while(it.hasNext()) {
				LatLon cur=(LatLon)it.next();
				if(cur.speedFrom(prev)<epsilon) {
					// We are going to insert prev if it is different from the last inserted point
					LatLon lastInsert=res.set.floor(prev);
					if(res.set.size()==0 || (lastInsert.distanceTo(prev) > 0) ) {
						res.set.add(prev);
					}
				}
				prev=cur;
			}
			return res;
		}
	
		/* Clustering par la méthode DensityJoignable */
		Clusters DJCluster(double epsilon, int minPts) {
			Clusters clusters=new Clusters();
			BEexample data=this.preprocessDJ(0.005);
			Cluster tmpCluster=new Cluster();
		
			int nb=0;
		
			for(LatLon l : data.set) {
			
				// 1 - We build tmpCluster with all points close enough (ditance < epsilon)
				tmpCluster.set.clear();
				for(LatLon m : data.set) {
					if(l.distanceTo(m) <= epsilon ) {
						tmpCluster.set.add(m);
					}
				}
			
				// 2 - Then we continue with this one only if it has enough points (>minPts)
				if(tmpCluster.set.size() >= minPts) {
					
					// 2.1 Search for mergeable clusters and merge them
					HashSet<Cluster> mergedClusters=new HashSet<Cluster>();
					for(Cluster k : clusters.set) {
						if(tmpCluster.isJoinable(k)) {			
							tmpCluster.join(k);
							mergedClusters.add(k);
						}
					}
					// 2.2 Then remove the merged clusters
					for(Cluster k : mergedClusters) {
						clusters.set.remove(k);
					}
					// 2.3 And finally add the new cluster to the list of clusters
					clusters.set.add(new Cluster(tmpCluster));
				}
		
			}
			clusters.computeMedioids();
			return clusters;
		}
		
		/********************************************************/
		/* Le BE commence ici                                   */
		/********************************************************/
		
		
		/* Une attaque BeginEng */
		public BEexample BEattack(int delay){
			BEexample res=new BEexample();
			// Faire en sorte que res contienne le résultat de l'attaque
			return res;
		}
	

		/* Exemples d'utilisation */
		public static void main (String[] args){
			BEexample be=new BEexample();
			Clusters cl;
			BEexample medioids;
			
			/* 1st Example : on the researcher */
			be.readCSV("ID1.txt",",",2,0,1);
			// ... do what you need to do
			XXX.writeCSV("result-researcher.csv");
			/* With this example, we should be able to find the individual exact home address and almost his place of work*/
			System.out.println("Example 1 done -> result-researcher.csv");
			
			be.set.clear();
			
			/* 2nd Example : on the taxi */
			be.readCSV("ID2.txt"," ",3,0,1);
			// ... do what you need to do
			XXX.writeCSV("result-taxi.csv");
			/* With this example, we should be able to find the individual address */
			System.out.println("Example 2 done -> result-taxi.csv");
			
					
		}
	}