package personal.william.raytracer;

public class SurfacePoint {

    private final SceneObject object;

    private final Vector3d point;
    private final Vector3d normal;
    private final Material material;

    public SurfacePoint(SceneObject object, Vector3d point, Vector3d normal, Material material) {
        this.object = object;
        this.point = point;
        this.normal = normal;
        this.material = material;
    }

    @Override
    public String toString() {
        return "SurfacePoint{" +
                "point=" + point +
                ", normal=" + normal +
                ", material=" + material +
                '}';
    }

    public SceneObject getObject() {
        return object;
    }

    public Vector3d getPoint() {
        return point;
    }

    public Vector3d getNormal() {
        return normal;
    }

    public Material getMaterial() {
        return material;
    }
}
