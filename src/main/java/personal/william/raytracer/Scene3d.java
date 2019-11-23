package personal.william.raytracer;

import org.jscience.mathematics.number.Float64;
import org.jscience.mathematics.vector.Float64Vector;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.OptionalInt;

public class Scene3d implements VectorSpaceScene {

    private int bgColor = Color.BLACK.getRGB();

    private final Collection<PositionedObject<Light>> lights = new ArrayList<>();
    private final Collection<PositionedObject<SceneObject>> objects = new ArrayList<>();

    private static class PositionedObject<O> {

        private final Float64Vector position;
        private final O object;

        public PositionedObject(Float64Vector position, O object) {
            this.position = position;
            this.object = object;
        }
    }

    private class PositionedCamera implements Camera {

        private final Float64Vector position;
        private final Float64Vector faceDirection;
        private final Float64Vector upDirection;
        private final float fieldOfView;

        public PositionedCamera(
                Float64Vector position, Float64Vector faceDirection, Float64Vector upDirection, float fieldOfView) {
            this.position = position;
            this.faceDirection = VectorUtils.normalize(faceDirection);
            this.upDirection = VectorUtils.normalize(upDirection);
            this.fieldOfView = fieldOfView;
        }

        @Override
        public VectorSpaceScene getScene() {
            return Scene3d.this;
        }

        @Override
        public BufferedImage renderAsImage(int width, int height) {
            final Float64Vector screenCenter = position.plus(faceDirection);
            final float fovSideWidth = (float) (Math.tan(fieldOfView / 2.0));
            final float screenRatio = width / (float) height;
            final Float64Vector screenXVector = faceDirection.cross(upDirection);
            final Float64Vector screenYVector = upDirection;
            final float xFactor =  fovSideWidth * screenRatio;
            final float yFactor =  fovSideWidth;

            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            for (int i = 0; i < width; ++i) {
                for (int j = 0; j < height; ++j) {
                    float x = (float) ((2.0 * ((i + 0.5) / (float) width) - 1) * xFactor);
                    float y = (float) -((2.0 * ((j + 0.5) / (float) height) - 1) * yFactor);
                    Float64Vector ray = screenCenter.plus(screenXVector.times(x)).plus(screenYVector.times(y));

                    image.setRGB(i, j, castRay(ray).orElse(bgColor));
                }
            }
            return image;
        }

        private class RayHit {

            private final Float64Vector point;
            private final Float64Vector normal;
            private final Material material;

            public RayHit(Float64Vector point, Float64Vector normal, Material material) {
                this.point = point;
                this.normal = normal;
                this.material = material;
            }

            public Float64Vector getPoint() {
                return point;
            }

            public Float64Vector getNormal() {
                return normal;
            }

            public Material getMaterial() {
                return material;
            }
        }

        private OptionalInt castRay(Float64Vector ray) {
            Optional<RayHit> optHit = intersectScene(position, ray);
            if (! optHit.isPresent()) return OptionalInt.empty();

            RayHit hit = optHit.get();
            float diffuseLightIntensity = 0;
            float specularLightIntensity = 0;
            for (PositionedObject<Light> posLight : lights) {
                Float64Vector lightDir = VectorUtils.normalize(posLight.position.minus(hit.getPoint()));

                if (checkPointAtShadow(hit.getPoint(), hit.getNormal(), posLight.position, lightDir)) continue;

                diffuseLightIntensity +=
                        posLight.object.getIntensity() * Math.max(0f, lightDir.times(hit.getNormal()).floatValue());
                specularLightIntensity +=
                        calculateSpecularIntensity(
                                ray.opposite(), lightDir.opposite(), hit.getNormal(),
                                posLight.object, hit.getMaterial());
            }
            float specular = specularLightIntensity * hit.getMaterial().getSpecularAlbedo();
            float[] rgbParts = hit.getMaterial().getDiffuseColor().getRGBColorComponents(null);
            float r = rgbParts[0] * diffuseLightIntensity * hit.getMaterial().getDiffuseAlbedo() + specular;
            float g = rgbParts[1] * diffuseLightIntensity * hit.getMaterial().getDiffuseAlbedo() + specular;
            float b = rgbParts[2] * diffuseLightIntensity * hit.getMaterial().getDiffuseAlbedo() + specular;
            float max = Math.max(r, Math.max(g, b));
            if (max > 1) {
                r = r / max;
                g = g / max;
                b = b / max;
            }
            int rgb = Math.round(0b11111111 * r);
            rgb = (rgb << 8) + Math.round(0b11111111 * g);
            rgb = (rgb << 8) + Math.round(0b11111111 * b);
            return OptionalInt.of(rgb);
        }

        private Optional<RayHit> intersectScene(Float64Vector orig, Float64Vector dir) {
            dir = VectorUtils.normalize(dir);
            float shortestDist = Float.MAX_VALUE;
            Float64Vector hit = null;
            Float64Vector normal = null;
            Material material = null;
            for (PositionedObject<SceneObject> posObj : objects) {
                Optional<Float64Vector> optHit = posObj.object.getFirstIntersection(posObj.position, orig, dir);
                if (! optHit.isPresent()) continue;

                Float64Vector tmpHit = optHit.get();
                double dist = tmpHit.norm().doubleValue();
                if (dist >= shortestDist) continue;

                shortestDist = (float) dist;
                hit = tmpHit;
                normal = posObj.object.getNormalVector(posObj.position, hit);
                material = posObj.object.getMaterial();
            }

            // Ignore the rays which go too far away
            return shortestDist < 1000 ? Optional.of(new RayHit(hit, normal, material)) : Optional.empty();
        }

        private boolean checkPointAtShadow(
                Float64Vector point, Float64Vector normal, Float64Vector lightPos, Float64Vector lightDir) {
            Float64 lightDist = lightPos.minus(point).norm();
            Float64Vector shadowOrig = normal.times(lightDir).compareTo(0) < 0
                    ? point.minus(normal.times(1e-3)) : point.plus(normal.times(1e-3));
            Optional<RayHit> OptHit = intersectScene(shadowOrig, lightDir);
            if (! OptHit.isPresent()) return false;

            RayHit hit = OptHit.get();
            return (hit.getPoint().minus(shadowOrig)).norm().compareTo(lightDist) < 0;
        }

        private float calculateSpecularIntensity(
                Float64Vector orig, Float64Vector light, Float64Vector normal, Light lightSource, Material material) {
            orig = VectorUtils.normalize(orig);
            light = VectorUtils.normalize(light);
            normal = VectorUtils.normalize(normal);

            Float64Vector reflect = light.minus(normal.times(2.0).times(light.times(normal)));
            float reflectIntensity = reflect.times(orig).floatValue();
            if (reflectIntensity <= 0) return 0f;
            return (float) (lightSource.getIntensity() * Math.pow(reflectIntensity, material.getSpecularExponent()));
        }
    }

    @Override
    public Camera setupCamera(
            Float64Vector position, Float64Vector faceDirection, Float64Vector upDirection, float fieldOfView) {
        if (! faceDirection.times(upDirection).equals(0.0)) {
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
    public void putLight(Light light, Float64Vector position) {
        lights.add(new PositionedObject<>(position, light));
    }

    @Override
    public void putObject(SceneObject object, Float64Vector position) {
        objects.add(new PositionedObject<>(position, object));
    }
}
