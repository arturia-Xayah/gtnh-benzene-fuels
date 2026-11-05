package dev.benzenefuels.packtest;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fluids.FluidRegistry;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;

import betterquesting.api.properties.NativeProps;
import betterquesting.api.questing.IQuest;
import betterquesting.api.storage.BQ_Settings;
import betterquesting.api.utils.UuidConverter;
import betterquesting.api2.utils.ParticipantInfo;
import betterquesting.commands.admin.QuestCommandDefaults;
import betterquesting.handlers.SaveLoadHandler;
import betterquesting.questing.QuestDatabase;
import betterquesting.questing.QuestLineDatabase;
import bq_standard.tasks.TaskFluid;
import cpw.mods.fml.common.registry.GameRegistry;
import dev.benzenefuels.FuelContent;
import dev.benzenefuels.FuelStage;
import dev.benzenefuels.quest.BenzeneQuestChapter;
import gregtech.api.GregTechAPI;

/** Real BQ database, task and persistence checks, only in a disposable dedicated-server world. */
final class QuestSuite {

    static void run(DisplaySuite.Check check) throws Exception {
        BenzeneQuestChapter chapter = new BenzeneQuestChapter();
        check.test(
            "quests.autoInstalled",
            QuestLineDatabase.INSTANCE.containsKey(chapter.chapterId()),
            "Native GTNH default book includes the chapter on first world startup");
        JsonObject bundle;
        try (InputStreamReader reader = new InputStreamReader(
            QuestSuite.class.getResourceAsStream("/assets/benzenefuels/quests/chapter.json"),
            StandardCharsets.UTF_8)) {
            bundle = new JsonParser().parse(reader)
                .getAsJsonObject();
        }
        validateRegistry(bundle, check);
        Map<String, UUID> ids = new HashMap<>();
        for (JsonElement row : bundle.getAsJsonArray("quests")) {
            JsonObject quest = row.getAsJsonObject()
                .getAsJsonObject("quest");
            UUID id = new UUID(
                quest.get("questIDHigh:4")
                    .getAsLong(),
                quest.get("questIDLow:4")
                    .getAsLong());
            ids.put(
                row.getAsJsonObject()
                    .get("key")
                    .getAsString(),
                id);
            IQuest loaded = QuestDatabase.INSTANCE.get(id);
            check.test(
                "quests.loaded." + row.getAsJsonObject()
                    .get("key")
                    .getAsString(),
                loaded != null && loaded.getTasks()
                    .size()
                    == quest.getAsJsonObject("tasks:9")
                        .entrySet()
                        .size()
                    && loaded.getProperty(NativeProps.REPEAT_TIME) == -1,
                "Native task deserialization and one-time reward policy");
        }
        check.test(
            "quests.layout",
            QuestLineDatabase.INSTANCE.get(chapter.chapterId())
                .size() == 53,
            "46 new quests and seven references to original quests");
        int countBefore = QuestDatabase.INSTANCE.size();
        UUID originalId = new UUID(0, 3133);
        IQuest original = QuestDatabase.INSTANCE.get(originalId);
        NBTTagCompound originalDefinition = original.writeToNBT(new NBTTagCompound());
        FakePlayer player = new FakePlayer(
            MinecraftServer.getServer()
                .worldServerForDimension(0),
            new GameProfile(UUID.fromString("d723ed96-576a-4acc-af1d-91aa0d5f67ef"), "BenzeneQuestTest"));
        UUID playerId = new ParticipantInfo(player).UUID;
        original.setComplete(playerId, 12345L);
        original.setClaimed(playerId, 12345L);
        NBTTagCompound originalProgress = original.writeProgressToNBT(new NBTTagCompound(), null);
        // Emulate installing the addon into an existing book, without resetting any original quests.
        for (UUID id : chapter.questIds()) QuestDatabase.INSTANCE.remove(id);
        QuestLineDatabase.INSTANCE.remove(chapter.chapterId());
        check.test(
            "quests.existingWorldMerge",
            chapter.mergeMissing() && QuestDatabase.INSTANCE.size() == countBefore,
            "Only missing addon quests inserted");
        check.test(
            "quests.originalUntouched",
            original == QuestDatabase.INSTANCE.get(originalId)
                && originalDefinition.equals(original.writeToNBT(new NBTTagCompound()))
                && originalProgress.equals(original.writeProgressToNBT(new NBTTagCompound(), null)),
            "Original definition, instance, completion and claim state retained");
        IQuest editable = QuestDatabase.INSTANCE.get(ids.get("welcome"));
        editable.setProperty(NativeProps.NAME, "Administrator custom title");
        check.test(
            "quests.idempotent",
            !chapter.mergeMissing() && "Administrator custom title".equals(editable.getProperty(NativeProps.NAME)),
            "Repeated insertion preserves administrator edits and adds no duplicates");
        QuestDatabase.INSTANCE.remove(originalId);
        IQuest temporarilyRemoved = QuestDatabase.INSTANCE.remove(ids.get("stars"));
        check.test(
            "quests.missingAnchor",
            !chapter.mergeMissing() && !QuestDatabase.INSTANCE.containsKey(ids.get("stars")),
            "Missing native prerequisite prevents insertion");
        QuestDatabase.INSTANCE.put(originalId, original);
        QuestDatabase.INSTANCE.put(ids.get("stars"), temporarilyRemoved);
        IQuest materials = QuestDatabase.INSTANCE.get(ids.get("tower_materials"));
        check.test(
            "quests.optionalMaterials",
            materials.getTasks()
                .getValue(1)
                .ignored(playerId)
                && materials.getRewards()
                    .size() == 0,
            "Planning checklist never blocks the checkbox branch or supplies uncraftable parts");
        for (String language : new String[] { "zh_CN", "en_US" }) {
            java.util.Properties strings = new java.util.Properties();
            try (InputStreamReader reader = new InputStreamReader(
                QuestSuite.class.getResourceAsStream("/assets/benzenefuels/lang/" + language + ".lang"),
                StandardCharsets.UTF_8)) {
                strings.load(reader);
            }
            for (UUID questId : chapter.questIds()) {
                String prefix = "betterquesting.quest." + UuidConverter.encodeUuidStripPadding(questId);
                String title = strings.getProperty(prefix + ".name");
                String text = strings.getProperty(prefix + ".desc");
                check.test(
                    "quests.translation." + language + "." + questId,
                    title != null && text != null
                        && !String.format(text)
                            .isEmpty(),
                    "Native translation keys and formatter accepted");
            }
        }
        IQuest luv = QuestDatabase.INSTANCE.get(ids.get("fuel_luv"));
        check.test("quests.gatesLocked", !luv.isUnlocked(playerId), "Cannot skip activator prerequisite");
        for (UUID parent : luv.getRequirements()) QuestDatabase.INSTANCE.get(parent)
            .setComplete(playerId, 1L);
        check.test(
            "quests.gatesUnlocked",
            luv.isUnlocked(playerId),
            "Completion of real prerequisite unlocks fuel task");
        for (FuelStage stage : FuelStage.values()) {
            IQuest quest = QuestDatabase.INSTANCE.get(ids.get("fuel_" + stage.key));
            TaskFluid task = (TaskFluid) quest.getTasks()
                .getValue(0);
            for (int slot = 0; slot < player.inventory.getSizeInventory(); slot++)
                player.inventory.setInventorySlotContents(slot, null);
            player.inventory.setInventorySlotContents(0, FuelContent.cell(stage, 15));
            Map.Entry<UUID, IQuest> entry = new AbstractMap.SimpleImmutableEntry<>(ids.get("fuel_" + stage.key), quest);
            task.detect(new ParticipantInfo(player), entry);
            check
                .test("quests." + stage.key + ".short", !task.isComplete(playerId), "15 cells cannot satisfy 16000 mB");
            player.inventory.setInventorySlotContents(0, FuelContent.cell(stage, 16));
            task.detect(new ParticipantInfo(player), entry);
            check.test(
                "quests." + stage.key + ".detect",
                task.isComplete(playerId) && player.inventory.getStackInSlot(0).stackSize == 16,
                "Native BQ fluid detector accepts 16 custom cells without consuming fuel");
        }
        luv.setComplete(playerId, 1L);
        check.test("quests.rewardAvailable", luv.canClaim(player), "Milestone permits a reward");
        luv.claimReward(player);
        int coins = countCoins(player);
        check.test(
            "quests.rewardOnce",
            coins == 2 && luv.hasClaimed(playerId) && !luv.canClaim(player),
            "Two Chemist III coins, no second claim");
        Path defaults = Paths.get(BQ_Settings.defaultDir, "DefaultQuests");
        Path nativeQuest;
        try (java.util.stream.Stream<Path> paths = Files.walk(defaults.resolve("Quests"))) {
            nativeQuest = paths.filter(
                p -> p.toString()
                    .endsWith("-" + UuidConverter.encodeUuid(originalId) + ".json"))
                .findFirst()
                .get();
        }
        byte[] nativeBytes = Files.readAllBytes(nativeQuest);
        byte[] order = Files.readAllBytes(defaults.resolve("QuestLinesOrder.txt"));
        chapter.exportDefaults(defaults);
        chapter.exportDefaults(defaults);
        check.test(
            "quests.exportIdempotent",
            java.util.Arrays.equals(nativeBytes, Files.readAllBytes(nativeQuest))
                && java.util.Arrays.equals(order, Files.readAllBytes(defaults.resolve("QuestLinesOrder.txt"))),
            "Native files byte-for-byte unchanged; chapter ordering stable");
        SaveLoadHandler.INSTANCE.markDirty();
        SaveLoadHandler.INSTANCE.saveDatabases();
        SaveLoadHandler.INSTANCE.loadDatabases(MinecraftServer.getServer());
        check.test(
            "quests.diskPersistence",
            QuestDatabase.INSTANCE.get(ids.get("fuel_luv"))
                .hasClaimed(playerId)
                && QuestDatabase.INSTANCE.get(originalId)
                    .hasClaimed(playerId),
            "Addon and native quest progress survive actual save/load");
        QuestCommandDefaults.load(null, null, defaults.toFile(), false);
        check.test(
            "quests.nativeDefaultReload",
            QuestDatabase.INSTANCE.size() == countBefore && QuestLineDatabase.INSTANCE.containsKey(chapter.chapterId())
                && QuestDatabase.INSTANCE.get(ids.get("fuel_luv"))
                    .hasClaimed(playerId)
                && QuestDatabase.INSTANCE.get(originalId)
                    .hasClaimed(playerId),
            "Native default load retains chapter and both claim histories");
    }

