package personal.william.raytracer;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.atomic.AtomicInteger;

public class Scene3d implements Vector3dSpaceScene {

    private Color bgColor = Color.BLACK;
    private double refractiveIndex = 1;

    private int idCounter = 0;

    private final Collection<Lighting> lights = new ArrayList<>();
    private final Map<VectorSpaceObjectIdentity, PositionedObject<?, ?>> objectMap = new HashMap<>();

    private static class Lighting {

        private final Vector3d position;
        private final Light light;

        public Lighting(Vector3d position, Light light) {
            this.position = position;
            this.light = light;
        }
    }

    private static class PositionedObject<O extends SceneObject<P>, P extends Positionable.Positioning> {

        private final VectorSpaceObjectIdentity identity;

        private final P positioning;
        private final O object;

        public PositionedObject(VectorSpaceObjectIdentity identity, P positioning, O object) {
            this.identity = identity;
            this.positioning = positioning;
            this.object = object;
        }

        @Override
        public String toString() {
            return object.getClass().getSimpleName() + "[" + identity.getId() + "](" + identity.getName() + ")" +
                    "{identity=" + identity +
                    ", positioning=" + positioning +
                    ", object=" + object +
                    '}';
        }
    }

    private class PositionedCamera implements Camera {

        private final Vector3d position;
        private final UnitVector3d faceDirection;
        private final UnitVector3d downDirection;
        private final double fieldOfView;

        public PositionedCamera(
                Vector3d position, UnitVector3d faceDirection, UnitVector3d downDirection, double fieldOfView) {
            this.position = Objects.requireNonNull(position, "position cannot be null.");
            this.faceDirection = Objects.requireNonNull(faceDirection, "faceDirection cannot be null.");
            this.downDirection = Objects.requireNonNull(downDirection, "downDirection cannot be null.");
            this.fieldOfView = fieldOfView;
        }

        @Override
        public Vector3dSpaceScene getScene() {
            return Scene3d.this;
        }

        @Override
        public BufferedImage renderAsImage(int width, int height) {
            try {
                BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                ProjectionInfo projectionInfo = new ProjectionInfo(width, height);
                Renderer renderer = new Renderer(image, projectionInfo);
                ForkJoinPool.commonPool().submit(renderer);
                renderer.get();
                return image;
            } catch (InterruptedException | ExecutionException ex) {
                throw new RuntimeException(ex);
            }
        }

        private class ProjectionInfo {
            private final Vector3d screenCenter = position.plus(faceDirection);
            private final double fovSideWidth = Math.tan(fieldOfView / 2.0);
            private final double screenRatio;
            private final UnitVector3d screenXDir = downDirection.cross(faceDirection).normalize();
            private final UnitVector3d screenYDir = downDirection;
            private final double xFactor;
            private final double yFactor =  fovSideWidth;

            private ProjectionInfo(int width, int height) {
                screenRatio = width / (double) height;
                xFactor =  fovSideWidth * screenRatio;
            }
        }

        private class Renderer extends RecursiveAction {

            private final int THRESHOLD = 100;

            private final BufferedImage image;

            private final ProjectionInfo projectionInfo;
            private final int x0;
            private final int y0;
            private final int x1;
            private final int y1;

            private Renderer(BufferedImage image, ProjectionInfo projectionInfo) {
                this(image, projectionInfo, 0, 0, (image.getWidth() - 1), (image.getHeight() - 1));
            }

            private Renderer(BufferedImage image, ProjectionInfo projectionInfo, int x0, int y0, int x1, int y1) {
                this.image = image;
                this.projectionInfo = projectionInfo;

                this.x0 = x0;
                this.y0 = y0;
                this.x1 = x1;
                this.y1 = y1;
            }

            @Override
            protected void compute() {
                int width = x1 - x0;
                int height = y1 - y0;
                int count = width * height;
                if (count <= THRESHOLD) {
                    render();
                    return;
                }

                int x01 = x1;
                int y01 = y1;
                int x10 = x0;
                int y10 = y0;
                if (width >= height) {
                    x01 = x0 + (width / 2);
                    x10 = x01 + 1;
                } else {
                    y01 = y0 + (height / 2);
                    y10 = y01 + 1;
                }

                Renderer renderer0 = new Renderer(image, projectionInfo, x0, y0, x01, y01);
                Renderer renderer1 = new Renderer(image, projectionInfo, x10, y10, x1, y1);
                invokeAll(renderer0, renderer1);
            }

