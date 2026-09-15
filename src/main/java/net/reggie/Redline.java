package net.reggie;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.hadences.common.CustomBossBarManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.reggie.commands.*;
import net.reggie.entity.ModEntities;
import net.reggie.entity.custom.GomuArmsEntity;
import net.reggie.game.abilities.AbilityComponent;
import net.reggie.game.component.CombatModeComponent;
import net.reggie.game.doriki.DorikiComponentImpl;
import net.reggie.game.doriki.IDorikiComponent;
import net.reggie.game.fruit.DevilFruitComponentImpl;
import net.reggie.game.fruit.IDevilFruitComponent;
import net.reggie.game.haki.HakiComponentImpl;
import net.reggie.game.haki.IHakiComponent;
import net.reggie.game.races.IRaceComponent;
import net.reggie.game.races.RaceComponentImpl;
import net.reggie.game.styles.FightingStyleComponentImpl;
import net.reggie.game.styles.IFightingStyleComponent;
import net.reggie.item.ModItemGroups;
import net.reggie.item.ModItems;
import net.reggie.network.ModNetworking;
import net.reggie.particle.ModParticles;
import net.reggie.sound.ModSounds;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentInitializer;
import org.ladysnake.cca.api.v3.entity.RespawnCopyStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Redline implements ModInitializer, EntityComponentInitializer {
	public static final String MOD_ID = "redline";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {

		CustomBossBarManager.initServer();

		ModItems.registerModItems();
		ModItemGroups.registerItemGroups();

		ModSounds.registerSounds();

		ModNetworking.registerC2SPackets();

		ModParticles.registerParticles();

		HakiCommand.register();
		HakiStatsCommand.register();
		DorikiCommand.register();
		FightingStyleCommand.register();
		RaceCommand.register();

		FabricDefaultAttributeRegistry.register(ModEntities.GOMU_ARMS, GomuArmsEntity.setAttributes());

		ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
			// Holt die Haki-Komponente des frisch respawnten Spielers
			var hakiComp = Redline.HAKI.get(newPlayer);

			// Setzt die Haki-Energie sofort auf das aktuelle Maximum des Spielers
			float maxHaki = hakiComp.getMaxHaki();
			hakiComp.setHaki(maxHaki);

			// --- NEU: ERZWINGT DAS AUSSCHALTEN DER AURA BEIM RESPAWN ---
			if (hakiComp.isHaoActive()) {
				hakiComp.setHaoActive(false);
			}

			// Zwingt Cardinal Components zur sofortigen Synchronisation mit dem Client HUD
			Redline.HAKI.sync(newPlayer);


			// --- NEU: ERZWUNGENER DORIKI RESPAWN HEAL FIX ---
			// Zwingt Cardinal Components dazu, die kopierten Doriki-Daten sofort mit dem neuen Körper zu verknüpfen
			Redline.DORIKI.sync(newPlayer);

			// Holt das durch Doriki modifizierte maximale Leben des neuen Körpers (Dank des Mixins)
			float finalMaxHealth = newPlayer.getMaxHealth();

			// Heilt den Spieler beim Aufwachen sofort auf seine echten 40/40 Herzen hoch!
			newPlayer.setHealth(finalMaxHealth);

		});

		ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
			if (damageSource.getAttacker() instanceof ServerPlayerEntity player) {
				if (entity instanceof PlayerEntity) return;

				var dorikiComp = Redline.DORIKI.get(player);

				// --- LIMIT PRÜFUNG ENTFERNT -> Geht nun unendlich hoch! ---

				float maxHealth = entity.getMaxHealth();
				long gainedDoriki = Math.max(1, (long) (maxHealth / 4));

				dorikiComp.addDoriki(gainedDoriki);

				String dorikiMessage = "§a+ " + gainedDoriki + " Doriki §7[§eTotal: " + dorikiComp.getDoriki() + "§7]";
				player.sendMessage(Text.literal(dorikiMessage), true);
			}
		});

		ServerTickEvents.START_SERVER_TICK.register(server -> {
			server.getPlayerManager().getPlayerList().forEach(player -> {
				Redline.HAKI.get(player).tick(); // ← Hier anpassen!
			});
		});

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			var player = handler.getPlayer();

			// Holt deine flache Haki-Komponente über den echten Redline-Key
			var hakiComp = Redline.HAKI.get(player);

			// 1.21.1 FIX: Holt das Optional<NbtCompound> aus dem PlayerManager
			var playerNbtOptional = server.getPlayerManager().loadPlayerData(player);

			// Überprüfung, ob Daten existieren UND ob das Ryou-Tag darin vorhanden ist
			boolean hasColorTag = false;
			if (playerNbtOptional.isPresent()) {
				var nbt = playerNbtOptional.get();
				// Cardinal Components speichert Daten oft in einem Unter-Tag namens "cardinal_components"
				// Wir prüfen zur Sicherheit sowohl das Haupt-NBT als auch, ob das Haki-System geladen wurde
				if (nbt.contains("RyouColorIndex") || nbt.contains("cardinal_components")) {
					hasColorTag = true;
				}
			}

			// Wenn der Spieler komplett neu ist (First Time Join), würfeln wir EINE feste Farbe aus
			if (!hasColorTag) {
				int randomColor = player.getRandom().nextInt(7); // Generiert eine Zahl von 0 bis 6
				hakiComp.setRyouColorIndex(randomColor);

				// --- WICHTIGER FIX: Zwingt den Server, die gewürfelte Farbe SOFORT dauerhaft zu speichern ---
				server.getPlayerManager().savePlayerData(player);
			}
		});

		ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
			// Prüft, ob der Angreifer ein echter Spieler auf dem Server war
			if (damageSource.getAttacker() instanceof ServerPlayerEntity player) {

				// Verhindert, dass man durch das Töten von anderen Spielern (oder sich selbst) Doriki farmt
				if (entity instanceof PlayerEntity) return;

				// Holt die Doriki-Komponente des Spielers
				var dorikiComp = net.reggie.Redline.DORIKI.get(player);
				long currentDoriki = dorikiComp.getDoriki();

				// Wenn das Mine-Mine-no-Mi Maximum (z.B. 10.000) erreicht ist, gibt es keine Punkte mehr
				if (currentDoriki >= 10000) return;

				// Berechnet Doriki-XP: Mobs mit mehr Max-HP geben deutlich mehr Doriki!
				float maxHealth = entity.getMaxHealth();
				long gainedDoriki = Math.max(1, (long) (maxHealth / 4));

				// Doriki hinzufügen (CCA triggert hierdurch automatisch den Client-Sync!)
				dorikiComp.addDoriki(gainedDoriki);

				// --- TEXTANZEIGE IN DER ACTIONBAR ---
				// Formatiert: + 5 Doriki [Total: 1250] in den originalen Mine-Mine-no-Mi Farben
				String dorikiMessage = "§a+ " + gainedDoriki + " Doriki §7[§eTotal: " + dorikiComp.getDoriki() + "§7]";
				player.sendMessage(net.minecraft.text.Text.literal(dorikiMessage), true);
			}
		});
	}

	public static Identifier id(String path) {
		return Identifier.of(MOD_ID, path);
	}

	public static final ComponentKey<CombatModeComponent> COMBAT_COMPONENT =
			ComponentRegistry.getOrCreate(
					Identifier.of(MOD_ID, "combat_mode"),
					CombatModeComponent.class
			);

	public static final ComponentKey<AbilityComponent> ABILITY_COMPONENT =
			ComponentRegistry.getOrCreate(
					Identifier.of(MOD_ID, "ability"),
					AbilityComponent.class
			);

	public static final ComponentKey<IHakiComponent> HAKI =
			ComponentRegistry.getOrCreate(Identifier.of(MOD_ID, "haki"), IHakiComponent.class);

	public static final ComponentKey<IDorikiComponent> DORIKI =
			ComponentRegistry.getOrCreate(
					Identifier.of(MOD_ID, "doriki"),
					IDorikiComponent.class
			);

	public static final ComponentKey<IDevilFruitComponent> DEVIL_FRUIT =
			ComponentRegistry.getOrCreate(
					Identifier.of(MOD_ID, "devil_fruit"),
					IDevilFruitComponent.class
			);

	public static final ComponentKey<IFightingStyleComponent> FIGHTING_STYLE =
			ComponentRegistry.getOrCreate(
					Identifier.of(MOD_ID, "fighting_style"),
					IFightingStyleComponent.class
			);

	public static final ComponentKey<IRaceComponent> RACE =
			ComponentRegistry.getOrCreate(
					Identifier.of(MOD_ID, "race"),
					IRaceComponent.class
			);


	@Override
	public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {

		registry.registerForPlayers(
				Redline.COMBAT_COMPONENT,
				CombatModeComponent::new,
				RespawnCopyStrategy.ALWAYS_COPY
		);

		registry.registerForPlayers(
				Redline.ABILITY_COMPONENT,
				AbilityComponent::new,
				RespawnCopyStrategy.ALWAYS_COPY
		);

		registry.registerForPlayers(HAKI, HakiComponentImpl::new, RespawnCopyStrategy.ALWAYS_COPY);

		registry.registerForPlayers(
				Redline.DORIKI,
				DorikiComponentImpl::new,
				RespawnCopyStrategy.ALWAYS_COPY
		);

		registry.registerForPlayers(
				Redline.DEVIL_FRUIT,
				DevilFruitComponentImpl::new,
				RespawnCopyStrategy.ALWAYS_COPY
		);

		registry.registerForPlayers(
				Redline.FIGHTING_STYLE,
				FightingStyleComponentImpl::new,
				RespawnCopyStrategy.ALWAYS_COPY
		);

		registry.registerForPlayers(
				Redline.RACE,
				RaceComponentImpl::new,
				RespawnCopyStrategy.ALWAYS_COPY
		);
	}
}
