package personal.william.raytracer;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class Scene3d implements Vector3dSpaceScene {

    private int bgColor = Color.BLACK.getRGB();

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
                                        .plus(projectionInfo.screenYVector.times(y));

                        image.setRGB(i, j, castRay(ray).orElse(bgColor));
                    }
                }
            }

            private class RayHit {

                private final Vector3d point;
                private final Vector3d normal;
                private final Material material;

                public RayHit(Vector3d point, Vector3d normal, Material material) {
                    this.point = point;
                    this.normal = normal;
                    this.material = material;
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

            private OptionalInt castRay(Vector3d ray) {
                Optional<RayHit> optHit = intersectScene(position, ray);
                if (! optHit.isPresent()) return OptionalInt.empty();

                RayHit hit = optHit.get();
                double diffuseLightIntensity = 0;
                double specularLightIntensity = 0;
                for (PositionedObject<Light> posLight : lights) {
                    Vector3d lightDir = posLight.position.minus(hit.getPoint()).normalize();

                    if (checkPointAtShadow(hit.getPoint(), hit.getNormal(), posLight.position, lightDir)) continue;

                    diffuseLightIntensity +=
                            posLight.object.getIntensity() * Math.max(0f, lightDir.dot(hit.getNormal()));
                    specularLightIntensity +=
                            calculateSpecularIntensity(
                                    ray.negate(), lightDir.negate(), hit.getNormal(),
                                    posLight.object, hit.getMaterial());
                }
                double specular = specularLightIntensity * hit.getMaterial().getSpecularAlbedo();
                float[] rgbParts = hit.getMaterial().getDiffuseColor().getRGBColorComponents(null);
                double r = rgbParts[0] * diffuseLightIntensity * hit.getMaterial().getDiffuseAlbedo() + specular;
                double g = rgbParts[1] * diffuseLightIntensity * hit.getMaterial().getDiffuseAlbedo() + specular;
                double b = rgbParts[2] * diffuseLightIntensity * hit.getMaterial().getDiffuseAlbedo() + specular;
                double max = Math.max(r, Math.max(g, b));
                if (max > 1) {
                    r = r / max;
                    g = g / max;
                    b = b / max;
                }
                int rgb = Math.round(0b11111111 * (float) r);
                rgb = (rgb << 8) + Math.round(0b11111111 * (float) g);
                rgb = (rgb << 8) + Math.round(0b11111111 * (float) b);
                return OptionalInt.of(rgb);
            }

            private Optional<RayHit> intersectScene(Vector3d orig, Vector3d dir) {
                dir = dir.normalize();
                double shortestDist = Double.MAX_VALUE;
                Vector3d hit = null;
                Vector3d normal = null;
                Material material = null;
                for (PositionedObject<SceneObject> posObj : objects) {
                    Optional<Vector3d> optHit = posObj.object.getFirstIntersection(posObj.position, orig, dir);
                    if (! optHit.isPresent()) continue;

                    Vector3d tmpHit = optHit.get();
                    double dist = tmpHit.norm();
                    if (dist >= shortestDist) continue;

                    shortestDist = dist;
                    hit = tmpHit;
                    normal = posObj.object.getNormalVector(posObj.position, hit);
                    material = posObj.object.getMaterial();
                }

                // Ignore the rays which go too far away
                return shortestDist < 1000 ? Optional.of(new RayHit(hit, normal, material)) : Optional.empty();
            }

            private boolean checkPointAtShadow(
                    Vector3d point, Vector3d normal, Vector3d lightPos, Vector3d lightDir) {
                double lightDist = lightPos.minus(point).norm();
                Vector3d shadowOrig = normal.dot(lightDir) < 0
                        ? point.minus(normal.times(1e-3)) : point.plus(normal.times(1e-3));
                Optional<RayHit> OptHit = intersectScene(shadowOrig, lightDir);
                if (! OptHit.isPresent()) return false;

                RayHit hit = OptHit.get();
                return (hit.getPoint().minus(shadowOrig)).norm() < lightDist;
            }

            private double calculateSpecularIntensity(
                    Vector3d orig, Vector3d light, Vector3d normal,
                    Light lightSource, Material material) {
                orig = orig.normalize();
                light = light.normalize();
                normal = normal.normalize();

                Vector3d reflect = light.minus(normal.times(2.0).times(light.dot(normal)));
                double reflectIntensity = reflect.dot(orig);
                if (reflectIntensity <= 0) return 0;
                return (lightSource.getIntensity() * Math.pow(reflectIntensity, material.getSpecularExponent()));
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
        this.bgColor = color.getRGB();
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