            private void render() {
                for (int i = x0; i <= x1; ++i) {
                    for (int j = y0; j <= y1; ++j) {
                        double x = ((2.0 * ((i + 0.5) / (double) image.getWidth()) - 1) * projectionInfo.xFactor);
                        double y = ((2.0 * ((j + 0.5) / (double) image.getHeight()) - 1) * projectionInfo.yFactor);
                        Vector3d pixel =
                                projectionInfo.screenCenter
                                        .plus(projectionInfo.screenXDir.times(x))
                                        .plus(projectionInfo.screenYDir.times(y));
                        UnitVector3d ray = pixel.minus(position).normalize();
                        int rgb = castRay(position, ray, 0).map(Color::getRGB).orElse(bgColor.getRGB());
                        image.setRGB(i, j, rgb);
                    }
                }
            }

            private Optional<Color> castRay(Vector3d source, UnitVector3d ray, final int depth) {
                if (depth > 4) return Optional.empty();

                Optional<SurfacePoint> optSurface = intersectScene(source, ray);
                if (! optSurface.isPresent()) return Optional.empty();

                SurfacePoint surface = optSurface.get();
                double diffuseLightIntensity = 0;
                double specularLightIntensity = 0;

                UnitVector3d reflectDir = reflect(ray, surface.getNormal());

                Optional<UnitVector3d> optRefractDir =
                        refract(ray, surface.getNormal(), surface.getMaterial().getRefractiveIndex());
                Vector3d reflectSrc =
                        reflectDir.dot(surface.getNormal()) < 0
                                ? surface.getPoint().minus(surface.getNormal().times(1e-3))
                                : surface.getPoint().plus(surface.getNormal().times(1e-3));
                Color reflectColor = castRay(reflectSrc, reflectDir, (depth + 1)).orElse(bgColor);
                Color refractColor = optRefractDir.flatMap(v -> {
                            Vector3d refractSrc =
                                    v.dot(surface.getNormal()) < 0
                                            ? surface.getPoint().minus(surface.getNormal().times(1e-3))
                                            : surface.getPoint().plus(surface.getNormal().times(1e-3));
                            return castRay(refractSrc, v, (depth + 1));
                        })
                        .orElse(bgColor);

                for (Lighting lighting : lights) {
                    UnitVector3d lightDir = lighting.position.minus(surface.getPoint()).normalize();

                    if (checkPointAtShadow(surface.getPoint(), surface.getNormal(), lighting.position, lightDir)) {
                        continue;
                    }

                    diffuseLightIntensity +=
                            lighting.light.getIntensity() * Math.max(0f, lightDir.dot(surface.getNormal()));
                    specularLightIntensity +=
                            calculateSpecularIntensity(
                                    ray, lightDir, surface.getNormal(),
                                    lighting.light, surface.getMaterial());
                }
                return Optional.of(
                        calculateFinalColor(
                                surface.getMaterial(), diffuseLightIntensity, specularLightIntensity,
                                reflectColor, refractColor));
            }

            private Optional<SurfacePoint> intersectScene(Vector3d source, UnitVector3d dir) {
                double shortestDist = Double.MAX_VALUE;
                SurfacePoint surface = null;
                for (PositionedObject positionedObject : objectMap.values()) {
                    // The type should've already been checked in the construction time of "PositionedObject", therefore
                    // it is safe here to assume the type of the "PositionedObject.positioning" match.
                    @SuppressWarnings("unchecked")
                    Optional<SurfacePoint> optSurface =
                            positionedObject.object.cast(positionedObject.positioning, source, dir);
                    if (! optSurface.isPresent()) continue;

                    SurfacePoint objSurface = optSurface.get();
                    double hitDist = Math.abs(objSurface.getPoint().minus(source).norm());
                    if (hitDist >= shortestDist) continue;

                    shortestDist = hitDist;
                    surface = objSurface;
                }

                // Ignores the rays which go too far away
                return shortestDist < 1000 ? Optional.ofNullable(surface) : Optional.empty();
            }

            private boolean checkPointAtShadow(
                    Vector3d point, UnitVector3d normal, Vector3d lightPos, UnitVector3d lightDir) {
                double lightDist = lightPos.minus(point).norm();
                Vector3d shadowSrc = normal.dot(lightDir) < 0
                        ? point.minus(normal.times(1e-3)) : point.plus(normal.times(1e-3));
                Optional<SurfacePoint> optSurface = intersectScene(shadowSrc, lightDir);
                return optSurface.map(sf -> sf.getPoint().minus(shadowSrc).norm() < lightDist).orElse(Boolean.FALSE);
            }

