/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.polsl.kmeans;

import java.util.regex.Pattern;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.mllib.clustering.KMeans;
import org.apache.spark.mllib.clustering.KMeansModel;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.linalg.Vectors;

/**
 * Example using MLLib KMeans from Java.
 */
public final class ApacheSparkKMeansMLTest{

  private static class ParsePoint implements Function<String, Vector> {
	private static final long serialVersionUID = 4288126825728569854L;
	private static final Pattern SPLIT_MARK = Pattern.compile(",");

    @Override
    public Vector call(String line) {
      String[] tok = SPLIT_MARK.split(line);
      double[] point = new double[tok.length];
      for (int i = 0; i < tok.length; ++i) {
        point[i] = Double.parseDouble(tok[i]);
      }
      return Vectors.dense(point);
    }
  }

  public static void main(String[] args) {
    if (args.length < 3) {
      System.err.println(
        "Usage: JavaKMeans <input_file> <k> <max_iterations> <master> [<runs>]");
      System.exit(1);
    }
    String inputFile = args[0];
    int k = Integer.parseInt(args[1]);
    int iterations = Integer.parseInt(args[2]);
    String master = args[3];
    int runs = 1;

    if (args.length >= 5) {
      runs = Integer.parseInt(args[4]);
    }
    SparkConf sparkConf = new SparkConf().setAppName("ApacheSparkKMeansML").setMaster(master);
    JavaSparkContext sc = new JavaSparkContext(sparkConf);
    
    long start = System.currentTimeMillis();
    
    JavaRDD<String> lines = sc.textFile(inputFile);

    JavaRDD<Vector> points = lines.map(new ParsePoint());

    KMeansModel model = KMeans.train(points.rdd(), k, iterations, runs, KMeans.K_MEANS_PARALLEL());

    System.out.println("Cluster centers:");
    for (Vector center : model.clusterCenters()) {
      System.out.println(" " + center);
    }
    double cost = model.computeCost(points.rdd());
    System.out.println("Cost: " + cost);

    System.out.println(String.format("ApacheSparkKMeansMLTest executed in: %s[ms]", (System.currentTimeMillis() - start)));
    
    sc.stop();
    sc.close();
  }
}
