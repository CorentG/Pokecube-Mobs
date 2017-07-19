package pokecube.mobs;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

import com.google.common.collect.Maps;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.common.ForgeVersion.CheckResult;
import net.minecraftforge.common.ForgeVersion.Status;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.adventures.utils.DBLoader;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.database.Database;
import pokecube.core.database.Database.EnumDatabase;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.PokedexEntry.EvolutionData;
import pokecube.core.database.stats.CaptureStats;
import pokecube.core.database.stats.EggStats;
import pokecube.core.database.stats.StatsCollector;
import pokecube.core.database.worldgen.XMLWorldgenHandler;
import pokecube.core.events.CaptureEvent.Post;
import pokecube.core.events.CaptureEvent.Pre;
import pokecube.core.events.EvolveEvent;
import pokecube.core.events.handlers.EventsHandler;
import pokecube.core.events.onload.InitDatabase;
import pokecube.core.events.onload.RegisterPokecubes;
import pokecube.core.handlers.HeldItemHandler;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokecube.DefaultPokecubeBehavior;
import pokecube.core.interfaces.IPokecube.NormalPokecubeBehavoir;
import pokecube.core.interfaces.IPokecube.PokecubeBehavior;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.Stats;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.items.berries.BerryManager;
import pokecube.core.items.pokecubes.EntityPokecube;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.utils.Tools;
import pokecube.core.world.gen.template.PokecubeTemplates;
import pokecube.modelloader.CommonProxy;
import pokecube.modelloader.IMobProvider;
import pokecube.modelloader.ModPokecubeML;
import pokecube.modelloader.client.render.ModelWrapperEvent;
import pokecube.origin.render.ModelWrapperSpinda;
import thut.api.maths.Vector3;
import thut.core.client.ClientProxy;
import thut.lib.CompatWrapper;

@Mod(modid = PokecubeMobs.MODID, name = "Pokecube Mobs", version = PokecubeMobs.VERSION, dependencies = "required-after:pokecube", updateJSON = PokecubeMobs.UPDATEURL, acceptableRemoteVersions = "*", acceptedMinecraftVersions = PokecubeMobs.MCVERSIONS)
public class PokecubeMobs implements IMobProvider
{
    public static class UpdateNotifier
    {
        public UpdateNotifier()
        {
            MinecraftForge.EVENT_BUS.register(this);
        }

        @SubscribeEvent
        public void onPlayerJoin(TickEvent.PlayerTickEvent event)
        {
            if (event.player.getEntityWorld().isRemote
                    && event.player == FMLClientHandler.instance().getClientPlayerEntity())
            {
                MinecraftForge.EVENT_BUS.unregister(this);
                Object o = Loader.instance().getIndexedModList().get(PokecubeMobs.MODID);
                CheckResult result = ForgeVersion.getResult(((ModContainer) o));
                if (result.status == Status.OUTDATED)
                {
                    ITextComponent mess = ClientProxy.getOutdatedMessage(result, "Pokecube Mobs");
                    (event.player).addChatMessage(mess);
                }
            }
        }
    }

    Map<PokedexEntry, Integer> genMap     = Maps.newHashMap();
    public static final String MODID      = "pokecube_mobs";
    public static final String VERSION    = "@VERSION@";
    public static final String UPDATEURL  = "https://gist.githubusercontent.com/Thutmose/4d7320c36696cd39b336/raw/mobs.json";

    public final static String MCVERSIONS = "[1.9.4,1.12]";

