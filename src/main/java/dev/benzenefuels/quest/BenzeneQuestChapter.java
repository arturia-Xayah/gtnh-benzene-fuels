package dev.benzenefuels.quest;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.MinecraftForge;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import betterquesting.api.events.DatabaseEvent;
import betterquesting.api.storage.BQ_Settings;
import betterquesting.api.utils.NBTConverter;
import betterquesting.api.utils.UuidConverter;
import betterquesting.handlers.SaveLoadHandler;
import betterquesting.questing.QuestDatabase;
import betterquesting.questing.QuestInstance;
import betterquesting.questing.QuestLine;
import betterquesting.questing.QuestLineDatabase;
import betterquesting.questing.QuestLineEntry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import dev.benzenefuels.BenzeneFuels;

/** Additive native BQ chapter. Never replaces an existing quest definition or its progress. */
public final class BenzeneQuestChapter {

    private static BenzeneQuestChapter instance;
    private final JsonObject bundle;
    private final UUID chapterId;
    private final Map<UUID, NBTTagCompound> quests = new LinkedHashMap<>();
    private final Map<UUID, NBTTagCompound> entries = new LinkedHashMap<>();
    private final List<UUID> anchors = new ArrayList<>();

    public BenzeneQuestChapter() throws IOException {
        try (Reader reader = new InputStreamReader(
            BenzeneQuestChapter.class.getResourceAsStream("/assets/benzenefuels/quests/chapter.json"),
            StandardCharsets.UTF_8)) {
            bundle = new JsonParser().parse(reader)
                .getAsJsonObject();
        }
        patchController(bundle);
        chapterId = id(tag(bundle.getAsJsonObject("chapter")), "questLineID");
        for (JsonElement element : bundle.getAsJsonArray("quests")) {
            JsonObject row = element.getAsJsonObject();
            NBTTagCompound quest = tag(row.getAsJsonObject("quest"));
            UUID questId = id(quest, "questID");
            if (quests.put(questId, quest) != null) throw new IOException("Duplicate quest UUID " + questId);
            entries.put(questId, tag(row.getAsJsonObject("entry")));
        }
        for (JsonElement element : bundle.getAsJsonArray("anchors")) {
            NBTTagCompound entry = tag(element.getAsJsonObject());
            UUID questId = id(entry, "questID");
            anchors.add(questId);
            entries.put(questId, entry);
        }
    }

    private static void patchController(JsonElement element) {
        if (element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();
            if (object.has("id:8") && "gregtech:gt.blockmachines".equals(
                object.get("id:8")
                    .getAsString())
                && object.has("Damage:2")
                && object.get("Damage:2")
                    .getAsInt() == 32240) {
                object.addProperty("Damage:2", BenzeneFuels.generatorMetaId);
            }
            for (Map.Entry<String, JsonElement> child : object.entrySet()) patchController(child.getValue());
        } else if (element.isJsonArray()) {
            for (JsonElement child : element.getAsJsonArray()) patchController(child);
        }
    }

    public static void install() {
        try {
            instance = new BenzeneQuestChapter();
            MinecraftForge.EVENT_BUS.register(instance);
            instance.exportDefaults(
                Paths.get(BQ_Settings.defaultDir)
                    .resolve("DefaultQuests"));
        } catch (Exception error) {
            BenzeneFuels.LOG.error("Could not install benzene quest chapter; original quest book is unchanged", error);
        }
    }

    @SubscribeEvent
    public void loaded(DatabaseEvent.Load event) {
        if (event.getType() == DatabaseEvent.DBType.ALL && FMLCommonHandler.instance()
            .getEffectiveSide()
            .isServer()) {
            mergeMissing();
        }
    }

    public UUID chapterId() {
        return chapterId;
    }

    public List<UUID> questIds() {
        return new ArrayList<>(quests.keySet());
    }

