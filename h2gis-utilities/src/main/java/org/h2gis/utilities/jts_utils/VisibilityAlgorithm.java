package org.h2gis.utilities.jts_utils;


import com.vividsolutions.jts.algorithm.*;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.noding.IntersectionAdder;
import com.vividsolutions.jts.noding.MCIndexNoder;
import com.vividsolutions.jts.noding.NodedSegmentString;
import com.vividsolutions.jts.noding.SegmentString;
import com.vividsolutions.jts.util.*;

import java.util.*;

/**
 * This class compute an IsoVist from a coordinate and a set of originalSegments
 * This code is adapted from Byron Knoll javascript library https://github.com/byronknoll/visibility-polygon-js
 */
public class VisibilityAlgorithm {
  private static final double M_PI_DIV2 = Math.PI / 2.;
  private static final double M_2PI = Math.PI * 2.;
  private static final Coordinate NAN_COORDINATE = new Coordinate(Coordinate.NULL_ORDINATE, Coordinate.NULL_ORDINATE);
  // maintain the list of limits sorted by angle
  private double maxDistance;
  private List<SegmentString> originalSegments = new ArrayList<>();
  private double epsilon = 1e-6;
  private int numPoints = 100;

  public VisibilityAlgorithm(double maxDistance) {
    this.maxDistance = maxDistance;
  }

  /**
   * Split originalSegments that intersects. Run this method after calling the last addSegment before calling getIsoVist
   */
  private static List<SegmentString> fixSegments(List<SegmentString> segments) {
    MCIndexNoder mCIndexNoder = new MCIndexNoder();
    RobustLineIntersector robustLineIntersector = new RobustLineIntersector();
    mCIndexNoder.setSegmentIntersector(new IntersectionAdder(robustLineIntersector));
    mCIndexNoder.computeNodes(segments);
    Collection nodedSubstring = mCIndexNoder.getNodedSubstrings();
    ArrayList<SegmentString> ret = new ArrayList<>(nodedSubstring.size());
    for (Object aNodedSubstring : nodedSubstring) {
      ret.add((SegmentString) aNodedSubstring);
    }
    return ret;
  }

  public void fixSegments() {
    originalSegments = fixSegments(originalSegments);
  }

  private static void addSegment(List<SegmentString> segments, Coordinate p0, Coordinate p1) {
    segments.add(new NodedSegmentString(new Coordinate[]{p0, p1}, segments.size() + 1));
  }

  /**
   * @param numPoints Number of points of the bounding circle polygon. Default 100
   */
  public void setNumPoints(int numPoints) {
    this.numPoints = numPoints;
  }

  public void addSegment(Coordinate p0, Coordinate p1) {
    if (p0.distance(p1) < epsilon) {
      return;
    }
    addSegment(originalSegments, p0, p1);
  }

  private static double angle(Coordinate a, Coordinate b) {
    return Math.atan2(b.y - a.y, b.x - a.x);
  }

