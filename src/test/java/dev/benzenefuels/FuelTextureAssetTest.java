package dev.benzenefuels;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.imageio.ImageIO;

import org.junit.Test;

/** Checks the Minecraft animation contract and readable, genuinely animated packaged art. */
public class FuelTextureAssetTest {

    @Test
    public void fluidSpritesAreOpaqueAnimatedAndDistinct() throws Exception {
        Set<Integer> identities = new HashSet<>();
        for (FuelStage stage : FuelStage.values()) {
            BufferedImage image = sprite("blocks/fluids/benzene_" + stage.key);
            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    assertEquals("Fluid must cover the full tile", 255, image.getRGB(x, y) >>> 24);
                }
            }
            identities.add(Arrays.hashCode(image.getRGB(0, 0, 64, 64, null, 0, 64)));
        }
        assertEquals("Eight individual textures, not one common placeholder", 8, identities.size());
    }

    @Test
    public void cellsAnimateInsideAnUnchangingTransparentSilhouette() throws Exception {
        for (FuelStage stage : FuelStage.values()) {
            BufferedImage image = sprite("items/cell_" + stage.key);
            for (int frame = 0; frame < 64; frame++) {
                assertEquals("Transparent outside the cell", 0, image.getRGB(0, frame * 64) >>> 24);
                assertEquals("Metal cap stays static", image.getRGB(26, 5), image.getRGB(26, frame * 64 + 5));
                for (int y = 0; y < 64; y++) {
                    for (int x = 0; x < 64; x++) {
                        assertEquals(
                            "Stable item silhouette",
                            image.getRGB(x, y) >>> 24,
                            image.getRGB(x, frame * 64 + y) >>> 24);
                    }
                }
            }
        }
    }

    private static BufferedImage sprite(String path) throws Exception {
        String resource = "/assets/benzenefuels/textures/" + path + ".png";
        BufferedImage image;
        try (InputStream in = FuelTextureAssetTest.class.getResourceAsStream(resource)) {
            assertNotNull(resource, in);
            image = ImageIO.read(in);
        }
        assertNotNull("Valid PNG", image);
        assertEquals("Square animation frame", 64, image.getWidth());
        assertEquals("64 vertical frames", 4096, image.getHeight());
        try (InputStream in = FuelTextureAssetTest.class.getResourceAsStream(resource + ".mcmeta")) {
            assertNotNull("Minecraft must discover the animation", in);
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int count;
            while ((count = in.read(buffer)) != -1) bytes.write(buffer, 0, count);
            String metadata = new String(bytes.toByteArray(), StandardCharsets.UTF_8).replaceAll("\\s", "");
            assertEquals("Native animation timing", "{\"animation\":{\"frametime\":2}}", metadata);
        }
        Set<Integer> unique = new HashSet<>();
        for (int i = 0; i < 64; i++) unique.add(Arrays.hashCode(image.getRGB(0, i * 64, 64, 64, null, 0, 64)));
        assertTrue("Animation must contain at least 60 distinct frames", unique.size() >= 60);
        return image;
    }
}