    public PokecubeMobs()
    {
        ModPokecubeML.scanPaths.add("assets/pokecube_mobs/gen_1/entity/models/");
        ModPokecubeML.scanPaths.add("assets/pokecube_mobs/gen_2/entity/models/");
        ModPokecubeML.scanPaths.add("assets/pokecube_mobs/gen_3/entity/models/");
        ModPokecubeML.scanPaths.add("assets/pokecube_mobs/gen_4/entity/models/");
        ModPokecubeML.scanPaths.add("assets/pokecube_mobs/gen_5/entity/models/");
        ModPokecubeML.scanPaths.add("assets/pokecube_mobs/gen_6/entity/models/");
        ModPokecubeML.scanPaths.add("assets/pokecube_mobs/gen_7/entity/models/");
        CommonProxy.registerModelProvider(MODID, this);

        HeldItemHandler.megaVariants.add("absolmega");
        HeldItemHandler.megaVariants.add("aerodactylmega");
        HeldItemHandler.megaVariants.add("aggronmega");
        HeldItemHandler.megaVariants.add("alakazammega");
        HeldItemHandler.megaVariants.add("altariamega");
        HeldItemHandler.megaVariants.add("ampharosmega");
        HeldItemHandler.megaVariants.add("banettemega");
        HeldItemHandler.megaVariants.add("beedrillmega");
        HeldItemHandler.megaVariants.add("blastoisemega");
        HeldItemHandler.megaVariants.add("blazikenmega");
        HeldItemHandler.megaVariants.add("cameruptmega");
        HeldItemHandler.megaVariants.add("charizardmega-y");
        HeldItemHandler.megaVariants.add("charizardmega-x");
        HeldItemHandler.megaVariants.add("dianciemega");
        HeldItemHandler.megaVariants.add("gallademega");
        HeldItemHandler.megaVariants.add("garchompmega");
        HeldItemHandler.megaVariants.add("gardevoirmega");
        HeldItemHandler.megaVariants.add("gengarmega");
        HeldItemHandler.megaVariants.add("glaliemega");
        HeldItemHandler.megaVariants.add("gyaradosmega");
        HeldItemHandler.megaVariants.add("heracrossmega");
        HeldItemHandler.megaVariants.add("houndoommega");
        HeldItemHandler.megaVariants.add("kangaskhanmega");
        HeldItemHandler.megaVariants.add("latiasmega");
        HeldItemHandler.megaVariants.add("latiosmega");
        HeldItemHandler.megaVariants.add("lucariomega");
        HeldItemHandler.megaVariants.add("manectricmega");
        HeldItemHandler.megaVariants.add("mawilemega");
        HeldItemHandler.megaVariants.add("mewtwomega-y");
        HeldItemHandler.megaVariants.add("mewtwomega-x");
        HeldItemHandler.megaVariants.add("metagrossmega");
        HeldItemHandler.megaVariants.add("pidgeotmega");
        HeldItemHandler.megaVariants.add("pinsirmega");
        HeldItemHandler.megaVariants.add("sableyemega");
        HeldItemHandler.megaVariants.add("salamencemega");
        HeldItemHandler.megaVariants.add("sceptilemega");
        HeldItemHandler.megaVariants.add("scizormega");
        HeldItemHandler.megaVariants.add("sharpedomega");
        HeldItemHandler.megaVariants.add("slowbromega");
        HeldItemHandler.megaVariants.add("steelixmega");
        HeldItemHandler.megaVariants.add("swampertmega");
        HeldItemHandler.megaVariants.add("tyranitarmega");
        HeldItemHandler.megaVariants.add("venusaurmega");
        HeldItemHandler.sortMegaVariants();
        MinecraftForge.EVENT_BUS.register(this);
        initBerries();
        DBLoader.trainerDatabases.add("trainers.xml");
        DBLoader.tradeDatabases.add("trades.xml");
    }