  public Polygon getIsoVist(Coordinate position, boolean addEnvelope) {
    // Add bounding circle
    List<SegmentString> bounded;
    if(addEnvelope) {
      bounded = new ArrayList<>(originalSegments);
      GeometricShapeFactory geometricShapeFactory = new GeometricShapeFactory();
      geometricShapeFactory.setCentre(position);
      geometricShapeFactory.setWidth(maxDistance * 2);
      geometricShapeFactory.setHeight(maxDistance * 2);
      geometricShapeFactory.setNumPoints(numPoints);
      addPolygon(bounded, geometricShapeFactory.createEllipse());

      // Intersection with bounding circle
      bounded = fixSegments(bounded);
    } else {
      bounded = originalSegments;
    }

    List<Vertex> sorted = new ArrayList<>(bounded.size() * 2);

    for (int idSegment = 0; idSegment < bounded.size(); idSegment++) {
      SegmentString segment = bounded.get(idSegment);
      // Convert segment to angle relative to viewPoint
      for(int j=0; j < 2; ++j) {
        final Coordinate pt = segment.getCoordinate(j);
        sorted.add(new Vertex(idSegment, j, angle(pt, position)));
      }
    }
    Collections.sort(sorted);

    List<Integer> map = new ArrayList<>(bounded.size());
    for(int i=0; i<bounded.size();i++) {
      map.add(-1);
    }
    List<Integer> heap = new ArrayList<>(bounded.size());
    Coordinate start = new Coordinate(position.x + 1, position.y);

    for(int i=0; i < bounded.size(); i++) {
      SegmentString seg = bounded.get(i);
      double a1 = angle(seg.getCoordinate(0), position);
      double a2 = angle(seg.getCoordinate(1), position);
      boolean active = false;
      if(a1 > -Math.PI && a1 <= 0 && a2 <= Math.PI && a2 >= 0 && a2 - a1 > Math.PI) {
        active = true;
      }
      if(a2 > -Math.PI && a2 <= 0 && a1 <= Math.PI && a1 >= 0 && a1 - a2 > Math.PI) {
        active = true;
      }
      if(active) {
        insert(i, heap, position, bounded, start, map);
      }
    }

    List<Coordinate> polygon = new ArrayList<>();

    for(int i=0; i < sorted.size();) {
      boolean extend = false;
      boolean shorten = false;
      int orig = i;
      Coordinate vertex = bounded.get(sorted.get(i).idSegment).getCoordinate(sorted.get(i).vertexIndex);
      int oldSegment = heap.get(0);
      do {
        if(map.get(sorted.get(i).idSegment) != -1) {
          if(sorted.get(i).idSegment == oldSegment) {
            extend = true;
            vertex = bounded.get(sorted.get(i).idSegment).getCoordinate(sorted.get(i).vertexIndex);
          }
          remove(map.get(sorted.get(i).idSegment), heap, position, bounded, vertex, map);
        } else {
          insert(sorted.get(i).idSegment, heap, position, bounded, vertex, map);
          if(heap.get(0) != oldSegment) {
            shorten = true;
          }
        }
        i++;
        if(i == sorted.size()) {
          break;
        }
      } while (sorted.get(i).angle < sorted.get(orig).angle + epsilon);

      //System.out.println(String.format("a:%.1f seg:%d ext:%b short:%b", sorted.get(i).angle, sorted.get(i).idSegment, extend, shorten));
      if(extend) {
        polygon.add(vertex);
        Coordinate cur = intersectLines(bounded.get(heap.get(0)), position, vertex);
        if(cur != null && !cur.equals2D(vertex, epsilon)) {
          polygon.add(vertex);
        }
      } else if(shorten) {
        polygon.add(intersectLines(bounded.get(oldSegment), position, vertex));
        polygon.add(intersectLines(bounded.get(heap.get(0)), position, vertex));
      }
    }
    polygon.add(polygon.get(0));
    GeometryFactory geometryFactory = new GeometryFactory();
    return geometryFactory.createPolygon(polygon.toArray(new Coordinate[polygon.size()]));
  }

  private static Coordinate intersectLines(SegmentString a, Coordinate b1,  Coordinate b2) {
    final Coordinate a1 = a.getCoordinate(0);
    final Coordinate a2 = a.getCoordinate(1);
    double dbx = b2.x - b1.x;
    double dby = b2.y - b1.y;
    double dax = a2.x - a1.x;
    double day = a2.y - a1.y;

    double u_b  = dby * dax - dbx * day;
    if (u_b != 0) {
      double ua = (dbx * (a1.y - b1.y) - dby * (a1.x - b1.x)) / u_b;
      return new Coordinate(a1.x - ua * -dax, a1.y - ua * -day);
    }
    return NAN_COORDINATE;
  }

  private static int getChild(int index) {
    return 2 * index + 1;
  }

  private static int getParent(int index) {
    return (int)Math.floor((index-1)/2.0);
  }

  private double angle2(Coordinate a, Coordinate b, Coordinate c) {
    double a1 = angle(a, b);
    double a2 = angle(b, c);
    double a3 = a1 - a2;
    if(a3 < 0) {
      a3 += M_2PI;
    }
    if(a3 > M_2PI) {
      a3 -= M_2PI;
    }
    return a3;
  }

  private boolean lessThan(int index1,int index2,Coordinate position, List<SegmentString> segments, Coordinate destination) {
    System.out.println(String.format("lessThan %d %d", index1, index2));
    Coordinate inter1 = intersectLines(segments.get(index1), position, destination);
    Coordinate inter2 = intersectLines(segments.get(index2), position, destination);
    if (!inter1.equals2D(inter2, epsilon)) {
      double d1 = inter1.distance(position);
      double d2 = inter2.distance(position);
      System.out.println(String.format("d1 < d2 = %b", d1 < d2));
      return d1 < d2;
    }
    int end1 = 0;
    if (inter1.equals2D(segments.get(index1).getCoordinate(0), epsilon)) {
      end1 = 1;
    }
    int end2 = 0;
    if (inter2.equals2D(segments.get(index2).getCoordinate(0), epsilon)) {
      end2 = 1;
    }
    double a1 = angle2(segments.get(index1).getCoordinate(end1), inter1, position);
    double a2 = angle2(segments.get(index2).getCoordinate(end2), inter2, position);
    if (a1 < Math.PI) {
      return a2 > Math.PI || a2 < a1;
    } else {
      return a1 < a2;
    }
  }

