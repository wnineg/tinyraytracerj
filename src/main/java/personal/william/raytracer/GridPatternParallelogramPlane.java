package personal.william.raytracer;

import java.util.Objects;
import java.util.Optional;

public class GridPatternParallelogramPlane implements SceneObject<ParallelogramPlanePositioning> {

    private final double width;
    private final double height;

    private final Pattern pattern;

    public GridPatternParallelogramPlane(double width, double height, Pattern pattern) {
        this.width = width;
        this.height = height;
        this.pattern = pattern;
    }

    @Override
    public Optional<SurfacePoint> cast(ParallelogramPlanePositioning positioning, Vector3d source, UnitVector3d ray) {
        Objects.requireNonNull(positioning, "positioning cannot be null.");
        Objects.requireNonNull(source, "source cannot be null.");
        Objects.requireNonNull(ray, "ray cannot be null.");

        UnitVector3d normal = positioning.getDirectionX().cross(positioning.getDirectionY()).normalize();
        double fluxNorm = normal.dot(ray);
        if (fluxNorm == 0) return Optional.empty();
        if (fluxNorm > 0) {
            normal = normal.negate();
            fluxNorm = -fluxNorm;
        }

        double dist = positioning.getOrigin().minus(source).dot(normal) / fluxNorm;
        Vector3d hit = source.plus(ray.times(dist));

        Vector3d srcToHit = hit.minus(source);
        if (srcToHit.dot(ray) <= 0) return Optional.empty();

        Vector3d poh = hit.minus(positioning.getOrigin());
        double x = poh.dot(positioning.getDirectionX());
        if (x < 0 || x > width) return Optional.empty();
        double y = poh.dot(positioning.getDirectionY());
        if (y < 0 || y > height) return Optional.empty();

        int gridIdx0 = (((int) (x / pattern.boxWidth)) % pattern.materialGrid.length);
        int gridIdx1 = (((int) (y / pattern.boxHeight)) % pattern.materialGrid[0].length);

        return Optional.of(new SurfacePoint(this, hit, normal, pattern.materialGrid[gridIdx0][gridIdx1]));
    }

    public static class Pattern {

        private final double boxWidth;
        private final double boxHeight;

        private final Material[][] materialGrid;

        public Pattern(double boxWidth, double boxHeight, Material[][] materialGrid) {
            this.boxWidth = boxWidth;
            this.boxHeight = boxHeight;
            this.materialGrid = new Material[materialGrid.length][materialGrid[0].length];

            for (int i = 0; i < materialGrid.length; ++i) {
                Material[] row = materialGrid[i];
                System.arraycopy(row, 0, this.materialGrid[i], 0, row.length);
            }
        }
    }
}
