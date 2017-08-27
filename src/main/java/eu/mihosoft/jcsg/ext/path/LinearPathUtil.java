/*
 * Copyright 2017 Michael Hoffer <info@michaelhoffer.de>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * If you use this software for scientific research then please cite the following publication(s):
 *
 * M. Hoffer, C. Poliwoda, & G. Wittum. (2013). Visual reflection library:
 * a framework for declarative GUI programming on the Java platform.
 * Computing and Visualization in Science, 2013, 16(4),
 * 181–192. http://doi.org/10.1007/s00791-014-0230-y
 */
package eu.mihosoft.jcsg.ext.path;

import eu.mihosoft.vvecmath.Vector3d;
import org.poly2tri.triangulation.util.Tuple2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Tools for linear paths.
 *
 * @author Michael Hoffer <info@michaelhoffer.de>
 */
public final class LinearPathUtil {

    private LinearPathUtil() {
        throw new AssertionError("Don't instantiate me!");
    }

    /**
     * Extends the specified linear closed path by the given amount.
     *
     * @param path path to extend
     * @param amount amount
     * @return extended linear path (list of points)
     */
    public static List<Vector3d> extend(List<Vector3d> path, double amount) {
        List<Vector3d> result = new ArrayList<>(path.size());

        // 1. compute edge normals
        List<Vector3d> edgeNormals = new ArrayList<>(path.size());

        for (int i = 1; i < path.size(); i++) {

            Vector3d segment = path.get(i).minus(path.get(i - 1));
            edgeNormals.add(Vector3d.xy(-segment.y(), segment.x()).normalized());
        }

        Vector3d segment = path.get(0).minus(path.get(path.size() - 1));
        edgeNormals.add(Vector3d.xy(-segment.y(), segment.x()).normalized());

        // 2. compute vertex normals (average of adjacent edge normals)
        List<Vector3d> vertexNormals = new ArrayList<>(path.size());

        Vector3d n0 = edgeNormals.get(edgeNormals.size() - 1).
                lerp(edgeNormals.get(0), 0.5).
                normalized();
        vertexNormals.add(n0);

        for (int i = 1; i < edgeNormals.size(); i++) {
            Vector3d n = edgeNormals.get(i).lerp(edgeNormals.get(i - 1), 0.5).
                    normalized();
            vertexNormals.add(n);
        }

        // 3. extend path along vertex normals
        for (int i = 0; i < path.size(); i++) {
            Vector3d newPoint = path.get(i).plus(vertexNormals.get(i).times(amount));
            if (result.size()>3) {
                for (int j = 0; j < result.size()-2; j++) {
                    Tuple2<Boolean, Vector3d> test = linesIntersect(result.get(j), result.get(j + 1), result.get(result.size() - 1), newPoint);
                    if (test.a) {
                        //Intersection is found. Removing collision points
                        for (int k = j+1; k < result.size(); k++) {
                            result.remove(k);
                            k--;
                        }
                        if (test.b != null) {
                            //If found intersection point, adding it as midpoint
                            result.add(test.b);
                        }
                    }
                }
            }
            result.add(newPoint);
        }
        return result;
    }

    // Determines if the lines AB and CD intersect.
    private static Tuple2<Boolean, Vector3d> linesIntersect(Vector3d A, Vector3d B, Vector3d C, Vector3d D)
    {
        Vector3d CmP = Vector3d.xy(C.getX() - A.getX(), C.getY() - A.getY());
        Vector3d r = Vector3d.xy(B.getX() - A.getX(), B.getY() - A.getY());
        Vector3d s = Vector3d.xy(D.getX() - C.getX(), D.getY() - C.getY());

        double CmPxr = CmP.getX() * r.getY() - CmP.getY() * r.getX();
        double CmPxs = CmP.getX() * s.getY() - CmP.getY() * s.getX();
        double rxs = r.getX() * s.getY() - r.getY() * s.getX();

        if (CmPxr == 0.0)
        {
            // Lines are collinear, and so intersect if they have any overlap
            return new Tuple2<>(((C.getX() - A.getX() < 0.0) != (C.getX() - B.getX() < 0.0))
                    || ((C.getY() - A.getY() < 0.0) != (C.getY() - B.getY() < 0.0)), null);
        }

        if (rxs == 0.0)
            return new Tuple2<>(Boolean.FALSE, null); // Lines are parallel.

        double rxsr = 1.0 / rxs;
        double t = CmPxs * rxsr;
        double u = CmPxr * rxsr;

        if ((t >= 0.0) && (t <= 1.0) && (u >= 0.0) && (u <= 1.0)) {
            return new Tuple2<>(Boolean.TRUE, getIntersection(A, B, C, D));
        } else {
            return new Tuple2<>(Boolean.FALSE, null);
        }
    }

    //Get intersection of 2 lines AB and CD, not segments. Need to detect segments interception first
    private static Vector3d getIntersection(Vector3d A, Vector3d B, Vector3d C, Vector3d D)
    {
        double xo = A.getX(), yo = A.getY();
        double p = B.getX() - A.getX(), q = B.getY() - A.getY();

        double x1 = C.getX(), y1 = C.getY();
        double p1 = D.getX() - C.getX(), q1 = D.getY() - C.getY();

        double x = (xo * q * p1 - x1 * q1 * p - yo * p * p1 + y1 * p * p1) /
                (q * p1 - q1 * p);
        double y = (yo * p * q1 - y1 * p1 * q - xo * q * q1 + x1 * q * q1) /
                (p * q1 - p1 * q);

        return Vector3d.xy(x, y);
    }

    /**
     * Make not closed path thicker, adding reverse side of points.
     *
     * @param path not closed path
     * @param width desired width of path
     * @return 2d path width desired width
     */
    public static List<Vector3d> thickPath(List<Vector3d> path, double width) {
        List<Vector3d> result = new ArrayList<>(path.size());

        // 1. compute edge normals
        List<Vector3d> edgeNormals = new ArrayList<>(path.size()-1);

        for (int i = 0; i < path.size()-1; i++) {
            Vector3d segment = path.get(i+1).minus(path.get(i));
            edgeNormals.add(Vector3d.xy(-segment.y(), segment.x()).normalized());
        }

        // 2. compute vertex normals (average of adjacent edge normals)
        List<Vector3d> vertexNormals = new ArrayList<>(path.size());

        //First vertex normal is equal to edge normal
        vertexNormals.add(edgeNormals.get(0).normalized());

        for (int i = 1; i < edgeNormals.size(); i++) {
            Vector3d n = edgeNormals.get(i).lerp(edgeNormals.get(i - 1), 0.5).
                    normalized();
            vertexNormals.add(n);
        }
        //Last vertex normal is equal to edge normal
        vertexNormals.add(edgeNormals.get(edgeNormals.size()-1).normalized());

        //reverse side of path
        List<Vector3d> reverseSide = new ArrayList<>(path.size());
        // 3. extend path along vertex normals
        for (int i = 0; i < path.size()-1; i++) {
            result.add(path.get(i).plus(vertexNormals.get(i).times(width/2)));
            reverseSide.add(path.get(i).minus(vertexNormals.get(i).times(width/2)));
        }
        Collections.reverse(reverseSide); //has to be reversed
        result.addAll(reverseSide);

        return result;
    }
}