  private void remove(int index, List<Integer> heap, Coordinate position, List<SegmentString> segments, Coordinate destination, List<Integer> map) {
    System.out.println(String.format("remove %d", index));
    map.set(heap.get(index), -1);
    if(index == heap.size() -1) {
      heap.remove(heap.size() - 1);
      return;
    }
    heap.set(index, heap.remove(heap.size() - 1));
    map.set(heap.get(index), index);
    int cur = index;
    if(cur != 0 && lessThan(heap.get(cur), heap.get(getParent(cur)), position, segments, destination)) {
      while(cur > 0) {
        int parent = getParent(cur);
        if(lessThan(heap.get(cur), heap.get(parent), position, segments, destination)) {
          break;
        }
        map.set(heap.get(parent), cur);
        map.set(heap.get(cur), parent);
        int temp = heap.get(cur);
        heap.set(cur, heap.get(parent));
        heap.set(parent, temp);
        cur = parent;
      }
    } else {
      while (true) {
        int left = getChild(cur);
        int right = left + 1;
        if(left < heap.size() && lessThan(heap.get(left), heap.get(cur), position, segments, destination) &&
                (right == heap.size() || lessThan(heap.get(left), heap.get(right), position, segments, destination))) {
          map.set(heap.get(left), cur);
          map.set(heap.get(cur), left);
          int temp = heap.get(left);
          heap.set(left, heap.get(cur));
          heap.set(cur, temp);
          cur = left;
        } else if(right < heap.size() && lessThan(heap.get(right), heap.get(cur), position, segments, destination)) {
          map.set(heap.get(left), cur);
          map.set(heap.get(cur), right);
          int temp = heap.get(right);
          heap.set(right, heap.get(cur));
          heap.set(cur, temp);
          cur = right;
        } else {
          break;
        }
      }
    }
  }

  private void insert(int index, List<Integer> heap, Coordinate position, List<SegmentString> segments, Coordinate destination, List<Integer> map) {
    System.out.println(String.format("insert %d", index));
    Coordinate inter = intersectLines(segments.get(index), position, destination);
    if (NAN_COORDINATE.equals2D(inter, epsilon)) {
      return;
    }
    int cur = heap.size();
    heap.add(index);
    map.set(index, cur);
    while (cur > 0) {
      int parent = getParent(cur);
      if (!lessThan(heap.get(cur), heap.get(parent), position, segments, destination)) {
        break;
      }
      map.set(heap.get(parent), cur);
      map.set(heap.get(cur), parent);
      int temp = heap.get(cur);
      heap.set(cur, heap.get(parent));
      heap.set(parent, temp);
      cur = parent;
    }
  }

  public double getEpsilon() {
    return epsilon;
  }

  public void setEpsilon(double epsilon) {
    this.epsilon = epsilon;
  }

  public void addLineString(LineString lineString) {
    addLineString(originalSegments, lineString);
  }

  public static void addLineString(List<SegmentString> segments, LineString lineString) {
    int nPoint = lineString.getNumPoints();
    for (int idPoint = 0; idPoint < nPoint - 1; idPoint++) {
      addSegment(segments, lineString.getCoordinateN(idPoint), lineString.getCoordinateN(idPoint + 1));
    }
  }

  private static void addPolygon(List<SegmentString> segments, Polygon poly) {
    addLineString(segments, poly.getExteriorRing());
    final int ringCount = poly.getNumInteriorRing();
    // Keep interior ring if the viewpoint is inside the polygon
    for (int nr = 0; nr < ringCount; nr++) {
      addLineString(segments, poly.getInteriorRingN(nr));
    }
  }

  private static void addGeometry(List<SegmentString> segments, GeometryCollection geometry) {
      int geoCount = geometry.getNumGeometries();
      for (int n = 0; n < geoCount; n++) {
        Geometry simpleGeom = geometry.getGeometryN(n);
        if (simpleGeom instanceof LineString) {
          addLineString(segments, (LineString) simpleGeom);
        } else if (simpleGeom instanceof Polygon) {
          addPolygon(segments, (Polygon)simpleGeom);
        }
      }
  }

  public void addGeometry(Geometry geometry) {
    if (geometry instanceof LineString) {
      addLineString(originalSegments, (LineString) geometry);
    } else if (geometry instanceof Polygon) {
      addPolygon(originalSegments, (Polygon) geometry);
    } else if(geometry instanceof GeometryCollection) {
      addGeometry(originalSegments, (GeometryCollection) geometry);
    }
  }

  /**
   * Define segment vertices
   */
  public static final class Vertex implements Comparable<Vertex> {
    final int idSegment;
    final int vertexIndex; // 0 or 1
    final double angle; //vertex angle with position of view point


    public Vertex(int idSegment, int vertexIndex, double angle) {
      this.idSegment = idSegment;
      this.vertexIndex = vertexIndex;
      this.angle = angle;
    }

    @Override
    public int compareTo(Vertex o) {
      int res = Double.compare(angle, o.angle);
      if (res != 0) {
        return res;
      }
      res = Integer.compare(idSegment, o.idSegment);
      if (res != 0) {
        return res;
      }
      return Integer.compare(vertexIndex, o.vertexIndex);
    }
  }
}
