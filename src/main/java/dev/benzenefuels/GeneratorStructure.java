package dev.benzenefuels;

import static com.gtnewhorizon.structurelib.structure.StructureUtility.*;
import static gregtech.api.util.GTStructureUtility.buildHatchAdder;
import static gregtech.api.util.GTStructureUtility.ofCoil;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.gtnewhorizon.structurelib.structure.IStructureDefinition;
import com.gtnewhorizon.structurelib.structure.StructureDefinition;

import cpw.mods.fml.common.registry.GameRegistry;

/** Same voxel source as the design preview; optional hatches have separate symbols. */
public final class GeneratorStructure {

    public static final int X_OFFSET = 16, Y_OFFSET = 126;
    public static final List<Cell> CELLS = new ArrayList<>();
    private static IStructureDefinition<ResonanceGenerator> definition;

    public static final class Cell {

        public final int x, y, z;
        public final String role;
        public final Block block;
        public final int meta;

        Cell(int x, int y, int z, String role, Block block, int meta) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.role = role;
            this.block = block;
            this.meta = meta;
        }

        public boolean frontHatch() {
            return z == -16 && y >= 2
                && y <= 3
                && (role.equals("shielding") || role.endsWith("port") && !role.equals("output_port")
                    || role.equals("maintenance"));
        }

        public boolean dynamo() {
            return z == 14 && (y == 2 || y == 3) && (role.equals("frame") || role.equals("output_port"));
        }

        public boolean light() {
            return role.equals("light") || role.equals("beacon");
        }
    }

    private GeneratorStructure() {}

    public static synchronized IStructureDefinition<ResonanceGenerator> definition() {
        if (definition != null) return definition;
        JsonObject json;
        try (InputStreamReader reader = new InputStreamReader(
            GeneratorStructure.class.getResourceAsStream("/assets/benzenefuels/generator-structure-runtime.json"),
            StandardCharsets.UTF_8)) {
            json = new Gson().fromJson(reader, JsonObject.class);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot load generator shape", e);
        }
        Map<String, Character> symbols = new LinkedHashMap<>();
        StructureDefinition.Builder<ResonanceGenerator> builder = StructureDefinition.builder();
        char[][][] shape = new char[33][129][33];
        for (char[][] plane : shape) for (char[] row : plane) java.util.Arrays.fill(row, ' ');
        for (JsonElement value : json.getAsJsonArray("voxels")) {
            JsonArray v = value.getAsJsonArray();
            int x = v.get(0)
                .getAsInt(),
                y = v.get(1)
                    .getAsInt(),
                z = v.get(2)
                    .getAsInt();
            String role = v.get(3)
                .getAsString();
            JsonObject entry = json.getAsJsonObject("materials")
                .getAsJsonObject(role);
            String id = entry.get("registry_name")
                .isJsonNull() ? null
                    : entry.get("registry_name")
                        .getAsString();
            Block block = null;
            int meta = 0;
            if (id != null && !role.endsWith("port") && !role.equals("maintenance")) {
                String[] key = id.split(":", 2);
                block = GameRegistry.findBlock(key[0], key[1]);
                meta = entry.get("metadata")
                    .getAsInt();
                if (block == null || block == Blocks.air)
                    throw new IllegalStateException("Missing structure block " + id);
            }
            Cell cell = new Cell(x, y, z, role, block, meta);
            CELLS.add(cell);
            char symbol;
            if (role.equals("controller")) symbol = '~';
            else if (cell.frontHatch()) symbol = 'H';
            else if (cell.dynamo()) symbol = 'D';
            else {
                Character known = symbols.get(role);
                if (known == null) {
                    symbol = (char) ('a' + symbols.size());
                    symbols.put(role, symbol);
                    if (cell.light()) builder.addElement(
                        symbol,
                        ofChain(
                            ofBlock(FuelContent.resonanceLightCasing, 0),
                            ofBlock(FuelContent.resonanceLightCasing, 1)));
                    else if (role.equals("heating_coil")) builder
                        .addElement(symbol, ofCoil(ResonanceGenerator::setCoilLevel, ResonanceGenerator::getCoilLevel));
                    else builder.addElement(symbol, ofBlock(block, meta));
                } else symbol = known;
            }
            shape[z + 16][128 - y][x + 16] = symbol;
        }
        String[][] strings = new String[33][129];
        for (int z = 0; z < 33; z++) for (int y = 0; y < 129; y++) strings[z][y] = new String(shape[z][y]);
        builder.addShape("tower", strings);
        builder.addElement(
            'H',
            buildHatchAdder(ResonanceGenerator.class).adder(ResonanceGenerator::addFrontHatch)
                .hatchClasses(
                    gregtech.api.metatileentity.implementations.MTEHatchInput.class,
                    gregtech.api.metatileentity.implementations.MTEHatchMaintenance.class)
                .casingIndex(176)
                .hint(1)
                .buildAndChain(gregtech.api.GregTechAPI.sBlockCasings8, 0));
        builder.addElement(
            'D',
            buildHatchAdder(ResonanceGenerator.class).adder(ResonanceGenerator::addRearDynamo)
                .hatchClass(gregtech.api.metatileentity.implementations.MTEHatchDynamo.class)
                .casingIndex(16)
                .hint(2)
                .buildAndChain(gregtech.api.GregTechAPI.sBlockCasings2, 0));
        definition = builder.build();
        return definition;
    }
}