    private static int countCoins(FakePlayer player) {
        int count = 0;
        for (ItemStack stack : player.inventory.mainInventory) {
            if (stack != null && stack.getItem() == GameRegistry.findItem("dreamcraft", "CoinChemistIII"))
                count += stack.stackSize;
        }
        return count;
    }

    private static void validateRegistry(JsonElement element, DisplaySuite.Check check) {
        if (element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();
            if (object.has("id:8")) {
                String name = object.get("id:8")
                    .getAsString();
                String[] parts = name.split(":", 2);
                boolean valid = GameRegistry.findItem(parts[0], parts[1]) != null;
                if ("gregtech:gt.blockmachines".equals(name))
                    valid &= GregTechAPI.METATILEENTITIES[object.get("Damage:2")
                        .getAsInt()] != null;
                check.test("quests.registry." + name, valid, "Native item/icon/reward exists");
            }
            if (object.has("FluidName:8")) {
                String name = object.get("FluidName:8")
                    .getAsString();
                check.test(
                    "quests.registry." + name,
                    FluidRegistry.getFluid(name) != null,
                    "Native fluid task resolves");
            }
            for (Map.Entry<String, JsonElement> child : object.entrySet()) validateRegistry(child.getValue(), check);
        } else if (element.isJsonArray())
            for (JsonElement child : element.getAsJsonArray()) validateRegistry(child, check);
    }
}
