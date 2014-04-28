package eu.fbk.phd;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class featureSelection {
	static LinkedList<row> trainingF = new LinkedList<row>();
	static Hashtable<Integer, Integer> featues = new Hashtable<Integer, Integer>();

	public static void main(String args[]) throws IOException {
		parse(args);
	}

	static void parse(String args[]) throws IOException {
		check(args);
		String fileName = args[0];
		String metric = args[1];
		String outputFile = args[2];
		readFile(fileName);
		if (args.length > 3) {
			String active = args[3];

			deactiveFeatures(active);
		}
		adjustMatrixes();
		if (metric.equalsIgnoreCase("avg")) {
			Hashtable<Integer, Double> avg = avg();
			// System.out.println("**AVG**");
			// System.out.println(avg);
			Map<Integer, Double> sortedMapAsc = sortByComparator(avg, false);
			outputFile(outputFile, sortedMapAsc);
		} else if (metric.equalsIgnoreCase("pmi")) {
			// System.out.println("**PMI**");
			Hashtable<Integer, Double> pmi = pmi();
			// System.out.println(pmi);
			Map<Integer, Double> sortedMapAsc = sortByComparator(pmi, false);
			outputFile(outputFile, sortedMapAsc);
		} else {
			// System.out.println("**chi2**");
			Hashtable<Integer, Double> chi2 = chisquare();
			// System.out.println(chi2);
			Map<Integer, Double> sortedMapAsc = sortByComparator(chi2, false);
			outputFile(outputFile, sortedMapAsc);
		}

	}

	private static void adjustMatrixes() {
		//TODO all the other features should be zeros
		for(row r :trainingF){
			
			for(Entry<Integer, Integer> f: featues.entrySet()){
				if(!r.values.containsKey(f.getKey())){
					r.values.put(f.getKey(), 0);
				}
				
			}
		}
		
	}

	private static void deactiveFeatures(String active) {
		active = "," + active + ",";
		for (row r : trainingF) {
			Iterator<Integer> rit = r.values.keySet().iterator();
			while (rit.hasNext()) {
				Integer rtmp = rit.next();
				String tmp = "," + rtmp + ",";
				if (!active.contains(tmp)) {
					rit.remove();
				}
			}
		}

		Iterator<Integer> fit = featues.keySet().iterator();
		while (fit.hasNext()) {
			Integer ftmp = fit.next();
			String tmp = "," + ftmp + ",";
			if (!active.contains(tmp)) {
				fit.remove();
			}
		}

	}

	private static void check(String[] args) {
		if (args.length < 3) {
			System.err.println("There are missed args!\n" + args);
			usage();
			System.exit(0);
		}
		String fn = args[0];
		File f = new File(fn);
		if (!f.isFile() || !f.exists()) {
			System.err.println("Input training file doesn't exist."
					+ f.getPath());
			usage();
			System.exit(0);
		}

		String feature = args[1];
		if (!feature.equals("chi") && !feature.equals("pmi")
				&& !feature.equals("avg")) {
			System.err
					.println("Please choose a correct metric to run between: [chi|avg|pmi]");
			usage();
			System.exit(0);
		}

		String fn1 = args[2];
		File f1 = new File(fn1);
		if (fn1 == null) {
			System.err.println("Output file doesn't exist." + f1.getPath());
			usage();
			System.exit(0);
		}

	}

	private static void usage() {
		System.out
				.println(">java eu.fbk.phd.featureSelection <arg1> <arg2> <arg3> [<arg4>]");
		System.out.println("<arg1> Input file name.");
		System.out
				.println("<arg2> Choose the feature selection metric[chi|avg|pmi].");
		System.out.println("<arg3> Output file name.");
		System.out
				.println("<arg4> Select the active feature index(s): ex.(0,2,3), otherwise all the features will be considered.");
	}

	public static Hashtable<Integer, Double> chisquare() {
		Hashtable<Integer, Double> selectedFeatures = new Hashtable<Integer, Double>();
		Integer feature;
		String category;

		int N1dot, N0dot, N00, N01, N10, N11;
		double chisquareScore;
		Double previousScore;
		for (Entry<Integer, Integer> entry1 : featues.entrySet()) {
			feature = entry1.getKey();
			int count = entry1.getValue();
			N1dot = count;
			N0dot = trainingF.size() - N1dot;

			for (row entry2 : trainingF) {
				category = entry2.id;
				N11 = countLabelWithFeatureX(category, feature);
				N01 = countLabelWithNotFeatureX(category, feature);
				N00 = N0dot - N01;
				N10 = N1dot - N11;

				chisquareScore = trainingF.size()
						* Math.pow(N11 * N00 - N10 * N01, 2)
						/ ((N11 + N01) * (N11 + N10) * (N10 + N00) * (N01 + N00));

				previousScore = selectedFeatures.get(feature);
				
				double in=0.0;
				if(previousScore != null){
					chisquareScore+=previousScore;
				}
				if (!Double.isNaN(chisquareScore)){
					in+=chisquareScore;
				}
				selectedFeatures.put(feature, in);
				/*if (previousScore == null || chisquareScore > previousScore) {
					if (Double.isNaN(chisquareScore))
						chisquareScore = 0.0;
					selectedFeatures.put(feature, chisquareScore);
				}*/

			}
		}

		return selectedFeatures;
	}

	private static Map<Integer, Double> sortByComparator(
			Hashtable<Integer, Double> in, final boolean order) {

		List<Entry<Integer, Double>> list = new LinkedList<Entry<Integer, Double>>(
				in.entrySet());

		Collections.sort(list, new Comparator<Entry<Integer, Double>>() {
			public int compare(Entry<Integer, Double> o1,
					Entry<Integer, Double> o2) {
				if (order) {
					return o1.getValue().compareTo(o2.getValue());
				} else {
					return o2.getValue().compareTo(o1.getValue());

				}
			}
		});

		Map<Integer, Double> sortedMap = new LinkedHashMap<Integer, Double>();
		for (Entry<Integer, Double> entry : list) {
			sortedMap.put(entry.getKey(), entry.getValue());
		}

		return sortedMap;
	}

	private static Hashtable<Integer, Double> avg() {
		Hashtable<Integer, Double> results = new Hashtable<Integer, Double>();
		for (Entry<Integer, Integer> ff : featues.entrySet()) {

			double avg = (float) ff.getValue() / trainingF.size();
			results.put(ff.getKey(), avg);
		}
		return results;
	}

	private static Hashtable<Integer, Double> pmi() {
		Hashtable<Integer, Double> results = new Hashtable<Integer, Double>();
		Hashtable<String, String> resultsHH = new Hashtable<String, String>();
		for (row r : trainingF) {
			if (!resultsHH.containsKey(r.id)) {

				double py = (float) countLabel(r.id) / trainingF.size();
				for (Entry<Integer, Integer> ff : featues.entrySet()) {

					double px = (float) ff.getValue() / trainingF.size();
					double pxy = (float) countLabelWithFeatureX(r.id,
							ff.getKey())
							/ trainingF.size();

					double mi = Math.log(((float) pxy / (px * py)));
					double score = ((float)mi/Math.log(2));
					if(!Double.isInfinite(mi)){
					if (results.containsKey(ff.getKey())) {
						score += results.get(ff.getKey());
						results.put(ff.getKey(), score);
					} else {
						results.put(ff.getKey(), score);
					}
					resultsHH.put(r.id, "");
					}
				}

			}
		}
		return results;
	}

	private static int countLabelWithNotFeatureX(String id, Integer featurekey) {
		int sum = 0;
		for (row r : trainingF) {
			if (r.id.equals(id) &&r.values.containsKey(featurekey)&& r.values.get(featurekey) == 0)
				sum += 1;
		}
		return sum;
	}

	private static int countLabelWithFeatureX(String id, Integer featurekey) {
		int sum = 0;
		for (row r : trainingF) {
			if (r.id.equals(id) && r.values.containsKey(featurekey)&&r.values.get(featurekey) == 1)
				sum += 1;
		}
		return sum;
	}

	private static int countLabel(String id) {
		int sum = 0;
		for (row r : trainingF) {
			if (r.id.equals(id))
				sum += 1;
		}
		return sum;
	}


	public static void outputFile(String FileName,
			Map<Integer, Double> sortedMapAsc) {
		try {

			FileOutputStream OutputFile = new FileOutputStream(FileName);
			OutputStreamWriter Output = new OutputStreamWriter(OutputFile,
					"utf-8");

			System.out.println("File is writing...");

			for (Entry<Integer, Double> val : sortedMapAsc.entrySet()) {

				double chisquareScore = val.getValue();
				if (Double.isNaN(chisquareScore))
					chisquareScore = 0.0;

				Output.write(val.getKey() + "\t" + chisquareScore);
				Output.write("\n");
				Output.flush();
			}

			Output.close();

			System.out.println("Output file is written sucessfully!");
		} catch (Exception e) {
		}

	}

	static void readFile(String fileName) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		String line = "";
		while ((line = br.readLine()) != null) {
			String[] cols = line.split(" ");
			LinkedHashMap<Integer, Integer> values = new LinkedHashMap<Integer, Integer>();
			for (int i = 1; i < cols.length; i++) {
				String[] cc2 = cols[i].split(":");
				// libsvm format test3
				Integer c = 1;
				int a1 = Integer.parseInt(cc2[0]);
				int a2 = Integer.parseInt(cc2[1]);
				if (a2 == 1) {
					if (featues.containsKey(a1)) {
						c = featues.get(a1) + 1;
					}
					featues.put(a1, c);
				} else {
					if (!featues.containsKey(a1))
						featues.put(a1, 0);
				}

				values.put(a1, a2);

				/*
				 * MY test File Integer c =1; if(Integer.parseInt(cols[i])==1){
				 * if(featues.containsKey(i)){ c = featues.get(i)+1; }
				 * featues.put(i, c); }else{ if(!featues.containsKey(i))
				 * featues.put(i, 0); }
				 * 
				 * 
				 * values.put(i,Integer.parseInt(cols[i]));
				 */

			}
			row e = new row();
			e.id = cols[0];
			e.values = values;
			trainingF.addLast(e);

		}
		br.close();
	}

}

class group {
	String label;
	Integer featureId;
}

class row {

	String id;
	LinkedHashMap<Integer, Integer> values;
}
