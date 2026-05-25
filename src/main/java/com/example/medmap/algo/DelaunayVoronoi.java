package com.example.medmap.algo;

import com.example.medmap.model.Point;
import java.util.ArrayList;
import java.util.List;

public class DelaunayVoronoi {
    private QuadEdge startingEdge = null;
    private final List<QuadEdge> quadEdges = new ArrayList<>();
    private final BoundingBox bbox = new BoundingBox();

    // Classe interne
    private static class BoundingBox {
        private double minx = Double.MAX_VALUE;
        private double miny = Double.MAX_VALUE;
        private double maxx = -Double.MAX_VALUE;
        private double maxy = -Double.MAX_VALUE;

        // On garde les mêmes instances du début à la fin
        private final Point a = new Point(0, 0, "BBox A");
        private final Point b = new Point(0, 0, "BBox B");
        private final Point c = new Point(0, 0, "BBox C");
        private final Point d = new Point(0, 0, "BBox D");

        public void setBounds(double minx, double miny, double maxx, double maxy) {
            this.minx = minx; this.miny = miny;
            this.maxx = maxx; this.maxy = maxy;

            double centerx = (minx + maxx) / 2.0;
            double centery = (miny + maxy) / 2.0;

            a.setX((minx - centerx - 1.0) * 10.0 + centerx);   a.setY((miny - centery - 1.0) * 10.0 + centery);
            b.setX((maxx - centerx + 1.0) * 10.0 + centerx);   b.setY((miny - centery - 1.0) * 10.0 + centery);
            c.setX((maxx - centerx + 1.0) * 10.0 + centerx);   c.setY((maxy - centery + 1.0) * 10.0 + centery);
            d.setX((minx - centerx - 1.0) * 10.0 + centerx);   d.setY((maxy - centery + 1.0) * 10.0 + centery);
        }

        public boolean containsPoint(Point p) {
            return p == a || p == b || p == c || p == d;
        }

        public Point getA() { return a; }
        public Point getB() { return b; }
        public Point getC() { return c; }
        public Point getD() { return d; }
    }

    public DelaunayVoronoi() {
        QuadEdge ab = QuadEdge.makeEdge(bbox.getA(), bbox.getB());
        QuadEdge bc = QuadEdge.makeEdge(bbox.getB(), bbox.getC());
        QuadEdge cd = QuadEdge.makeEdge(bbox.getC(), bbox.getD());
        QuadEdge da = QuadEdge.makeEdge(bbox.getD(), bbox.getA());
        QuadEdge.splice(ab.sym(), bc);
        QuadEdge.splice(bc.sym(), cd);
        QuadEdge.splice(cd.sym(), da);
        QuadEdge.splice(da.sym(), ab);

        this.startingEdge = ab;
    }

    public void setBoundingBox(double minx, double miny, double maxx, double maxy) {
        bbox.setBounds(minx, miny, maxx, maxy);
    }

    private void updateBoundingBox(Point p) {
        double minx = Math.min(bbox.minx, p.getX());
        double maxx = Math.max(bbox.maxx, p.getX());
        double miny = Math.min(bbox.miny, p.getY());
        double maxy = Math.max(bbox.maxy, p.getY());
        setBoundingBox(minx, miny, maxx, maxy);
    }

    private QuadEdge locate(Point p) {
        if (p.getX() < bbox.minx || p.getX() > bbox.maxx || p.getY() < bbox.miny || p.getY() > bbox.maxy) {
            updateBoundingBox(p);
        }

        QuadEdge e = startingEdge;
        while (true) {
            if (p.getX() == e.orig().getX() && p.getY() == e.orig().getY()) return e;
            if (p.getX() == e.dest().getX() && p.getY() == e.dest().getY()) return e;

            if (QuadEdge.isAtRightOf(e, p))
                e = e.sym();
            else if (!QuadEdge.isAtRightOf(e.onext(), p))
                e = e.onext();
            else if (!QuadEdge.isAtRightOf(e.dprev(), p))
                e = e.dprev();
            else
                return e;
        }
    }

