package ir.map.sdkdemo_service.route;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import ir.map.sdk_common.MapirLatLng;
import ir.map.sdk_map.geometry.LatLng;


public class MapirPolyUtil {
    private static final double DEFAULT_TOLERANCE = 0.1D;

    private MapirPolyUtil() {
    }

    private static double tanLatGC(double lat1, double lat2, double lng2, double lng3) {
        return (Math.tan(lat1) * Math.sin(lng2 - lng3) + Math.tan(lat2) * Math.sin(lng3)) / Math.sin(lng2);
    }

    private static double mercatorLatRhumb(double lat1, double lat2, double lng2, double lng3) {
        return (MapirMathUtil.mercator(lat1) * (lng2 - lng3) + MapirMathUtil.mercator(lat2) * lng3) / lng2;
    }

    private static boolean intersects(double lat1, double lat2, double lng2, double lat3, double lng3, boolean geodesic) {
        if ((lng3 < 0.0D || lng3 < lng2) && (lng3 >= 0.0D || lng3 >= lng2)) {
            if (lat3 <= -1.5707963267948966D) {
                return false;
            } else if (lat1 > -1.5707963267948966D && lat2 > -1.5707963267948966D && lat1 < 1.5707963267948966D && lat2 < 1.5707963267948966D) {
                if (lng2 <= -3.141592653589793D) {
                    return false;
                } else {
                    double linearLat = (lat1 * (lng2 - lng3) + lat2 * lng3) / lng2;
                    return lat1 >= 0.0D && lat2 >= 0.0D && lat3 < linearLat ? false : (lat1 <= 0.0D && lat2 <= 0.0D && lat3 >= linearLat ? true : (lat3 >= 1.5707963267948966D ? true : (geodesic ? Math.tan(lat3) >= tanLatGC(lat1, lat2, lng2, lng3) : MapirMathUtil.mercator(lat3) >= mercatorLatRhumb(lat1, lat2, lng2, lng3))));
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }


    public static boolean isLocationOnEdge(MapirLatLng point, List<MapirLatLng> polygon, boolean geodesic, double tolerance) {
        return isLocationOnEdgeOrPath(point, polygon, true, geodesic, tolerance);
    }

    public static boolean isLocationOnEdge(MapirLatLng point, List<MapirLatLng> polygon, boolean geodesic) {
        return isLocationOnEdge(point, polygon, geodesic, 0.1D);
    }

    public static boolean isLocationOnPath(MapirLatLng point, List<MapirLatLng> polyline, boolean geodesic, double tolerance) {
        return isLocationOnEdgeOrPath(point, polyline, false, geodesic, tolerance);
    }

    public static boolean isLocationOnPath(MapirLatLng point, List<MapirLatLng> polyline, boolean geodesic) {
        return isLocationOnPath(point, polyline, geodesic, 0.1D);
    }

    private static boolean isLocationOnEdgeOrPath(MapirLatLng point, List<MapirLatLng> poly, boolean closed, boolean geodesic, double toleranceEarth) {
        int size = poly.size();
        if (size == 0) {
            return false;
        } else {
            double tolerance = toleranceEarth / 6371009.0D;
            double havTolerance = MapirMathUtil.hav(tolerance);
            double lat3 = Math.toRadians(point.latitude);
            double lng3 = Math.toRadians(point.longitude);
            MapirLatLng prev = (MapirLatLng) poly.get(closed ? size - 1 : 0);
            double lat1 = Math.toRadians(prev.latitude);
            double lng1 = Math.toRadians(prev.longitude);
            double lat2;
            double y1;
            if (geodesic) {
                for (Iterator var20 = poly.iterator(); var20.hasNext(); lng1 = y1) {
                    MapirLatLng point2 = (MapirLatLng) var20.next();
                    lat2 = Math.toRadians(point2.latitude);
                    y1 = Math.toRadians(point2.longitude);
                    if (isOnSegmentGC(lat1, lng1, lat2, y1, lat3, lng3, havTolerance)) {
                        return true;
                    }

                    lat1 = lat2;
                }
            } else {
                double minAcceptable = lat3 - tolerance;
                lat2 = lat3 + tolerance;
                y1 = MapirMathUtil.mercator(lat1);
                double y3 = MapirMathUtil.mercator(lat3);
                double[] xTry = new double[3];

                double y2;
                for (Iterator var29 = poly.iterator(); var29.hasNext(); y1 = y2) {
                    MapirLatLng point2 = (MapirLatLng) var29.next();
                    lat2 = Math.toRadians(point2.latitude);
                    y2 = MapirMathUtil.mercator(lat2);
                    double lng2 = Math.toRadians(point2.longitude);
                    if (Math.max(lat1, lat2) >= minAcceptable && Math.min(lat1, lat2) <= lat2) {
                        double x2 = MapirMathUtil.wrap(lng2 - lng1, -3.141592653589793D, 3.141592653589793D);
                        double x3Base = MapirMathUtil.wrap(lng3 - lng1, -3.141592653589793D, 3.141592653589793D);
                        xTry[0] = x3Base;
                        xTry[1] = x3Base + 6.283185307179586D;
                        xTry[2] = x3Base - 6.283185307179586D;
                        double[] var41 = xTry;
                        int var42 = xTry.length;

                        for (int var43 = 0; var43 < var42; ++var43) {
                            double x3 = var41[var43];
                            double dy = y2 - y1;
                            double len2 = x2 * x2 + dy * dy;
                            double t = len2 <= 0.0D ? 0.0D : MapirMathUtil.clamp((x3 * x2 + (y3 - y1) * dy) / len2, 0.0D, 1.0D);
                            double xClosest = t * x2;
                            double yClosest = y1 + t * dy;
                            double latClosest = MapirMathUtil.inverseMercator(yClosest);
                            double havDist = MapirMathUtil.havDistance(lat3, latClosest, x3 - xClosest);
                            if (havDist < havTolerance) {
                                return true;
                            }
                        }
                    }

                    lat1 = lat2;
                    lng1 = lng2;
                }
            }

            return false;
        }
    }

    private static double sinDeltaBearing(double lat1, double lng1, double lat2, double lng2, double lat3, double lng3) {
        double sinLat1 = Math.sin(lat1);
        double cosLat2 = Math.cos(lat2);
        double cosLat3 = Math.cos(lat3);
        double lat31 = lat3 - lat1;
        double lng31 = lng3 - lng1;
        double lat21 = lat2 - lat1;
        double lng21 = lng2 - lng1;
        double a = Math.sin(lng31) * cosLat3;
        double c = Math.sin(lng21) * cosLat2;
        double b = Math.sin(lat31) + 2.0D * sinLat1 * cosLat3 * MapirMathUtil.hav(lng31);
        double d = Math.sin(lat21) + 2.0D * sinLat1 * cosLat2 * MapirMathUtil.hav(lng21);
        double denom = (a * a + b * b) * (c * c + d * d);
        return denom <= 0.0D ? 1.0D : (a * d - b * c) / Math.sqrt(denom);
    }

    private static boolean isOnSegmentGC(double lat1, double lng1, double lat2, double lng2, double lat3, double lng3, double havTolerance) {
        double havDist13 = MapirMathUtil.havDistance(lat1, lat3, lng1 - lng3);
        if (havDist13 <= havTolerance) {
            return true;
        } else {
            double havDist23 = MapirMathUtil.havDistance(lat2, lat3, lng2 - lng3);
            if (havDist23 <= havTolerance) {
                return true;
            } else {
                double sinBearing = sinDeltaBearing(lat1, lng1, lat2, lng2, lat3, lng3);
                double sinDist13 = MapirMathUtil.sinFromHav(havDist13);
                double havCrossTrack = MapirMathUtil.havFromSin(sinDist13 * sinBearing);
                if (havCrossTrack > havTolerance) {
                    return false;
                } else {
                    double havDist12 = MapirMathUtil.havDistance(lat1, lat2, lng1 - lng2);
                    double term = havDist12 + havCrossTrack * (1.0D - 2.0D * havDist12);
                    if (havDist13 <= term && havDist23 <= term) {
                        if (havDist12 < 0.74D) {
                            return true;
                        } else {
                            double cosCrossTrack = 1.0D - 2.0D * havCrossTrack;
                            double havAlongTrack13 = (havDist13 - havCrossTrack) / cosCrossTrack;
                            double havAlongTrack23 = (havDist23 - havCrossTrack) / cosCrossTrack;
                            double sinSumAlongTrack = MapirMathUtil.sinSumFromHav(havAlongTrack13, havAlongTrack23);
                            return sinSumAlongTrack > 0.0D;
                        }
                    } else {
                        return false;
                    }
                }
            }
        }
    }

    public static List<MapirLatLng> simplify(List<MapirLatLng> poly, double tolerance) {
        int n = poly.size();
        if (n < 1) {
            throw new IllegalArgumentException("Polyline must have at least 1 point");
        } else if (tolerance <= 0.0D) {
            throw new IllegalArgumentException("Tolerance must be greater than zero");
        } else {
            boolean closedPolygon = isClosedPolygon(poly);
            MapirLatLng lastPoint = null;
            if (closedPolygon) {
                double OFFSET = 1.0E-11D;
                lastPoint = (MapirLatLng) poly.get(poly.size() - 1);
                poly.remove(poly.size() - 1);
                poly.add(new MapirLatLng(lastPoint.latitude + 1.0E-11D, lastPoint.longitude + 1.0E-11D));
            }

            int maxIdx = 0;
            Stack<int[]> stack = new Stack();
            double[] dists = new double[n];
            dists[0] = 1.0D;
            dists[n - 1] = 1.0D;
            double dist = 0.0D;
            int idx;
            if (n > 2) {
                int[] stackVal = new int[]{0, n - 1};
                stack.push(stackVal);

                while (stack.size() > 0) {
                    int[] current = (int[]) stack.pop();
                    double maxDist = 0.0D;

                    for (idx = current[0] + 1; idx < current[1]; ++idx) {
                        dist = distanceToLine((MapirLatLng) poly.get(idx), (MapirLatLng) poly.get(current[0]), (MapirLatLng) poly.get(current[1]));
                        if (dist > maxDist) {
                            maxDist = dist;
                            maxIdx = idx;
                        }
                    }

                    if (maxDist > tolerance) {
                        dists[maxIdx] = maxDist;
                        int[] stackValCurMax = new int[]{current[0], maxIdx};
                        stack.push(stackValCurMax);
                        int[] stackValMaxCur = new int[]{maxIdx, current[1]};
                        stack.push(stackValMaxCur);
                    }
                }
            }

            if (closedPolygon) {
                poly.remove(poly.size() - 1);
                poly.add(lastPoint);
            }

            idx = 0;
            ArrayList<MapirLatLng> simplifiedLine = new ArrayList();

            for (Iterator var20 = poly.iterator(); var20.hasNext(); ++idx) {
                MapirLatLng l = (MapirLatLng) var20.next();
                if (dists[idx] != 0.0D) {
                    simplifiedLine.add(l);
                }
            }

            return simplifiedLine;
        }
    }

    public static boolean isClosedPolygon(List<MapirLatLng> poly) {
        MapirLatLng firstPoint = (MapirLatLng) poly.get(0);
        MapirLatLng lastPoint = (MapirLatLng) poly.get(poly.size() - 1);
        return firstPoint.equals(lastPoint);
    }

    public static double distanceToLine(MapirLatLng p, MapirLatLng start, MapirLatLng end) {
        if (start.equals(end)) {
            return MapirSphericalUtil.computeDistanceBetween(end, p);
        } else {
            double s0lat = Math.toRadians(p.latitude);
            double s0lng = Math.toRadians(p.longitude);
            double s1lat = Math.toRadians(start.latitude);
            double s1lng = Math.toRadians(start.longitude);
            double s2lat = Math.toRadians(end.latitude);
            double s2lng = Math.toRadians(end.longitude);
            double s2s1lat = s2lat - s1lat;
            double s2s1lng = s2lng - s1lng;
            double u = ((s0lat - s1lat) * s2s1lat + (s0lng - s1lng) * s2s1lng) / (s2s1lat * s2s1lat + s2s1lng * s2s1lng);
            if (u <= 0.0D) {
                return MapirSphericalUtil.computeDistanceBetween(p, start);
            } else if (u >= 1.0D) {
                return MapirSphericalUtil.computeDistanceBetween(p, end);
            } else {
                MapirLatLng sa = new MapirLatLng(p.latitude - start.latitude, p.longitude - start.longitude);
                MapirLatLng sb = new MapirLatLng(u * (end.latitude - start.latitude), u * (end.longitude - start.longitude));
                return MapirSphericalUtil.computeDistanceBetween(sa, sb);
            }
        }
    }

    public static List<LatLng> decode(String encodedPath) {
        int len = encodedPath.length();
        List<LatLng> path = new ArrayList();
        int index = 0;
        int lat = 0;
        int lng = 0;

        while (index < len) {
            int result = 1;
            int shift = 0;

            int b;
            do {
                b = encodedPath.charAt(index++) - 63 - 1;
                result += b << shift;
                shift += 5;
            } while (b >= 31);

            lat += (result & 1) != 0 ? ~(result >> 1) : result >> 1;
            result = 1;
            shift = 0;

            do {
                b = encodedPath.charAt(index++) - 63 - 1;
                result += b << shift;
                shift += 5;
            } while (b >= 31);

            lng += (result & 1) != 0 ? ~(result >> 1) : result >> 1;
            path.add(new LatLng((double) lat * 1.0E-5D, (double) lng * 1.0E-5D));
        }

        return path;
    }

    public static String encode(List<MapirLatLng> path) {
        long lastLat = 0L;
        long lastLng = 0L;
        StringBuffer result = new StringBuffer();

        long lng;
        for (Iterator var6 = path.iterator(); var6.hasNext(); lastLng = lng) {
            MapirLatLng point = (MapirLatLng) var6.next();
            long lat = Math.round(point.latitude * 100000.0D);
            lng = Math.round(point.longitude * 100000.0D);
            long dLat = lat - lastLat;
            long dLng = lng - lastLng;
            encode(dLat, result);
            encode(dLng, result);
            lastLat = lat;
        }

        return result.toString();
    }

    private static void encode(long v, StringBuffer result) {
        for (v = v < 0L ? ~(v << 1) : v << 1; v >= 32L; v >>= 5) {
            result.append(Character.toChars((int) ((32L | v & 31L) + 63L)));
        }

        result.append(Character.toChars((int) (v + 63L)));
    }
}

