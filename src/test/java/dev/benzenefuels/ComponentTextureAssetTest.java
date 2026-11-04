package dev.benzenefuels;

import static org.junit.Assert.*;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;

import org.junit.Test;

public class ComponentTextureAssetTest {

    @Test
    public void everyComponentHasItsOwnPackagedIcon() throws Exception {
        assertEquals(
            43,
            ComponentTextures.all()
                .size());
        Set<String> names = new HashSet<>();
        Set<Integer> pixels = new HashSet<>();
        for (Map.Entry<Integer, String> entry : ComponentTextures.all()
            .entrySet()) {
            assertTrue(
                entry.getValue()
                    .startsWith("benzenefuels:"));
            assertTrue(names.add(entry.getValue()));
            BufferedImage image = load(
                "items/" + entry.getValue()
                    .split(":")[1]);
            assertEquals(64, image.getWidth());
            assertTrue(image.getHeight() == 64 || image.getHeight() == 4096);
            assertEquals(0, image.getRGB(0, 0) >>> 24);
            assertTrue(
                "No two components share the same first frame",
                pixels.add(Arrays.hashCode(image.getRGB(0, 0, 64, 64, null, 0, 64))));
        }
    }

    @Test
    public void routeFluidsAndCellsHaveNativeAnimationAndFixedShells() throws Exception {
        for (GeneratorRoute route : GeneratorRoute.values()) for (int mk = 1; mk <= 5; mk++) {
            String key = route.key + "_mk" + mk;
            BufferedImage fluid = animated("blocks/fluids/" + route.key + "_base_mk" + mk);
            BufferedImage cell = animated("items/base_cell_" + key);
            for (int frame = 0; frame < 64; frame++) for (int y = 0; y < 64; y++) for (int x = 0; x < 64; x++) {
                assertEquals(255, fluid.getRGB(x, frame * 64 + y) >>> 24);
                assertEquals(cell.getRGB(x, y) >>> 24, cell.getRGB(x, frame * 64 + y) >>> 24);
            }
            for (int frame = 0; frame < 64; frame++) {
                assertEquals("Metal cap remains still", cell.getRGB(26, 5), cell.getRGB(26, frame * 64 + 5));
                assertEquals("Rank contacts remain still", cell.getRGB(22, 53), cell.getRGB(22, frame * 64 + 53));
            }
        }
        animated("blocks/fluids/uv_activator");
    }

    private static BufferedImage animated(String path) throws Exception {
        BufferedImage image = load(path);
        assertEquals(4096, image.getHeight());
        try (InputStream in = ComponentTextureAssetTest.class
            .getResourceAsStream("/assets/benzenefuels/textures/" + path + ".png.mcmeta")) {
            assertNotNull("Native animation metadata", in);
        }
        Set<Integer> frames = new HashSet<>();
        for (int i = 0; i < 64; i++) frames.add(Arrays.hashCode(image.getRGB(0, i * 64, 64, 64, null, 0, 64)));
        assertTrue("Genuinely animated fluid", frames.size() >= 60);
        return image;
    }

    private static BufferedImage load(String path) throws Exception {
        try (InputStream in = ComponentTextureAssetTest.class
            .getResourceAsStream("/assets/benzenefuels/textures/" + path + ".png")) {
            assertNotNull(path, in);
            BufferedImage image = ImageIO.read(in);
            assertNotNull(image);
            return image;
        }
    }
}