    public void insertPoint(Point p) {
        QuadEdge e = locate(p);

        if (p.getX() == e.orig().getX() && p.getY() == e.orig().getY()) return;
        if (p.getX() == e.dest().getX() && p.getY() == e.dest().getY()) return;

        if (QuadEdge.isOnLine(e, p)) {
            e = e.oprev();
            this.quadEdges.remove(e.onext().sym());
            this.quadEdges.remove(e.onext());
            QuadEdge.deleteEdge(e.onext());
        }

        QuadEdge base = QuadEdge.makeEdge(e.orig(), p);
        this.quadEdges.add(base);

        QuadEdge.splice(base, e);
        this.startingEdge = base;
        do {
            base = QuadEdge.connect(e, base.sym());
            this.quadEdges.add(base);
            e = base.oprev();
        } while (e.lnext() != startingEdge);

        do {
            QuadEdge t = e.oprev();

            if (QuadEdge.isAtRightOf(e, t.dest()) && QuadEdge.inCircle(e.orig(), t.dest(), e.dest(), p)) {
                QuadEdge.swapEdge(e);
                e = e.oprev();
            } else if (e.onext() == startingEdge)
                return;
            else
                e = e.onext().lprev();
        } while (true);
    }

    private void markBoundingBoxEdges() {
        for (QuadEdge q : this.quadEdges) {
            q.setMark(false);
            q.sym().setMark(false);
            if (bbox.containsPoint(q.orig())) q.setMark(true);
            if (bbox.containsPoint(q.dest())) q.sym().setMark(true);
        }
    }

    public List<Point[]> computeEdges() {
        List<Point[]> edges = new ArrayList<>();
        for (QuadEdge q : this.quadEdges) {
            if (bbox.containsPoint(q.orig()) || bbox.containsPoint(q.dest())) continue;
            edges.add(new Point[]{q.orig(), q.dest()});
        }
        return edges;
    }

    public List<Point[]> computeTriangles() {
        List<Point[]> triangles = new ArrayList<>();
        markBoundingBoxEdges();

        for (QuadEdge qe : quadEdges) {
            checkAndAddTriangle(triangles, qe);
            checkAndAddTriangle(triangles, qe.sym());
            qe.setMark(true);
            qe.sym().setMark(true);
        }
        return triangles;
    }

    private void checkAndAddTriangle(List<Point[]> triangles, QuadEdge start) {
        QuadEdge q2 = start.lnext();
        QuadEdge q3 = q2.lnext();
        if (!start.isMarked() && !q2.isMarked() && !q3.isMarked()) {
            triangles.add(new Point[]{start.orig(), q2.orig(), q3.orig()});
        }
    }

    public List<Point[]> computeVoronoi() {
        List<Point[]> voronoi = new ArrayList<>();
        markBoundingBoxEdges();

        for (QuadEdge qe : quadEdges) {
            for (int b = 0; b <= 1; b++) {
                QuadEdge qstart = (b == 0) ? qe : qe.sym();
                if (qstart.isMarked()) continue;

                List<Point> poly = new ArrayList<>();
                QuadEdge qregion = qstart;
                while (true) {
                    qregion.setMark(true);
                    if (qregion.rot().orig() == null) {
                        qregion.rot().setOrig(calculateCircumCenter(qregion));
                    }
                    poly.add(qregion.rot().orig());
                    qregion = qregion.onext();
                    if (qregion == qstart) break;
                }
                voronoi.add(poly.toArray(new Point[0]));
            }
        }
        return voronoi;
    }

    private Point calculateCircumCenter(QuadEdge q) {
        Point p0 = q.orig();
        Point p1 = q.lnext().orig();
        Point p2 = q.lnext().lnext().orig();

        double ex = p1.getX() - p0.getX(), ey = p1.getY() - p0.getY();
        double nx = p2.getY() - p1.getY(), ny = p1.getX() - p2.getX();
        double dx = (p0.getX() - p2.getX()) * 0.5, dy = (p0.getY() - p2.getY()) * 0.5;
        double s = (ex * dx + ey * dy) / (ex * nx + ey * ny);

        return new Point(
                (p1.getX() + p2.getX()) * 0.5 + s * nx,
                (p1.getY() + p2.getY()) * 0.5 + s * ny,
                "CircumCenter"
        );
    }
}