    public static void initBerries()
    {
        BerryManager.addBerry("cheri", 1, 10, 0, 0, 0, 0);// Cures Paralysis
        BerryManager.addBerry("chesto", 2, 0, 10, 0, 0, 0);// Cures sleep
        BerryManager.addBerry("pecha", 3, 0, 0, 10, 0, 0);// Cures poison
        BerryManager.addBerry("rawst", 4, 0, 0, 0, 10, 0);// Cures burn
        BerryManager.addBerry("aspear", 5, 0, 0, 0, 0, 10);// Cures freeze
        BerryManager.addBerry("leppa", 6, 10, 0, 10, 10, 10);// Restores 10PP
        BerryManager.addBerry("oran", 7, 10, 10, 10, 10, 10);// Restores 10HP
        BerryManager.addBerry("persim", 8, 10, 10, 10, 0, 10);// Cures confusion
        BerryManager.addBerry("lum", 9, 10, 10, 10, 10, 0);// Cures any status
                                                           // ailment
        BerryManager.addBerry("sitrus", 10, 0, 10, 10, 10, 10);// Restores 1/4
                                                               // HP
        BerryManager.addBerry("nanab", 18, 0, 0, 10, 10, 0);// Pokeblock
                                                            // ingredient
        BerryManager.addBerry("pinap", 20, 10, 0, 0, 0, 10);// Pokeblock
                                                            // ingredient
        BerryManager.addBerry("pomeg", 21, 10, 0, 10, 10, 0);// EV Berry
        BerryManager.addBerry("kelpsy", 22, 0, 10, 0, 10, 10);// EV Berry
        BerryManager.addBerry("qualot", 23, 10, 0, 10, 0, 10);// EV Berry
        BerryManager.addBerry("hondew", 24, 10, 10, 0, 10, 0);// EV Berry
        BerryManager.addBerry("grepa", 25, 0, 10, 10, 0, 10);// EV Berry
        BerryManager.addBerry("tamato", 26, 20, 10, 0, 0, 0);// EV Berry
        BerryManager.addBerry("cornn", 27, 0, 20, 10, 0, 0);// Pokeblock
                                                            // ingredient
        BerryManager.addBerry("enigma", 60, 40, 10, 0, 0, 0);// Restores 1/4 of
                                                             // HP
        BerryManager.addBerry("jaboca", 63, 0, 0, 0, 40, 10);// 4th gen. Causes
                                                             // recoil damage on
                                                             // foe if holder is
                                                             // hit by a
                                                             // physical move
        BerryManager.addBerry("rowap", 64, 10, 0, 0, 0, 40);// 4th gen. Causes
                                                            // recoil damage on
                                                            // foe if holder is
                                                            // hit by a special
                                                            // move
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        if (event.getSide() == Side.CLIENT)
        {
            new UpdateNotifier();
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void initModel(ModelWrapperEvent evt)
    {
        if (evt.name.equalsIgnoreCase("spinda"))
        {
            evt.wrapper = new ModelWrapperSpinda(evt.wrapper.model, evt.wrapper.renderer);
        }
    }

    @Override
    public String getModelDirectory(PokedexEntry entry)
    {
        int gen = getGen(entry);
        switch (gen)
        {
        case 1:
            return "gen_1/entity/models/";
        case 2:
            return "gen_2/entity/models/";
        case 3:
            return "gen_3/entity/models/";
        case 4:
            return "gen_4/entity/models/";
        case 5:
            return "gen_5/entity/models/";
        case 6:
            return "gen_6/entity/models/";
        case 7:
            return "gen_7/entity/models/";
        }
        return "models/";
    }

    private int getGen(PokedexEntry entry)
    {
        int gen;
        if (genMap.containsKey(entry))
        {
            gen = genMap.get(entry);
        }
        else
        {
            gen = entry.getGen();
            PokedexEntry real = entry;
            if (entry.getBaseForme() != null) entry = entry.getBaseForme();
            for (EvolutionData d : entry.getEvolutions())
            {
                int gen1 = d.evolution.getGen();
                if (genMap.containsKey(d.evolution))
                {
                    gen1 = genMap.get(d.evolution);
                }
                if (gen1 < gen)
                {
                    gen = gen1;
                }
                for (EvolutionData d1 : d.evolution.getEvolutions())
                {
                    gen1 = d1.evolution.getGen();
                    if (genMap.containsKey(d1.evolution))
                    {
                        gen1 = genMap.get(d1.evolution);
                    }
                    if (d.evolution == entry && gen1 < gen)
                    {
                        gen = gen1;
                    }
                }
            }
            for (PokedexEntry e : Database.allFormes)
            {
                int gen1 = e.getGen();
                if (genMap.containsKey(e))
                {
                    gen1 = genMap.get(e);
                }
                for (EvolutionData d : e.getEvolutions())
                {
                    if (d.evolution == entry && gen1 < gen)
                    {
                        gen = gen1;
                    }
                }
            }
            genMap.put(real, gen);
        }
        return gen;
    }

    @Override
    public String getTextureDirectory(PokedexEntry entry)
    {
        int gen = getGen(entry);
        switch (gen)
        {
        case 1:
            return "gen_1/entity/textures/";
        case 2:
            return "gen_2/entity/textures/";
        case 3:
            return "gen_3/entity/textures/";
        case 4:
            return "gen_4/entity/textures/";
        case 5:
            return "gen_5/entity/textures/";
        case 6:
            return "gen_6/entity/textures/";
        case 7:
            return "gen_7/entity/textures/";
        }
        return "textures/entities/";
    }

    @Override
    public Object getMod()
    {
        return this;
    }

    @SubscribeEvent
    public void registerPokecubes(RegisterPokecubes event)
    {
        String[] cubes = { "poke", "great", "ultra", "master", "snag", "dusk", "quick", "timer", "net", "nest", "dive",
                "repeat", "premier", "cherish" };
        int[] indecies = { 0, 1, 2, 3, 99, 5, 6, 7, 8, 9, 10, 11, 12, 13 };
        for (int i = 0; i < cubes.length; i++)
        {
            event.cubePrefixes.put(indecies[i], cubes[i]);
        }

        final PokecubeHelper helper = new PokecubeHelper();

        event.behaviors.put(0, new NormalPokecubeBehavoir(1));
        event.behaviors.put(1, new NormalPokecubeBehavoir(1.5));
        event.behaviors.put(2, new NormalPokecubeBehavoir(2));
        event.behaviors.put(3, new NormalPokecubeBehavoir(255));
        event.behaviors.put(5, new DefaultPokecubeBehavior()
        {
            @Override
            public double getCaptureModifier(IPokemob mob)
            {
                return helper.dusk(mob);
            }
        });
        event.behaviors.put(6, new DefaultPokecubeBehavior()
        {
            @Override
            public double getCaptureModifier(IPokemob mob)
            {
                return helper.quick(mob);
            }
        });
        event.behaviors.put(7, new DefaultPokecubeBehavior()
        {
            @Override
            public double getCaptureModifier(IPokemob mob)
            {
                return helper.timer(mob);
            }
        });
        event.behaviors.put(8, new DefaultPokecubeBehavior()
        {
            @Override
            public double getCaptureModifier(IPokemob mob)
            {
                return helper.net(mob);
            }
        });
        event.behaviors.put(9, new DefaultPokecubeBehavior()
        {
            @Override
            public double getCaptureModifier(IPokemob mob)
            {
                return helper.nest(mob);
            }
        });
        event.behaviors.put(10, new DefaultPokecubeBehavior()
        {
            @Override
            public double getCaptureModifier(IPokemob mob)
            {
                return helper.dive(mob);
            }
        });
        event.behaviors.put(12, new NormalPokecubeBehavoir(1));
        event.behaviors.put(13, new NormalPokecubeBehavoir(1));

        PokecubeBehavior snag = new PokecubeBehavior()
        {

            @Override
            public void onPostCapture(Post evt)
            {
                IPokemob mob = evt.caught;
                evt.pokecube.entityDropItem(PokecubeManager.pokemobToItem(mob), 1.0F);
                evt.setCanceled(true);
            }

            @Override
            public void onPreCapture(Pre evt)
            {
                boolean tameSnag = !evt.caught.isPlayerOwned() && evt.caught.getPokemonAIState(IMoveConstants.TAMED);

                if (evt.caught.isShadow())
                {
                    EntityPokecube cube = (EntityPokecube) evt.pokecube;

                    IPokemob mob = (IPokemob) PokecubeCore.instance.createPokemob(evt.caught.getPokedexEntry(),
                            cube.getEntityWorld());
                    cube.tilt = Tools.computeCatchRate(mob, 1);
                    cube.time = cube.tilt * 20;

                    if (!tameSnag) evt.caught.setPokecube(evt.filledCube);

                    cube.setEntityItemStack(PokecubeManager.pokemobToItem(evt.caught));
                    PokecubeManager.setTilt(cube.getEntityItem(), cube.tilt);
                    Vector3.getNewVector().set(evt.pokecube).moveEntity(cube);
                    ((Entity) evt.caught).setDead();
                    cube.motionX = cube.motionZ = 0;
                    cube.motionY = 0.1;
                    cube.getEntityWorld().spawnEntityInWorld(cube.copy());
                    evt.pokecube.setDead();
                }
                evt.setCanceled(true);
            }

            @Override
            public double getCaptureModifier(IPokemob mob)
            {
                return 0;
            }
        };

        PokecubeBehavior repeat = new PokecubeBehavior()
        {
            @Override
            public void onPostCapture(Post evt)
            {

            }

            @Override
            public void onPreCapture(Pre evt)
            {
                if (evt.getResult() == Result.DENY) return;

                EntityPokecube cube = (EntityPokecube) evt.pokecube;

                IPokemob mob = (IPokemob) PokecubeCore.instance.createPokemob(evt.caught.getPokedexEntry(),
                        cube.getEntityWorld());
                Vector3 v = Vector3.getNewVector();
                Entity thrower = cube.shootingEntity;
                int has = CaptureStats.getTotalNumberOfPokemobCaughtBy(thrower.getUniqueID(), mob.getPokedexEntry());
                has = has + EggStats.getTotalNumberOfPokemobHatchedBy(thrower.getUniqueID(), mob.getPokedexEntry());
                double rate = has > 0 ? 3 : 1;
                cube.tilt = Tools.computeCatchRate(mob, rate);
                cube.time = cube.tilt * 20;
                evt.caught.setPokecube(evt.filledCube);
                cube.setEntityItemStack(PokecubeManager.pokemobToItem(evt.caught));
                PokecubeManager.setTilt(cube.getEntityItem(), cube.tilt);
                v.set(evt.pokecube).moveEntity(cube);
                v.moveEntity((Entity) mob);
                ((Entity) evt.caught).setDead();
                cube.motionX = cube.motionZ = 0;
                cube.motionY = 0.1;
                cube.getEntityWorld().spawnEntityInWorld(cube.copy());
                evt.setCanceled(true);
                evt.pokecube.setDead();
            }

            @Override
            public double getCaptureModifier(IPokemob mob)
            {
                return 0;
            }

        };

        event.behaviors.put(99, snag);
        event.behaviors.put(11, repeat);
    }

    @SubscribeEvent
    public void makeShedinja(EvolveEvent.Post evt)
    {
        Entity owner;
        if ((owner = evt.mob.getPokemonOwner()) instanceof EntityPlayer)
        {
            makeShedinja(evt.mob, (EntityPlayer) owner);
        }
    }

    @SubscribeEvent
    public void livingUpdate(LivingUpdateEvent evt)
    {
        if (evt.getEntityLiving() instanceof IPokemob && ((IPokemob) evt.getEntityLiving()).getPokedexNb() == 213)
        {
            IPokemob shuckle = (IPokemob) evt.getEntityLiving();

            if (evt.getEntityLiving().getEntityWorld().isRemote) return;

            ItemStack item = evt.getEntityLiving().getHeldItemMainhand();
            if (!CompatWrapper.isValid(item)) return;
            Item itemId = item.getItem();
            boolean berry = item.isItemEqual(BerryManager.getBerryItem("oran"));
            Random r = new Random();
            if (berry && r.nextGaussian() > EventsHandler.juiceChance)
            {
                if (shuckle.getPokemonOwner() != null)
                {
                    String message = "A sweet smell is coming from "
                            + shuckle.getPokemonDisplayName().getFormattedText();
                    ((EntityPlayer) shuckle.getPokemonOwner()).addChatMessage(new TextComponentString(message));
                }
                shuckle.setHeldItem(new ItemStack(PokecubeItems.berryJuice));
                return;
            }
            berry = itemId == PokecubeItems.berryJuice;
            if (berry && (r.nextGaussian() > EventsHandler.candyChance))
            {
                ItemStack candy = PokecubeItems.makeCandyStack();
                if (!CompatWrapper.isValid(candy)) return;

                if (shuckle.getPokemonOwner() != null)
                {
                    String message = "The smell coming from " + shuckle.getPokemonDisplayName().getFormattedText()
                            + " has changed";
                    ((EntityPlayer) shuckle.getPokemonOwner()).addChatMessage(new TextComponentString(message));
                }
                shuckle.setHeldItem(candy);
                return;
            }
        }
    }

    @SubscribeEvent
    public void evolveTyrogue(EvolveEvent.Pre evt)
    {
        if (evt.mob.getPokedexEntry() == Database.getEntry("Tyrogue"))
        {
            int atk = evt.mob.getStat(Stats.ATTACK, false);
            int def = evt.mob.getStat(Stats.DEFENSE, false);
            if (atk > def) evt.forme = Database.getEntry("Hitmonlee");
            else if (def > atk) evt.forme = Database.getEntry("Hitmonchan");
            else evt.forme = Database.getEntry("Hitmontop");
        }
    }

    void makeShedinja(IPokemob evo, EntityPlayer player)
    {
        if (evo.getPokedexEntry() == Database.getEntry("ninjask"))
        {
            InventoryPlayer inv = player.inventory;
            boolean hasCube = false;
            boolean hasSpace = false;
            ItemStack cube = CompatWrapper.nullStack;
            int m = -1;
            for (int n = 0; n < inv.getSizeInventory(); n++)
            {
                ItemStack item = inv.getStackInSlot(n);
                if (item == CompatWrapper.nullStack) hasSpace = true;
                if (!hasCube && PokecubeItems.getCubeId(item) >= 0 && !PokecubeManager.isFilled(item))
                {
                    hasCube = true;
                    cube = item;
                    m = n;
                }
                if (hasCube && hasSpace) break;

            }
            if (hasCube && hasSpace)
            {
                Entity pokemon = PokecubeMod.core.createPokemob(Database.getEntry("shedinja"), player.getEntityWorld());
                if (pokemon != null)
                {
                    ItemStack mobCube = cube.copy();
                    CompatWrapper.setStackSize(mobCube, 1);
                    IPokemob poke = (IPokemob) pokemon;
                    poke.setPokecube(mobCube);
                    poke.setPokemonOwner(player);
                    poke.setExp(Tools.levelToXp(poke.getExperienceMode(), 20), true);
                    ((EntityLivingBase) poke).setHealth(((EntityLivingBase) poke).getMaxHealth());
                    ItemStack shedinja = PokecubeManager.pokemobToItem(poke);
                    StatsCollector.addCapture(poke);
                    CompatWrapper.increment(cube, -1);
                    if (!CompatWrapper.isValid(cube)) inv.setInventorySlotContents(m, CompatWrapper.nullStack);
                    inv.addItemStackToInventory(shedinja);
                }
            }
        }
    }

    @SubscribeEvent
    public void registerDatabases(InitDatabase.Pre evt)
    {
        checkConfigFiles();
        Database.addDatabase("pokemobs.json", EnumDatabase.POKEMON);
    }

    public static void checkConfigFiles()
    {
        File file = new File("./config/pokecube.cfg");
        String seperator = System.getProperty("file.separator");
        String folder = file.getAbsolutePath();
        String name = file.getName();
        Database.CONFIGLOC = folder.replace(name, "pokecube" + seperator + "database" + seperator + "");
        PokecubeTemplates.TEMPLATES = folder.replace(name, "pokecube" + seperator + "structures" + seperator + "");
        PokecubeTemplates.initFiles();
        XMLWorldgenHandler.loadDefaults(new File(PokecubeTemplates.TEMPLATES, "worldgen.xml"));
        writeDefaultConfig();
        return;
    }

    private static void writeDefaultConfig()
    {
        try
        {
            File temp = new File(Database.CONFIGLOC);
            if (!temp.exists())
            {
                temp.mkdirs();
            }
            copyDatabaseFile("moves.json");
            copyDatabaseFile("animations.json");
            copyDatabaseFile("pokemobs.json");
            Database.DBLOCATION = Database.DBLOCATION.replace("pokecube", "pokecube_adventures");
            Database.CONFIGLOC = Database.CONFIGLOC.replace("database", "trainers");
            copyDatabaseFile("trainers.xml");
            copyDatabaseFile("trades.xml");
            Database.DBLOCATION = Database.DBLOCATION.replace("pokecube_adventures", "pokecube");
            Database.CONFIGLOC = Database.CONFIGLOC.replace("trainers", "database");
            Database.DBLOCATION = Database.CONFIGLOC;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    static void copyDatabaseFile(String name)
    {
        File temp1 = new File(Database.CONFIGLOC + name);
        if (temp1.exists() && !Database.FORCECOPY)
        {
            System.out.println(" Not Overwriting old database " + name);
            return;
        }
        ArrayList<String> rows = Database.getFile(Database.DBLOCATION + name);
        int n = 0;
        try
        {
            File file = new File(Database.CONFIGLOC + name);
            Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
            for (int i = 0; i < rows.size(); i++)
            {
                out.write(rows.get(i) + "\n");
                n++;
            }
            out.close();
        }
        catch (Exception e)
        {
            System.err.println(name + " " + n);
            e.printStackTrace();
        }
    }
}