            private UnitVector3d reflect(UnitVector3d ray, UnitVector3d normal) {
                return ray.minus(normal.times(2.0).times(ray.dot(normal))).normalize();
            }

            private Optional<UnitVector3d> refract(UnitVector3d ray, UnitVector3d normal, double refractiveIndex) {
                double c = -ray.dot(normal);
                if (c > 1) c = 1;
                else if (c < -1) c = -1;

                double idx1 = Scene3d.this.refractiveIndex;
                double idx2 = refractiveIndex;
                if (c < 0) {
                    c = -c;
                    normal = normal.negate();
                    double idx = idx1;
                    idx1 = idx2;
                    idx2 = idx;
                }
                double r = idx1 / idx2;
                double nFactor = 1 - (r * r * (1 - (c * c)));
                return nFactor >= 0
                        ? Optional.of(ray.times(r).plus(normal.times((r * c) - Math.sqrt(nFactor))).normalize())
                        : Optional.empty();
            }

            private double calculateSpecularIntensity(
                    UnitVector3d ray, UnitVector3d lightDir, UnitVector3d normal,
                    Light light, Material material) {
                Vector3d reflect = reflect(lightDir.negate(), normal);
                double reflectIntensity = reflect.dot(ray.negate());
                if (reflectIntensity <= 0) return 0f;
                return light.getIntensity() * Math.pow(reflectIntensity, material.getSpecularExponent());
            }

            private Color calculateFinalColor(
                    Material material, double diffuseLightIntensity, double specularLightIntensity,
                    Color reflectColor, Color refractColor) {
                double specular = specularLightIntensity * material.getSpecularAlbedo();
                float[] diffuseRgbParts = material.getDiffuseColor().getRGBColorComponents(null);
                float[] reflectRgbParts = reflectColor.getRGBColorComponents(null);
                float[] refractRgbParts =
                        refractColor == null ? new float[]{0f, 0f, 0f} : refractColor.getRGBColorComponents(null);
                double r =
                        (diffuseRgbParts[0] * diffuseLightIntensity * material.getDiffuseAlbedo())
                                + specular
                                + (reflectRgbParts[0] * material.getReflectionAlbedo())
                                + (refractRgbParts[0] * material.getRefractiveAlbedo());
                double g =
                        (diffuseRgbParts[1] * diffuseLightIntensity * material.getDiffuseAlbedo())
                                + specular
                                + (reflectRgbParts[1] * material.getReflectionAlbedo())
                                + (refractRgbParts[1] * material.getRefractiveAlbedo());
                double b =
                        (diffuseRgbParts[2] * diffuseLightIntensity * material.getDiffuseAlbedo())
                                + specular
                                + (reflectRgbParts[2] * material.getReflectionAlbedo())
                                + (refractRgbParts[2] * material.getRefractiveAlbedo());
                double max = Math.max(r, Math.max(g, b));
                if (max > 1) {
                    r = r / max;
                    g = g / max;
                    b = b / max;
                }
                return new Color((float) r, (float) g, (float) b);
            }
        }
    }

    @Override
    public Camera setupCamera(
            Vector3d position, UnitVector3d faceDirection, UnitVector3d upDirection, double fieldOfView) {
        if (faceDirection.dot(upDirection) != 0) {
            throw new IllegalArgumentException(
                    "The faceDirection and upDirection are not perpendicular to each other.");
        }
        return new PositionedCamera(position, faceDirection, upDirection, fieldOfView);
    }

    @Override
    public void setBackgroundColor(Color color) {
        this.bgColor = color;
    }

    @Override
    public void setRefractiveIndex(float index) {
        this.refractiveIndex = index;
    }

    @Override
    public void putLight(Light light, Vector3d position) {
        lights.add(new Lighting(position, light));
    }

    @Override
    public <P extends Positionable.Positioning> VectorSpaceObjectIdentity putObject(
            SceneObject<P> object, P positioning) {
        VectorSpaceObjectIdentity identity =
                new VectorSpaceObjectIdentity(++idCounter, object.toString());
        objectMap.put(identity, new PositionedObject<>(identity, positioning, object));
        return identity;
    }

    @Override
    public <P extends Positionable.Positioning> VectorSpaceObjectIdentity putObject(
            SceneObject<P> object, P positioning, String name) {
        VectorSpaceObjectIdentity identity = new VectorSpaceObjectIdentity(++idCounter, name);
        objectMap.put(identity, new PositionedObject<>(identity, positioning, object));
        return identity;
    }
}