    /** Existing worlds load their own database; add only absent objects after BQ restores progress. */
    public boolean mergeMissing() {
        for (UUID anchor : anchors) {
            if (!QuestDatabase.INSTANCE.containsKey(anchor)) {
                BenzeneFuels.LOG.warn("Benzene chapter skipped: required GTNH quest {} is absent", anchor);
                return false;
            }
        }
        boolean changed = false;
        for (Map.Entry<UUID, NBTTagCompound> entry : quests.entrySet()) {
            if (QuestDatabase.INSTANCE.containsKey(entry.getKey())) continue;
            QuestInstance quest = new QuestInstance();
            quest.readFromNBT(entry.getValue());
            QuestDatabase.INSTANCE.put(entry.getKey(), quest);
            changed = true;
        }
        if (!QuestLineDatabase.INSTANCE.containsKey(chapterId)) {
            QuestLine chapter = new QuestLine();
            chapter.readFromNBT(tag(bundle.getAsJsonObject("chapter")));
            for (Map.Entry<UUID, NBTTagCompound> entry : entries.entrySet()) {
                chapter.put(entry.getKey(), new QuestLineEntry(entry.getValue()));
            }
            QuestLineDatabase.INSTANCE.put(chapterId, chapter);
            UUID after = UUID.fromString(
                bundle.get("insertAfter")
                    .getAsString());
            QuestLineDatabase.INSTANCE.setOrderIndex(chapterId, QuestLineDatabase.INSTANCE.getOrderIndex(after) + 1);
            changed = true;
        }
        if (changed) {
            SaveLoadHandler.INSTANCE.markDirty();
            BenzeneFuels.LOG.info("Added missing benzene quests; preserved all existing definitions and progress");
        }
        return changed;
    }

    /** Also extend native defaults so BQ's default-load command retains the chapter. */
    public void exportDefaults(Path root) throws IOException {
        if (!Files.isRegularFile(root.resolve("QuestSettings.json"))) return;
        for (UUID anchor : anchors) {
            String suffix = "-" + UuidConverter.encodeUuid(anchor) + ".json";
            try (Stream<Path> paths = Files.walk(root.resolve("Quests"))) {
                if (!paths.anyMatch(
                    p -> p.getFileName()
                        .toString()
                        .endsWith(suffix))) {
                    BenzeneFuels.LOG.warn("Benzene defaults skipped: GTNH anchor {} is absent", anchor);
                    return;
                }
            }
        }
        String folder = "BenzeneBeyondHori-" + UuidConverter.encodeUuid(chapterId);
        Path questDir = root.resolve("Quests")
            .resolve(folder);
        Path lineDir = root.resolve("QuestLines")
            .resolve(folder);
        Files.createDirectories(questDir);
        Files.createDirectories(lineDir);
        for (JsonElement element : bundle.getAsJsonArray("quests")) {
            JsonObject row = element.getAsJsonObject();
            UUID questId = id(tag(row.getAsJsonObject("quest")), "questID");
            String name = row.get("key")
                .getAsString() + "-"
                + UuidConverter.encodeUuid(questId)
                + ".json";
            writeNew(questDir.resolve(name), row.getAsJsonObject("quest"));
            writeNew(lineDir.resolve(name), row.getAsJsonObject("entry"));
        }
        for (JsonElement element : bundle.getAsJsonArray("anchors")) {
            UUID questId = id(tag(element.getAsJsonObject()), "questID");
            writeNew(lineDir.resolve("Anchor-" + UuidConverter.encodeUuid(questId) + ".json"), element);
        }
        writeNew(lineDir.resolve("QuestLine.json"), bundle.getAsJsonObject("chapter"));
        Path orderPath = root.resolve("QuestLinesOrder.txt");
        List<String> order = Files.readAllLines(orderPath, StandardCharsets.UTF_8);
        String encoded = UuidConverter.encodeUuid(chapterId);
        if (order.stream()
            .anyMatch(line -> line.startsWith(encoded + ":"))) return;
        String after = UuidConverter.encodeUuid(
            UUID.fromString(
                bundle.get("insertAfter")
                    .getAsString()));
        int index = order.size();
        for (int i = 0; i < order.size(); i++) if (order.get(i)
            .startsWith(after + ":")) index = i + 1;
        order.add(index, encoded + ": The Great Lord of the Benzene Rings");
        Path backup = root.resolve("QuestLinesOrder.txt.before-benzenefuels");
        if (!Files.exists(backup)) Files.copy(orderPath, backup);
        Path temp = Files.createTempFile(root, "benzene-order-", ".tmp");
        try {
            Files.write(temp, order, StandardCharsets.UTF_8);
            Files.move(temp, orderPath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } finally {
            Files.deleteIfExists(temp);
        }
    }

    private static void writeNew(Path path, JsonElement element) throws IOException {
        if (!Files.exists(path)) Files.write(
            path,
            (new GsonBuilder().setPrettyPrinting()
                .create()
                .toJson(element) + "\n").getBytes(StandardCharsets.UTF_8),
            StandardOpenOption.CREATE_NEW);
    }

    private static NBTTagCompound tag(JsonObject object) {
        return NBTConverter.JSONtoNBT_Object(object, new NBTTagCompound(), true);
    }

    private static UUID id(NBTTagCompound tag, String prefix) {
        return new UUID(tag.getLong(prefix + "High"), tag.getLong(prefix + "Low"));
    }
}
