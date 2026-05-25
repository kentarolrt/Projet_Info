package com.example.medmap.algo;
import com.example.medmap.model.Point;

public class QuadEdge {
    private QuadEdge onext;
    private QuadEdge rot;
    private Point orig;
    private boolean mark = false;

    // constructeur
    private QuadEdge(QuadEdge onext, QuadEdge rot, Point orig) {
        this.onext = onext;
        this.rot = rot;
        this.orig = orig;
    }

    // Getters / Setters
    public QuadEdge onext() { return onext; }
    public QuadEdge rot() { return rot; }
    public Point orig() { return orig; }

    public boolean isMarked() { return mark; }
    public void setMark(boolean mark) { this.mark = mark; }

    public void setOnext(QuadEdge next) { this.onext = next; }
    public void setRot(QuadEdge rot) { this.rot = rot; }
    public void setOrig(Point p) { this.orig = p; }

    // Navigation
    public QuadEdge sym() {
        return this.rot.rot;
    }

    public Point dest() {
        return sym().orig();
    }

    public QuadEdge rotSym() {
        return this.rot.sym();
    }

    public QuadEdge oprev() {
        return this.rot.onext.rot;
    }

    public QuadEdge dprev() {
        return rotSym().onext.rotSym();
    }

    public QuadEdge lnext() {
        return rotSym().onext.rot;
    }

    public QuadEdge lprev() {
        return this.onext.sym();
    }

    // Fabrique statique
    public static QuadEdge makeEdge(Point orig, Point dest) {
        QuadEdge q0 = new QuadEdge(null, null, orig);
        QuadEdge q1 = new QuadEdge(null, null, null);
        QuadEdge q2 = new QuadEdge(null, null, dest);
        QuadEdge q3 = new QuadEdge(null, null, null);

        q0.onext = q0; q2.onext = q2;
        q1.onext = q3; q3.onext = q1;

        q0.rot = q1; q1.rot = q2;
        q2.rot = q3; q3.rot = q0;

        return q0;
    }

    public static void splice(QuadEdge a, QuadEdge b) {
        QuadEdge alpha = a.onext.rot;
        QuadEdge beta  = b.onext.rot;

        QuadEdge t1 = b.onext;
        QuadEdge t2 = a.onext;
        QuadEdge t3 = beta.onext;
        QuadEdge t4 = alpha.onext;

        a.onext = t1;
        b.onext = t2;
        alpha.onext = t3;
        beta.onext = t4;
    }

    public static QuadEdge connect(QuadEdge e1, QuadEdge e2) {
        QuadEdge q = makeEdge(e1.dest(), e2.orig());
        splice(q, e1.lnext());
        splice(q.sym(), e2);
        return q;
    }

    public static void swapEdge(QuadEdge e) {
        QuadEdge a = e.oprev();
        QuadEdge b = e.sym().oprev();
        splice(e, a);
        splice(e.sym(), b);
        splice(e, a.lnext());
        splice(e.sym(), b.lnext());
        e.setOrig(a.dest());
        e.sym().setOrig(b.dest());
    }

    public static void deleteEdge(QuadEdge q) {
        splice(q, q.oprev());
        splice(q.sym(), q.sym().oprev());
    }

    // Calculs géométriques
    public static boolean isOnLine(QuadEdge e, Point p) {
        double val1 = (p.getX() - e.orig().getX()) * (e.dest().getY() - e.orig().getY());
        double val2 = (p.getY() - e.orig().getY()) * (e.dest().getX() - e.orig().getX());
        return Math.abs(val1 - val2) < 1e-9;
    }

    public static boolean isAtRightOf(QuadEdge q, Point p) {
        return isCounterClockwise(p, q.dest(), q.orig());
    }

    public static boolean isCounterClockwise(Point a, Point b, Point c) {
        return (a.getX() - b.getX()) * (b.getY() - c.getY()) > (a.getY() - b.getY()) * (b.getX() - c.getX());
    }

    public static boolean inCircle(Point a, Point b, Point c, Point d) {
        double a2 = a.getX() * a.getX() + a.getY() * a.getY();
        double b2 = b.getX() * b.getX() + b.getY() * b.getY();
        double c2 = c.getX() * c.getX() + c.getY() * c.getY();
        double d2 = d.getX() * d.getX() + d.getY() * d.getY();

        double det44 = 0;
        det44 += d2  * det33( a.getX(), a.getY(), 1,  b.getX(), b.getY(), 1,  c.getX(), c.getY(), 1 );
        det44 -= d.getX() * det33( a2 , a.getY(), 1,  b2 , b.getY(), 1,  c2 , c.getY(), 1 );
        det44 += d.getY() * det33( a2 , a.getX(), 1,  b2 , b.getX(), 1,  c2 , c.getX(), 1 );
        det44 -= 1   * det33( a2,  a.getX(), a.getY(), b2, b.getX(), b.getY(), c2, c.getX(), c.getY() );

        return det44 < 0;
    }

    private static double det33(double... m) {
        return m[0] * (m[4] * m[8] - m[5] * m[7])
                - m[1] * (m[3] * m[8] - m[5] * m[6])
                + m[2] * (m[3] * m[7] - m[4] * m[6]);
    }
}