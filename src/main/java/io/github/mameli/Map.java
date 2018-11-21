package io.github.mameli;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.log4j.Logger;

/**
 * Created by mameli on 19/02/2017. K means mapper
 */
public class Map extends Mapper<Object, Text, Center, Point> {

  private Logger logger = Logger.getLogger(Map.class);
  private List<Center> centers = new ArrayList<Center>();

  @Override
  protected void setup(Context context) throws IOException, InterruptedException {
    Configuration conf = context.getConfiguration();
    Path centersPath = new Path(conf.get("centersFilePath"));
    SequenceFile.Reader reader = new SequenceFile.Reader(conf,
        SequenceFile.Reader.file(centersPath));
    IntWritable key = new IntWritable();
    Center value = new Center();
    while (reader.next(key, value)) {
      Center c = new Center(value.getListOfCoordinates());
      c.setNumberOfPoints(new IntWritable(0));
      c.setIndex(key);
      centers.add(c);
    }
    reader.close();
    logger.fatal("Centers: " + centers.toString());
  }

  @Override
  public void map(Object key, Text value, Context context)
      throws IOException, InterruptedException {
    String line = value.toString();
    List<DoubleWritable> spaceValues = new ArrayList<DoubleWritable>();
    StringTokenizer tokenizer = new StringTokenizer(line, ";");
    while (tokenizer.hasMoreTokens()) {
      spaceValues.add(new DoubleWritable(Double.parseDouble(tokenizer.nextToken())));
    }
    Point p = new Point(spaceValues);

    Center minDistanceCenter = null;
    Double minDistance = Double.MAX_VALUE;
    Double distanceTemp;
    for (Center c : centers) {
      distanceTemp = Distance.findDistance(c, p);
      if (minDistance > distanceTemp) {
        minDistanceCenter = c;
        minDistance = distanceTemp;
      }
    }
    context.write(minDistanceCenter, p);
  }


}
