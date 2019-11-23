package personal.william.raytracer;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class Scene3d implements Vector3dSpaceScene {

    private Color bgColor = Color.BLACK;
    private double refractiveIndex = 1;

    private final Collection<PositionedObject<Light>> lights = new ArrayList<>();
    private final Collection<PositionedObject<SceneObject>> objects = new ArrayList<>();

    private static class PositionedObject<O> {

        private final Vector3d position;
        private final O object;

        public PositionedObject(Vector3d position, O object) {
            this.position = position;
            this.object = object;
        }
    }

    private class PositionedCamera implements Camera {

        private final Vector3d position;
        private final Vector3d faceDirection;
        private final Vector3d upDirection;
        private final double fieldOfView;

        public PositionedCamera(
                Vector3d position, Vector3d faceDirection, Vector3d upDirection, double fieldOfView) {
            this.position = position;
            this.faceDirection = faceDirection.normalize();
            this.upDirection = upDirection.normalize();
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
            private final Vector3d screenXVector = faceDirection.cross(upDirection);
            private final Vector3d screenYVector = upDirection;
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
                        double y = -((2.0 * ((j + 0.5) / (double) image.getHeight()) - 1) * projectionInfo.yFactor);
                        Vector3d ray =
                                projectionInfo.screenCenter
                                        .plus(projectionInfo.screenXVector.times(x))
                                        .plus(projectionInfo.screenYVector.times(y)).normalize();

                        int rgb = castRay(position, ray, 0).map(Color::getRGB).orElse(bgColor.getRGB());
                        image.setRGB(i, j, rgb);
                    }
                }
            }

            private Optional<Color> castRay(Vector3d orig, Vector3d ray, final int depth) {
                if (depth > 4) return Optional.empty();

                Optional<SurfacePoint> optSurface = intersectScene(orig, ray);
                if (! optSurface.isPresent()) return Optional.empty();

                SurfacePoint surface = optSurface.get();
                double diffuseLightIntensity = 0;
                double specularLightIntensity = 0;

                Vector3d reflectDir = reflect(ray, surface.getNormal());

                Optional<Vector3d> optRefractDir =
                        refract(ray, surface.getNormal(), surface.getMaterial().getRefractiveIndex());
                Vector3d reflectOrig =
                        reflectDir.dot(surface.getNormal()) < 0
                                ? surface.getPoint().minus(surface.getNormal().times(1e-3))
                                : surface.getPoint().plus(surface.getNormal().times(1e-3));
                Color reflectColor = castRay(reflectOrig, reflectDir, (depth + 1)).orElse(bgColor);
                Color refractColor = optRefractDir.flatMap(v -> {
                            Vector3d refractOrig =
                                    v.dot(surface.getNormal()) < 0
                                            ? surface.getPoint().minus(surface.getNormal().times(1e-3))
                                            : surface.getPoint().plus(surface.getNormal().times(1e-3));
                            return castRay(refractOrig, v, (depth + 1));
                        })
                        .orElse(bgColor);

                for (PositionedObject<Light> posLight : lights) {
                    Vector3d lightDir = posLight.position.minus(surface.getPoint()).normalize();

                    if (checkPointAtShadow(surface.getPoint(), surface.getNormal(), posLight.position, lightDir)) {
                        continue;
                    }

                    diffuseLightIntensity +=
                            posLight.object.getIntensity() * Math.max(0f, lightDir.dot(surface.getNormal()));
                    specularLightIntensity +=
                            calculateSpecularIntensity(
                                    ray.negate(), lightDir.negate(), surface.getNormal(),
                                    posLight.object, surface.getMaterial());
                }
                return Optional.of(
                        calculateFinalColor(
                                surface.getMaterial(), diffuseLightIntensity, specularLightIntensity,
                                reflectColor, refractColor));
            }

            private Optional<SurfacePoint> intersectScene(Vector3d orig, Vector3d dir) {
                dir = dir.normalize();
                double shortestDist = Double.MAX_VALUE;
                SurfacePoint surface = null;
                for (PositionedObject<SceneObject> posObj : objects) {
                    Optional<SurfacePoint> optSurface = posObj.object.cast(posObj.position, orig, dir);
                    if (! optSurface.isPresent()) continue;

                    SurfacePoint objSurface = optSurface.get();
                    double hitDist = objSurface.getPoint().norm();
                    if (hitDist >= shortestDist) continue;

                    shortestDist = hitDist;
                    surface = objSurface;
                }

                // Ignore the rays which go too far away
                return shortestDist < 1000 ? Optional.ofNullable(surface) : Optional.empty();
            }

            private boolean checkPointAtShadow(
                    Vector3d point, Vector3d normal, Vector3d lightPos, Vector3d lightDir) {
                double lightDist = lightPos.minus(point).norm();
                Vector3d shadowOrig = normal.dot(lightDir) < 0
                        ? point.minus(normal.times(1e-3)) : point.plus(normal.times(1e-3));
                Optional<SurfacePoint> optSurface = intersectScene(shadowOrig, lightDir);
                return optSurface.map(sf -> sf.getPoint().minus(shadowOrig).norm() < lightDist).orElse(Boolean.FALSE);
            }

            private Vector3d reflect(Vector3d ray, Vector3d normal) {
                ray = ray.normalize();
                normal = normal.normalize();
                return ray.minus(normal.times(2.0).times(ray.dot(normal))).normalize();
            }

            private Optional<Vector3d> refract(Vector3d ray, Vector3d normal, double refractiveIndex) {
                ray = ray.normalize();
                normal = normal.normalize();

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
                    Vector3d orig, Vector3d light, Vector3d normal,
                    Light lightSource, Material material) {
                orig = orig.normalize();

                Vector3d reflect = reflect(light, normal);
                double reflectIntensity = reflect.dot(orig);
                if (reflectIntensity <= 0) return 0f;
                return (lightSource.getIntensity() * Math.pow(reflectIntensity, material.getSpecularExponent()));
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
            Vector3d position, Vector3d faceDirection, Vector3d upDirection, double fieldOfView) {
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
        lights.add(new PositionedObject<>(position, light));
    }

    @Override
    public void putObject(SceneObject object, Vector3d position) {
        objects.add(new PositionedObject<>(position, object));
    }
}
